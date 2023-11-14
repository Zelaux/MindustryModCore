package mmc.annotations.entities;

import arc.struct.*;
import arc.util.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.*;
import mindustry.annotations.*;
import mindustry.annotations.util.*;
import mmc.annotations.*;
import mmc.annotations.ModAnnotations.*;
import mmc.annotations.entities.ModEntityProcess.*;

import javax.lang.model.element.Modifier;
import java.lang.reflect.*;

public class MindustrySerializationGenerator{
    static CodeBlock.Builder codeBuilder(MethodSpec.Builder builder){
        try{
            Field field = MethodSpec.Builder.class.getDeclaredField("code");
            field.setAccessible(true);
            return (CodeBlock.Builder)field.get(builder);
        }catch(NoSuchFieldException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    public void generate(ModEntityProcess processor) throws Exception{

        Builder builder = TypeSpec.classBuilder("MindustrySerialization")
        .addModifiers(Modifier.PUBLIC);
        ObjectMap<String, Selement> usedNames = new ObjectMap<>();
        ObjectMap<Selement, ObjectSet<String>> extraNames = new ObjectMap<>();
        for(Selement<?> type : processor.mindustryDefs){
            MindustryEntityDef ann = type.annotation(MindustryEntityDef.class);
            if(ann.serialize()) continue;
            //all component classes (not interfaces)
            Seq<Stype> components = processor.allComponents(type);
            Seq<GroupDefinition> groups = processor.groupDefs.select(g -> (!g.components.isEmpty() && !g.components.contains(s -> !components.contains(s))) || g.manualInclusions.contains(type));
            ObjectMap<String, Seq<Smethod>> methods = new ObjectMap<>();
            ObjectMap<FieldSpec, Svar> specVariables = new ObjectMap<>();
            ObjectSet<String> usedFields = new ObjectSet<>();

            //make sure there's less than 2 base classes
            Seq<Stype> baseClasses = components.select(s -> s.annotation(Annotations.Component.class).base());
            if(baseClasses.size > 2){
                BaseProcessor.err("No entity may have more than 2 base classes. Base classes: " + baseClasses, type);
            }

            //get base class type name for extension
            Stype baseClassType = baseClasses.any() ? baseClasses.first() : null;
            @Nullable TypeName baseClass = baseClasses.any() ? BaseProcessor.tname(BaseProcessor.packageName + "." + processor.baseName(baseClassType)) : null;
            //whether the main class is the base itself
            boolean typeIsBase = baseClassType != null && type.has(Annotations.Component.class) && type.annotation(Annotations.Component.class).base();

            if(type.isType() && (!type.name().endsWith("Def") && !type.name().endsWith("Comp"))){
                BaseProcessor.err("All entity def names must end with 'Def'/'Comp'", type.e);
            }

            String name = type.isType() ?
            type.name().replace("Def", "").replace("Comp", "") :
            processor.createName(type);

            //check for type name conflicts
            if(!typeIsBase && baseClass != null && name.equals(processor.baseName(baseClassType))){
                name += "Entity";
            }

            if(ann.legacy()){
                name += "Legacy" + Strings.capitalize(type.name());
            }

            //skip double classes
            if(usedNames.containsKey(name)){
                extraNames.get(usedNames.get(name), ObjectSet::new).add(type.name());
                continue;
            }

            usedNames.put(name, type);
            extraNames.get(type, ObjectSet::new).add(name);
            if(!type.isType()){
                extraNames.get(type, ObjectSet::new).add(type.name());
            }

//            Builder entityClassBuilder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);
            //add serialize() boolean
            if(!components.contains(t -> t.methods().contains(m -> {
                return m.name().equals("serialize") && m.params().isEmpty() && m.has(ReplaceInternalImpl.class);
            }))){
//                entityClassBuilder.addMethod(MethodSpec.methodBuilder("serialize").addModifiers(Modifier.PUBLIC).returns(boolean.class).addStatement("return " + ann.serialize()).build());
            }

            //all SyncField fields
            Seq<Svar> syncedFields = new Seq<>();
            Seq<Svar> allFields = new Seq<>();
            Seq<FieldSpec> allFieldSpecs = new Seq<>();

            boolean isSync = components.contains(s -> s.name().contains("Sync"));

            //add all components
            for(Stype comp : components){
                //whether this component's fields are defined in the base class
                boolean isShadowed = baseClass != null && !typeIsBase && processor.baseClassDeps.get(baseClassType).contains(comp);

                //write fields to the class; ignoring transient/imported ones
                Seq<Svar> fields = comp.fields().select(f -> !f.has(Annotations.Import.class));
                for(Svar f : fields){
                    if(!usedFields.add(f.name())){
                        BaseProcessor.err("Field '" + f.name() + "' of component '" + comp.name() + "' redefines a field in entity '" + type.name() + "'");
                        continue;
                    }

                    FieldSpec.Builder fbuilder = FieldSpec.builder(f.tname(), f.name());
                    //keep statics/finals
                    if(f.is(Modifier.STATIC)){
                        fbuilder.addModifiers(Modifier.STATIC);
                        if(f.is(Modifier.FINAL)) fbuilder.addModifiers(Modifier.FINAL);
                    }
                    //add transient modifier for serialization
                    if(f.is(Modifier.TRANSIENT)){
                        fbuilder.addModifiers(Modifier.TRANSIENT);
                    }

                    //add initializer if it exists
                    if(processor.varInitializers.containsKey(f.descString())){
                        fbuilder.initializer(processor.varInitializers.get(f.descString()));
                    }

                    fbuilder.addModifiers(f.has(Annotations.ReadOnly.class) ? Modifier.PROTECTED : Modifier.PUBLIC);
                    fbuilder.addAnnotations(f.annotations().map(AnnotationSpec::get));
                    FieldSpec spec = fbuilder.build();

                    //whether this field would be added to the superclass
                    boolean isVisible = !f.is(Modifier.STATIC) && !f.is(Modifier.PRIVATE) && !f.has(Annotations.ReadOnly.class);

                    //add the field only if it isn't visible or it wasn't implemented by the base class
                    if(!isShadowed || !isVisible){
//                        entityClassBuilder.addField(spec);
                    }

                    specVariables.put(spec, f);

                    allFieldSpecs.add(spec);
                    allFields.add(f);

                    //add extra sync fields
                    if(f.has(Annotations.SyncField.class) && isSync){
                        if(!f.tname().toString().equals("float")) BaseProcessor.err("All SyncFields must be of type float", f);

                        syncedFields.add(f);

                        //a synced field has 3 values:
                        //- target state
                        //- last state
                        //- current state (the field itself, will be written to)

                        //target
//                        entityClassBuilder.addField(FieldSpec.builder(float.class, f.name() + ModEntityIO.targetSuf).addModifiers(Modifier.TRANSIENT, Modifier.PRIVATE).build());

                        //last
//                        entityClassBuilder.addField(FieldSpec.builder(float.class, f.name() + ModEntityIO.lastSuf).addModifiers(Modifier.TRANSIENT, Modifier.PRIVATE).build());
                    }
                }

                //get all methods from components
                for(Smethod elem : comp.methods()){
                    methods.get(elem.toString(), Seq::new).add(elem);
                }
            }

            syncedFields.sortComparing(Selement::name);

            //override toString method
            /*entityClassBuilder.addMethod(MethodSpec.methodBuilder("toString")
            .addAnnotation(Override.class)
            .returns(String.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return $S + $L", name + "#", "id").build());*/

            ModEntityIO io = new ModEntityIO(type.name(), /*entityClassBuilder*/null, allFieldSpecs, processor.serializer,
            processor.rootDirectory().child(processor.annotationsSettings(AnnotationSettingsEnum.revisionsPath, "annotations/src/main/resources/revisions")).child(type.name()));


            ClassName className = ClassName.get("mindustry.gen", type.name().replace("Comp", ""));
            //entities with no sync comp and no serialization gen no code
            boolean hasIO = ann.genio() && (components.contains(s -> s.name().contains("Sync")) || true);

//            System.out.println(methods.keys().toSeq());
            processWriteMethod(processor,
            builder,
            processor.generateMethod(type, ann.pooled(), groups, syncedFields, allFields, io, hasIO, methods.get("write(arc.util.io.Writes)").copy(), "write(arc.util.io.Writes)"),
            className);

            processReadMethod(processor,
            builder,
            processor.generateMethod(type, ann.pooled(), groups, syncedFields, allFields, io, hasIO, methods.get("read(arc.util.io.Reads)").copy(), "read(arc.util.io.Reads)"),
            className);
            /*
            builder.addMethod(writeMethod)*/
            //add all methods from components
            /*for(ObjectMap.Entry<String, Seq<Smethod>> entry : methods){
                Seq<Smethod> smethods = entry.value.copy();
                String methodFullName = entry.key;
                if(smethods.contains(m -> m.has(Annotations.Replace.class))){
                    //check replacements
                    if(smethods.count(m -> m.has(Annotations.Replace.class)) > 1){
                        processor.err("Type " + type + " has multiple components replacing method " + methodFullName + ".");
                    }
                    Smethod base = smethods.find(m -> m.has(Annotations.Replace.class));
                    smethods.clear();
                    smethods.add(base);
                }
                //check multi return
                if(smethods.count(m -> !m.isAny(Modifier.NATIVE, Modifier.ABSTRACT) && !m.isVoid()) > 1){
                    processor.err("Type " + type + " has multiple components implementing non-void method " + methodFullName + ".");
                }


            }*/
        }
        builder.addMethod(MethodSpec.methodBuilder("afterRead").addModifiers(Modifier.PRIVATE, Modifier.STATIC).build());
//        System.out.println(builder.build());
        ModBaseProcessor.write(builder, BaseProcessor.packageName);
    }

    private void processReadMethod(ModEntityProcess processor, Builder builder, MethodSpec.Builder readMethod, ClassName className) throws Exception{
        readMethod.setName("read" + className.simpleName()).returns(className);
        readMethod.addModifiers(Modifier.STATIC);

        CodeBlock.Builder readBuilder = codeBuilder(readMethod);
        BlockStmt blockStmt = StaticJavaParser.parseBlock("{" + readBuilder.build() + "}");
        blockStmt.addStatement(0,StaticJavaParser.parseStatement(className.simpleName()+" OBJECT_TO_READ = "+className.simpleName()+".create();"));
        for(AssignExpr assignExpr : blockStmt.findAll(AssignExpr.class)){
            Expression t = assignExpr.getTarget();
            FieldAccessExpr target;
            //noinspection ConstantConditions
            if(!(t instanceof FieldAccessExpr && (target= (FieldAccessExpr)t)==t) || !target.getScope().isThisExpr()){
                continue;
            }
            MethodCallExpr methodCallExpr = new MethodCallExpr(target.getScope(), target.getName(), NodeList.nodeList(assignExpr.getValue()));
//            System.out.println("hmmm: "+methodCallExpr);
            assignExpr.replace(methodCallExpr);
//        assignExpr.replace(StaticJavaParser.parseExpression("someObject"))
        }
        blockStmt.findAll(ThisExpr.class).forEach(it -> it.replace(StaticJavaParser.parseExpression("OBJECT_TO_READ")));
        blockStmt.addStatement("return OBJECT_TO_READ;");

        readBuilder.clear();
        String string = blockStmt.toString();
        readBuilder.add(string.substring(1, string.length() - 1));
        builder.addMethod(readMethod.build());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void processWriteMethod(ModEntityProcess processor, Builder builder, MethodSpec.Builder writeMethod, ClassName className) throws Exception{
        writeMethod.setName("write" + className.simpleName()).addParameter(className, "OBJECT_TO_WRITE");
        writeMethod.addModifiers(Modifier.STATIC);

        CodeBlock.Builder writeBuilder = codeBuilder(writeMethod);
        BlockStmt blockStmt = StaticJavaParser.parseBlock("{" + writeBuilder.build() + "}");

        blockStmt.findAll(ThisExpr.class).forEach(it -> {
            Expression expression = (Expression)it.getParentNode().get();
            if(!expression.isFieldAccessExpr()){
                throw new RuntimeException("expression: " + expression);
            }else{
                FieldAccessExpr expr = expression.asFieldAccessExpr();
                expr.setName(expr.getNameAsString() + "()");
//                System.out.println("getIdentifier:" +expr.getName());
            }
            it.replace(StaticJavaParser.parseExpression("OBJECT_TO_WRITE"));
        });
//        blockStmt.addStatement("return OBJECT_TO_WRITE;");

        writeBuilder.clear();
        String string = blockStmt.toString();
        writeBuilder.add(string.substring(1, string.length() - 1));
        builder.addMethod(writeMethod.build());
    }
}
