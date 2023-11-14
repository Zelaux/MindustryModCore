package mmc.annotations.entities;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.pooling.*;
import arc.util.pooling.Pool.*;
import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.*;
import com.sun.tools.javac.processing.*;
import mindustry.annotations.*;
import mindustry.annotations.Annotations.*;
import mindustry.annotations.util.*;
import mindustry.mod.Mods.*;
import mmc.annotations.*;
import mmc.annotations.SupportedAnnotationTypes;
import mmc.annotations.ModAnnotations.*;
import mmc.annotations.remote.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.*;
import java.io.*;
import java.lang.annotation.*;
import java.util.*;

@SupportedAnnotationTypes({
    mindustry.annotations.Annotations.EntityDef.class,
    mindustry.annotations.Annotations.EntityInterface.class,
    mindustry.annotations.Annotations.BaseComponent.class,
    mindustry.annotations.Annotations.Component.class,
    mindustry.annotations.Annotations.TypeIOHandler.class,
    mmc.annotations.ModAnnotations.EntitySuperClass.class,
    mmc.annotations.ModAnnotations.CreateMindustrySerialization.class,
})
public class ModEntityProcess extends ModBaseProcessor{
    final Seq<Stype> baseComponents = new Seq<>();
    private final Seq<Stype> allComponents = new Seq<>();
    Seq<EntityDefinition> definitions = new Seq<>();
    Seq<Stype> allInterfaces = new Seq<>();
    @SuppressWarnings("rawtypes")
    Seq<Selement> allGroups = new Seq<>();
    @SuppressWarnings("rawtypes")
    Seq<Selement> allDefs = new Seq<>();
    @SuppressWarnings("rawtypes")
    Seq<Selement> mindustryDefs = new Seq<>();
    Seq<GroupDefinition> groupDefs = new Seq<>();
    ObjectMap<String, Stype> componentNames = new ObjectMap<>();
    ObjectMap<Stype, Seq<Stype>> componentDependencies = new ObjectMap<>();
    @SuppressWarnings("rawtypes")
    ObjectMap<Selement, Seq<Stype>> defComponents = new ObjectMap<>();
    ObjectMap<String, String> varInitializers = new ObjectMap<>();
    ObjectMap<String, String> methodBlocks = new ObjectMap<>();
    ObjectMap<Stype, ObjectSet<Stype>> baseClassDeps = new ObjectMap<>();
    ObjectSet<String> imports = new ObjectSet<>();
    Seq<TypeSpec.Builder> baseClasses = new Seq<>();
    ObjectSet<TypeSpec.Builder> baseClassIndexers = new ObjectSet<>();
    TypeIOResolver.ClassSerializer serializer;
    CreateMindustrySerialization createMindustrySerialization;
    //    Seq<String> anukeComponents = new Seq<>();
    boolean hasAnukeComps = false;
    boolean secondIsFinalRound = false;
    @SuppressWarnings("FieldCanBeLocal")
    private String compByAnukePackage;

    {
        rounds = 5;
    }

    @Override
    public void process(RoundEnvironment env) throws Exception{
        updateRounds();
        for(Stype type : types(ModAnnotations.EntitySuperClass.class)){
            if(!hasAnukeComps){
//                err();
                note("Local mindustry components.");
            }
            hasAnukeComps = true;
            allInterfaces.add(type.superclasses().peek());
        }
        int round;
        boolean debug = false;
        if(rootPackageName.equals("mmc")){
            if(this.round == 1) rounds -= 1;
            round = this.round;
            debug = true;
        }else{
            round = this.round - 1;
        }
        if(allComponents.isEmpty() && allDefs.isEmpty() && createMindustrySerialization == null){
            System.out.println("fast ending");
            this.round = rounds;
            return;
        }
        try{
            if(round == 0){
                System.out.println("Creating default components");
                zeroRound();
            }
            boolean generated = false;
            if(round == 1){
//                Log.info("First round");
                System.out.println("Generating Component's interfaces");
                generated |= firstRound();
                secondIsFinalRound = debug && !((JavacFiler)filer).newFiles();
            }
            if(round == 2 || round == 1 && !generated){
                if(round == 1)this.round++;
                System.out.println("Generating EntityMapping");
//                Log.info("Second round");
                secondRound();
            }
            if(round == 3 || (round == 2 && secondIsFinalRound)){
//                this.round=rounds;

                if(createMindustrySerialization != null){
                    boolean root = rootPackageName.equals("mmc");
//                  if(!root) throw new RuntimeException("You cannot use createMindustrySerialization")
                    if(root){
                        messager.printMessage(Diagnostic.Kind.NOTE, "Generating MindustrySerialization.");
//                        System.out.println("Generation minds");
                        new MindustrySerializationGenerator().generate(this);
                    }else{
                        messager.printMessage(Diagnostic.Kind.WARNING, "Cannot generate MindustrySerialization.");
                    }
                }
            }
            if(round == 3 || (round == 2 && secondIsFinalRound)){
                System.out.println("Generation Entity");
//                Log.info("Third round");
                thirdRound();
                clearZeroRound();
            }
        }catch(Exception e){
            throw e;
        }
//        System.out.println("post process: " + this.round + "/" + rounds);
    }

    private void clearZeroRound(){
        boolean root = rootPackageName.equals("mmc");
        if(root) return;
        try{
            Fi filesFi = getFilesFi(StandardLocation.SOURCE_OUTPUT);
            Fi mmc = filesFi.child("mmc");
//            mmc.walk(Fi::delete);
            mmc.deleteDirectory();
//            mmc.delete();
            if(mmc.exists()) throw new RuntimeException("Cannot delete mmc package!!!");
        }catch(IOException exception){
            Log.err("Cannot delete mmc package because @", exception);
        }
    }

    private void zeroRound(){
        try{
           /* for(Field field : Groups.class.getDeclaredFields()){
                field.
            }*/
            boolean root = rootPackageName.equals("mmc");
            if(root && !getFilesFi(StandardLocation.CLASS_OUTPUT).absolutePath().contains("tests/build")) return;
            compByAnukePackage = "mmc.entities.compByAnuke";
            Fi tmp = Fi.tempFile("zelaux-comp-tmp");
            for(Entry<String, String> entry : CompData.compMap){
                String compName = entry.key;
                String code = entry.value;
                tmp.writeString(code);
                JavaFileObject object = filer.createSourceFile(compByAnukePackage + "." + compName);
                OutputStream stream = object.openOutputStream();
                stream.write(tmp.readBytes());
                stream.close();
            }
            tmp.writeString(CompData.groupDefs);

            JavaFileObject object = filer.createSourceFile("mmc.entities.GroupDefs");
            OutputStream stream = object.openOutputStream();
            stream.write(tmp.readBytes());
            stream.close();

            tmp.delete();
        }catch(IOException e){
            err(Strings.getStackTrace(e));
        }
    }

    private void updateRounds(){
        allGroups.addAll(elements(Annotations.GroupDef.class));
        allInterfaces.addAll(types(Annotations.EntityInterface.class));
        allDefs.addAll(elements(Annotations.EntityDef.class));
        mindustryDefs.addAll(elements(MindustryEntityDef.class));
        if(serializer == null){
            serializer = ModTypeIOResolver.resolve(this);
        }
        baseComponents.addAll(types(Annotations.BaseComponent.class));
        allComponents.addAll(types(Annotations.Component.class));
        if(createMindustrySerialization == null){
            Seq<Selement<?>> types = elements(CreateMindustrySerialization.class).as();
            if(types.any()){
                createMindustrySerialization = types.first().annotation(CreateMindustrySerialization.class);
            }
        }
    }

    private boolean firstRound() throws Exception{
        Seq<Stype> allComponents = this.allComponents.copy();
        boolean generatedAny = false;
        //store code
        for(Stype component : allComponents){


            for(Svar f : component.fields()){

                //add initializer if it exists
                if(f.tree().getInitializer() != null){
                    String init = f.tree().getInitializer().toString();
                    varInitializers.put(f.descString(), init);
                }
            }
            TypeSpec.Builder defaultImpl = null;
            if(component.annotation(GenerateDefaultImplementation.class) != null){

                defaultImpl = TypeSpec.interfaceBuilder(component.name().replace("Comp", "cImpl"))
                    .addSuperinterface(ClassName.bestGuess(interfaceName(component)))
                    .addModifiers(Modifier.PUBLIC)

                ;
            }
            for(Smethod elem : component.methods()){
                if(elem.is(Modifier.ABSTRACT) || elem.is(Modifier.NATIVE)) continue;
                //get all statements in the method, store them
                String stringBody = elem.tree().getBody().toString();
                String value = stringBody
                    .replaceAll("this\\.<(.*)>self\\(\\)", "this") //fix parameterized self() calls
                    .replaceAll("self\\(\\)", "this") //fix self() calls
                    .replaceAll(" yield ", "") //fix enchanced switch
                    .replaceAll("\\/\\*missing\\*\\/", "var");
                methodBlocks.put(elem.descString(), value //fix vars
                );
                if(defaultImpl != null){
                    defaultImpl.addMethod(MethodSpec.methodBuilder(elem.name())
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                        .addCode(stringBody)
                        .addParameters(Seq.with(elem.params()).map(it -> ParameterSpec.builder(it.tname(), it.name()).build()))
                        .returns(elem.retn())
                        .build()
                    );
                }
            }
            if(defaultImpl != null){
                generatedAny = true;
                write(defaultImpl, getImports(component.e));
            }
        }

        //store components
        for(Stype type : allComponents){
            componentNames.put(type.name(), type);
        }


        //add component imports
        for(Stype comp : allComponents){
            imports.addAll(getImports(comp.e));
        }

        //create component interfaces
        for(Stype component : allComponents){
            Seq<Stype> depends = getDependencies(component);
            TypeSpec.Builder inter = null;
            boolean anuke = component.fullName().contains("compByAnuke");
            if(!anuke){
                inter = TypeSpec.interfaceBuilder(interfaceName(component))
                    .addModifiers(Modifier.PUBLIC).addAnnotation(Annotations.EntityInterface.class);

                inter.addJavadoc("Interface for {@link $L}", component.fullName());
                skipDeprecated(inter);

                //implement extra interfaces these components may have, e.g. position
                for(Stype extraInterface : component.interfaces().select(i -> !isCompInterface(i))){
                    //javapoet completely chokes on this if I add `addSuperInterface` or create the type name with TypeName.get
                    inter.superinterfaces.add(tname(extraInterface.fullName()));
                }

                //implement super interfaces
                for(Stype type : depends){
                    inter.addSuperinterface(ClassName.get(packageName, interfaceName(type)));
                }

                ObjectSet<String> signatures = new ObjectSet<>();

                //add utility methods to interface
                for(Smethod method : component.methods()){
                    //skip private methods, those are for internal use.
                    if(method.isAny(Modifier.PRIVATE, Modifier.STATIC)) continue;

                    //keep track of signatures used to prevent dupes
                    signatures.add(method.e.toString());

                    inter.addMethod(MethodSpec.methodBuilder(method.name())
                        .addJavadoc(method.doc() == null ? "" : method.doc())
                        .addExceptions(method.thrownt())
                        .addTypeVariables(method.typeVariables().map(TypeVariableName::get))
                        .returns(method.ret().toString().equals("void") ? TypeName.VOID : method.retn())
                        .addParameters(method.params().map(v -> ParameterSpec.builder(v.tname(), v.name())
                            .build())).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build());
                }
                //generate interface getters and setters for all "standard" fields
                for(Svar field : component.fields().select(e -> !e.is(Modifier.STATIC) && !e.is(Modifier.PRIVATE) && !e.has(Annotations.Import.class))){
                    String cname = field.name();
                    //getter
                    if(!signatures.contains(cname + "()")){
                        inter.addMethod(MethodSpec.methodBuilder(cname).addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                            .addAnnotations(Seq.with(field.annotations()).select(a -> a.toString().contains("Null") || a.toString().contains("Deprecated")).map(AnnotationSpec::get))
                            .addJavadoc(field.doc() == null ? "" : field.doc())
                            .returns(field.tname()).build());
                    }

                    //setter
                    if(!field.is(Modifier.FINAL) && !signatures.contains(cname + "(" + field.mirror().toString() + ")") &&
                       !field.annotations().contains(f -> f.toString().equals("@mindustry.annotations.Annotations.ReadOnly"))){
                        inter.addMethod(MethodSpec.methodBuilder(cname).addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                            .addJavadoc(field.doc() == null ? "" : field.doc())
                            .addParameter(ParameterSpec.builder(field.tname(), field.name())
                                .addAnnotations(Seq.with(field.annotations())
                                    .select(a -> a.toString().contains("Null") || a.toString().contains("Deprecated")).map(AnnotationSpec::get)).build()).build());
                    }
                }
                generatedAny = true;
                write(inter, Seq.with("import mindustry.gen.*;"));
            }

            //generate base class if necessary
            //SPECIAL CASE: components with EntityDefs don't get a base class! the generated class becomes the base class itself
            if(component.annotation(Annotations.Component.class).base()){

                Seq<Stype> deps = depends.copy().add(component);
                baseClassDeps.get(component, ObjectSet::new).addAll(deps);

                //do not generate base classes when the component will generate one itself
                if(!component.has(Annotations.EntityDef.class) && !anuke){
                    TypeSpec.Builder base = TypeSpec.classBuilder(baseName(component)).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

                    //go through all the fields.
                    for(Stype type : deps){
                        //add public fields
                        for(Svar field : type.fields().select(e -> !e.is(Modifier.STATIC) && !e.is(Modifier.PRIVATE) && !e.has(Annotations.Import.class) && !e.has(Annotations.ReadOnly.class))){
                            FieldSpec.Builder builder = FieldSpec.builder(field.tname(), field.name(), Modifier.PUBLIC);

                            //keep transience
                            if(field.is(Modifier.TRANSIENT)) builder.addModifiers(Modifier.TRANSIENT);
                            //keep all annotations
                            builder.addAnnotations(field.annotations().map(AnnotationSpec::get));

                            //add initializer if it exists
                            if(varInitializers.containsKey(field.descString())){
                                builder.initializer(varInitializers.get(field.descString()));
                            }

                            base.addField(builder.build());
                        }
                    }

                    //add interfaces
                    for(Stype type : deps){
                        base.addSuperinterface(tname(packageName, interfaceName(type)));
                    }

                    //add to queue to be written later
                    baseClasses.add(base);
                }
            }

            //LOGGING

            if(inter != null){
                Log.debug("&gGenerating interface for " + component.name());

                for(TypeName tn : inter.superinterfaces){
                    Log.debug("&g> &lbimplements @", simpleName(tn.toString()));
                }

                //log methods generated
                for(MethodSpec spec : inter.methodSpecs){
                    Log.debug("&g> > &c@ @(@)", simpleName(spec.returnType.toString()), spec.name, Seq.with(spec.parameters).toString(", ", p -> simpleName(p.type.toString()) + " " + p.name));
                }

            }
            Log.debug("");
        }
        return generatedAny;
    }


    void skipDeprecated(TypeSpec.Builder builder){
        //deprecations are irrelevant in generated code
        builder.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "\"deprecation\"").build());
    }

    private void secondRound() throws Exception{
        //round 2: get component classes and generate interfaces for
        //parse groups
        //this needs to be done before the entity interfaces are generated, as the entity classes need to know which groups to add themselves to
        for(Selement<?> group : allGroups){
            try{
                Annotations.GroupDef an = group.annotation(Annotations.GroupDef.class);
                Seq<Stype> rawTypes = new Seq<>();
                Seq<Stype> types = rawTypes.addAll(types(an, Annotations.GroupDef::value)).map(stype -> {
//                    Log.info("type: @",stype);
//                    rawTypes.add(stype);
                    Stype result = interfaceToComp(stype);
                    if(result == null)
                        throw new IllegalArgumentException("Interface " + stype + " does not have an associated component!");
                    return result;
                });

                //representative component type
                Stype repr = types.first();
                String groupType = repr.annotation(Annotations.Component.class).base() ? baseName(repr) : interfaceName(repr);

                String name = group.name().startsWith("g") ? group.name().substring(1) : group.name();

                boolean collides = an.collide();
                boolean modGroup = true;
                Element element = group.up();
                while(element != null && !(element instanceof TypeElement)) element = element.getEnclosingElement();

                if(element != null){
                    String string = element.toString();
                    modGroup = string.startsWith(rootPackageName) && !string.equals("mmc.entities.GroupDefs");
                }
                ClassName baseType = (rawTypes.first().toString().contains("<any?>")) ? ClassName.get(packageName, groupType) : ClassName.get(rawTypes.first().cname().packageName(), groupType);
                GroupDefinition groupDefinition = new GroupDefinition(name,
                    baseType, types, an.spatial(), an.mapping(), collides, modGroup);
                groupDefs.add(groupDefinition);

                if(modGroup){
                    TypeSpec.Builder accessor = TypeSpec.interfaceBuilder(groupDefinition.indexableEntityClass)
                        .addModifiers(Modifier.PUBLIC);
                    accessor.addMethod(MethodSpec.methodBuilder(groupDefinition.indexableEntityMethod).addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC).addParameter(int.class, "index").returns(void.class).build());
                    write(accessor);
                }

            }catch(RuntimeException e){
                Log.err("@", e);
            }
        }

        ObjectMap<String, Selement> usedNames = new ObjectMap<>();
        ObjectMap<Selement, ObjectSet<String>> extraNames = new ObjectMap<>();
        //look at each definition
        if(hasAnukeComps) for(Selement<?> type : allDefs){
            EntityDef ann = type.annotation(EntityDef.class);
            //all component classes (not interfaces)
            Seq<Stype> components = allComponents(type);
//            System.out.println("All components: "+components);
            Seq<GroupDefinition> groups = groupDefs.select(g -> {
//                System.out.println("Group: "+g.components);
                return (!g.components.isEmpty() && !g.components.contains(s -> !components.contains(s))) || g.manualInclusions.contains(type);
            });
//            System.out.println("groups: "+groups);
            ObjectMap<String, Seq<Smethod>> methods = new ObjectMap<>();
            ObjectMap<FieldSpec, Svar> specVariables = new ObjectMap<>();
            ObjectSet<String> usedFields = new ObjectSet<>();

            //make sure there's less than 2 base classes
            Seq<Stype> baseClasses = components.select(s -> s.annotation(Annotations.Component.class).base());
            if(baseClasses.size > 2){
                err("No entity may have more than 2 base classes. Base classes: " + baseClasses, type);
            }

            //get base class type name for extension
            Stype baseClassType = baseClasses.any() ? baseClasses.first() : null;
            @Nullable TypeName baseClass = baseClasses.any() ? tname(packageName + "." + baseName(baseClassType)) : null;
            @Nullable TypeSpec.Builder baseClassBuilder = baseClassType == null ? null : this.baseClasses.find(b -> Reflect.<String>get(b, "name").equals(baseName(baseClassType)));
            boolean addIndexToBase = baseClassBuilder != null && baseClassIndexers.add(baseClassBuilder);
            //whether the main class is the base itself
            boolean typeIsBase = baseClassType != null && type.has(Annotations.Component.class) && type.annotation(Annotations.Component.class).base();

            if(type.isType() && (!type.name().endsWith("Def") && !type.name().endsWith("Comp"))){
                err("All entity def names must end with 'Def'/'Comp'", type.e);
            }

            String name = type.isType() ?
                type.name().replace("Def", "").replace("Comp", "") :
                createName(type);

            //check for type name conflicts
            if(!typeIsBase && baseClass != null && name.equals(baseName(baseClassType))){
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

            Builder builder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);
            //add serialize() boolean
            if(!components.contains(t -> t.methods().contains(m -> {
                return m.name().equals("serialize") && m.params().isEmpty() && m.has(ReplaceInternalImpl.class);
            }))){
                builder.addMethod(MethodSpec.methodBuilder("serialize").addModifiers(Modifier.PUBLIC).returns(boolean.class).addStatement("return " + ann.serialize()).build());
            }

            //all SyncField fields
            Seq<Svar> syncedFields = new Seq<>();
            Seq<Svar> allFields = new Seq<>();
            Seq<FieldSpec> allFieldSpecs = new Seq<>();

            boolean isSync = components.contains(s -> s.name().contains("Sync"));

            //add all components
            for(Stype comp : components){
                //whether this component's fields are defined in the base class
                boolean isShadowed = baseClass != null && !typeIsBase && baseClassDeps.get(baseClassType).contains(comp);

                //write fields to the class; ignoring transient/imported ones
                Seq<Svar> fields = comp.fields().select(f -> !f.has(Annotations.Import.class));
                for(Svar f : fields){
                    if(!usedFields.add(f.name())){
                        err("Field '" + f.name() + "' of component '" + comp.name() + "' redefines a field in entity '" + type.name() + "'");
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
                    if(varInitializers.containsKey(f.descString())){
                        fbuilder.initializer(varInitializers.get(f.descString()));
                    }

                    fbuilder.addModifiers(f.has(ReadOnly.class) || f.is(Modifier.PRIVATE) ? Modifier.PROTECTED : Modifier.PUBLIC);
                    fbuilder.addAnnotations(f.annotations().map(AnnotationSpec::get));
                    FieldSpec spec = fbuilder.build();

                    //whether this field would be added to the superclass
                    boolean isVisible = !f.is(Modifier.STATIC) && !f.is(Modifier.PRIVATE) && !f.has(Annotations.ReadOnly.class);

                    //add the field only if it isn't visible or it wasn't implemented by the base class
                    if(!isShadowed || !isVisible){
                        builder.addField(spec);
                    }

                    specVariables.put(spec, f);

                    allFieldSpecs.add(spec);
                    allFields.add(f);

                    //add extra sync fields
                    if(f.has(Annotations.SyncField.class) && isSync){
                        if(!f.tname().toString().equals("float")) err("All SyncFields must be of type float", f);

                        syncedFields.add(f);

                        //a synced field has 3 values:
                        //- target state
                        //- last state
                        //- current state (the field itself, will be written to)

                        //target
                        builder.addField(FieldSpec.builder(float.class, f.name() + ModEntityIO.targetSuf).addModifiers(Modifier.TRANSIENT, Modifier.PRIVATE).build());

                        //last
                        builder.addField(FieldSpec.builder(float.class, f.name() + ModEntityIO.lastSuf).addModifiers(Modifier.TRANSIENT, Modifier.PRIVATE).build());
                    }
                }

                //get all methods from components
                for(Smethod elem : comp.methods()){
                    methods.get(elem.toString(), Seq::new).add(elem);
                }
            }

            syncedFields.sortComparing(Selement::name);

            if(!methods.containsKey("toString()")){
                //override toString method
                builder.addMethod(MethodSpec.methodBuilder("toString")
                    .addAnnotation(Override.class)
                    .returns(String.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return $S + $L", name + "#", "id").build());
            }
            ModEntityIO io = new ModEntityIO(type.name(), builder, allFieldSpecs, serializer,
                rootDirectory.child(annotationsSettings(AnnotationSettingsEnum.revisionsPath, "annotations/src/main/resources/revisions")).child(type.name()));
            //entities with no sync comp and no serialization gen no code
            boolean hasIO = ann.genio() && (components.contains(s -> s.name().contains("Sync")) || ann.serialize());

            TypeSpec.Builder indexBuilder = baseClassBuilder == null ? builder : baseClassBuilder;

            if(baseClassBuilder == null || addIndexToBase){
                //implement indexable interfaces.
                for(GroupDefinition def : groups){
                    if(def.mod){
                        indexBuilder.addSuperinterface(tname(packageName + "." + (def.indexableEntityClass)));
                        indexBuilder.addMethod(MethodSpec.methodBuilder(def.indexableEntityMethod).addParameter(int.class, "index").addModifiers(Modifier.PUBLIC).addAnnotation(Override.class)
                            .addCode("$L = index;", def.indexableEntityField).build());
                    }
                }
            }
            //add all methods from components
            for(ObjectMap.Entry<String, Seq<Smethod>> entry : methods){
                Seq<Smethod> smethods = entry.value.copy();
                String methodFullName = entry.key;
                if(smethods.contains(m -> m.has(Annotations.Replace.class))){
                    //check replacements
                    if(smethods.count(m -> m.has(Annotations.Replace.class)) > 1){
                        err("Type " + type + " has multiple components replacing method " + methodFullName + ".");
                    }
                    Smethod base = smethods.find(m -> m.has(Annotations.Replace.class));
                    smethods.clear();
                    smethods.add(base);
                }
                if(smethods.contains(m -> m.has(SuperMethod.class))){
                    //check replacements
                    if(smethods.size > 1){
                        err("Type " + type + " has multiple components replacing method " + methodFullName + ".");
                    }
                    Smethod base = smethods.first();
                    SuperMethod annotation = base.annotation(SuperMethod.class);
                    Seq<Stype> parentParams = types(annotation, SuperMethod::params);
                    if(parentParams.isEmpty() && base.params().size > 0){
                        base.params().each(v -> parentParams.add(Stype.of(v.e.asType())));
                    }
                    String parentName = annotation.parentName() + "(" + parentParams.toString(",") + ")";
                    Seq<Smethod> parent = methods.get(parentName, (Seq<Smethod>)null);
                    if(parent == null){
//                        Log.info("methods: @",methods.keys().toSeq());
                        err("Cannot find implementation for method " + parentName);
                        continue;
                    }
                    parent = parent.copy().removeAll(m -> {
                        return m.up().toString().equals(base.up().toString());
                    });
                    if(parent.isEmpty()){
                        Log.err("Cannot find any implementation for method " + parentName);

                        MethodSpec.Builder mbuilder = MethodSpec.methodBuilder(annotation.parentName())
                            .addModifiers(base.is(Modifier.PRIVATE) ? Modifier.PRIVATE : Modifier.PUBLIC);
                        //if(isFinal || entry.value.contains(s -> s.has(Final.class))) mbuilder.addModifiers(Modifier.FINAL);
                        if(smethods.contains(s -> s.has(Annotations.CallSuper.class)))
                            mbuilder.addAnnotation(Annotations.CallSuper.class); //add callSuper here if necessary
                        if(base.is(Modifier.STATIC)) mbuilder.addModifiers(Modifier.STATIC);

                        mbuilder.addTypeVariables(base.typeVariables().map(TypeVariableName::get));
                        mbuilder.returns(base.retn());
                        mbuilder.addExceptions(base.thrownt());

                        int paramID = 0;
                        for(Stype parentParam : parentParams){
                            mbuilder.addParameter(parentParam.tname(), "var" + paramID);
                            paramID++;
                        }

                        builder.addMethod(mbuilder.build());
                        continue;
                    }

                    List<MethodSpec> methodSpecs = builder.methodSpecs;
                    int beforeSize = methodSpecs.size();
                    generateMethod(type, ann.pooled(), groups, builder, syncedFields, allFields, io, hasIO, parent, parentName);
                    if(methodSpecs.size() > beforeSize){
                        MethodSpec spec = methodSpecs.get(beforeSize);
                        MethodSpec.Builder mbuilder = spec.toBuilder().setName(base.name());
//                        mbuilder.modifiers.clear();
                        mbuilder.modifiers.remove(Modifier.PUBLIC);
                        mbuilder.modifiers.remove(Modifier.PRIVATE);
                        mbuilder.modifiers.add(0, base.is(Modifier.PRIVATE) ? Modifier.PRIVATE : Modifier.PUBLIC);
                        methodSpecs.set(beforeSize, mbuilder.build());
                    }
//                    Log.info("MethodSpecs: @",methodSpecs.toString());
                    continue;
//                    entry.value.clear();
//                    entry.value.add(base);
                }
                //check multi return
                if(smethods.count(m -> !m.isAny(Modifier.NATIVE, Modifier.ABSTRACT) && !m.isVoid()) > 1){
                    err("Type " + type + " has multiple components implementing non-void method " + methodFullName + ".");
                }

                generateMethod(type, ann.pooled(), groups, builder, syncedFields, allFields, io, hasIO, smethods, methodFullName);
            }

            //add pool reset method and implement Poolable
            if(ann.pooled()){
                builder.addSuperinterface(Pool.Poolable.class);
                //implement reset()
                MethodSpec.Builder resetBuilder = MethodSpec.methodBuilder("reset").addModifiers(Modifier.PUBLIC);
                for(FieldSpec spec : allFieldSpecs){
                    @Nullable Svar variable = specVariables.get(spec);
                    if(variable != null && variable.isAny(Modifier.STATIC, Modifier.FINAL)) continue;
                    String desc = variable.descString();

                    if(spec.type.isPrimitive()){
                        //set to primitive default
                        resetBuilder.addStatement("$L = $L", spec.name, variable != null && varInitializers.containsKey(desc) ? varInitializers.get(desc) : getDefault(spec.type.toString()));
                    }else{
                        //set to default null
                        if(!varInitializers.containsKey(desc)){
                            resetBuilder.addStatement("$L = null", spec.name);
                        } //else... TODO reset if poolable
                    }
                }

                builder.addMethod(resetBuilder.build());
            }

            //make constructor private
            builder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PROTECTED).build());

            //add create() method
            builder.addMethod(MethodSpec.methodBuilder("create").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(tname(packageName + "." + name))
                .addStatement(ann.pooled() ? "return Pools.obtain($L.class, " + name + "::new)" : "return new $L()", name).build());

            if(true/*!legacy*/){
                TypeSpec.Builder fieldBuilder = baseClassBuilder != null ? baseClassBuilder : builder;
                if(addIndexToBase || baseClassBuilder == null){
                    //add group index int variables
                    for(GroupDefinition def : groups){
                        fieldBuilder.addField(FieldSpec.builder(int.class, def.indexableEntityField, Modifier.PROTECTED, Modifier.TRANSIENT).initializer("-1").build());
                    }
                }
            }

            definitions.add(new EntityDefinition(packageName + "." + name, builder, type, typeIsBase ? null : baseClass, components, groups, allFieldSpecs));
        }
        generateGroups();

        //load map of sync IDs
//        StringMap map = new StringMap();
//        Fi idProps = rootDirectory.child("annotations/src/main/resources/classids.properties");
//        if(!idProps.exists()) idProps.writeString("");
//        PropertiesUtils.load(map, idProps.reader());
        //next ID to be used in generation
//        Integer max = map.values().toSeq().map(Integer::parseInt).max(i -> i);
//        int maxID = max == null ? 0 : max + 1;

        //assign IDs
        definitions.sort(Structs.comparing(t -> t.naming.toString()));
        /*for(EntityDefinition def : definitions){
            String name = def.naming.fullName();
            if(map.containsKey(name)){
                def.classID = map.getInt(name);
            }else{
                def.classID = maxID++;
                map.put(name, String.valueOf(def.classID));
            }
        }*/

        /*OrderedMap<String, String> res = new OrderedMap<>();
        res.putAll(map);
        res.orderedKeys().sort();*/

        //write assigned IDs
//        PropertiesUtils.store(res, idProps.writer(false), "Maps entity names to IDs. Autogenerated.");


        //build mapping class for sync IDs
        Builder idBuilder = TypeSpec.classBuilder(classPrefix() + "EntityMapping").addModifiers(Modifier.PUBLIC);

        MethodSpec.Builder idStore = MethodSpec.methodBuilder("init").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeName.get(void.class));
        ModMeta modMeta = modInfoNull();
        //store the mappings
        for(EntityDefinition def : definitions){
            //store mapping
            idStore.addStatement("mindustry.gen.EntityMapping.register($S,$L::new)", def.name.substring(def.name.lastIndexOf(".") + 1), def.name);
            /* idStore.addStatement("idMap[$L] = $L::new", def.classID, def.name);*/
            extraNames.get(def.naming).each(extra -> {
                idStore.addStatement("mindustry.gen.EntityMapping.nameMap.put($S, $L::new)", extra, def.name);
                if(modMeta != null){
                    idStore.addStatement("mindustry.gen.EntityMapping.nameMap.put($S, $L::new)", modMeta.name + "-" + extra, def.name);
                }
                if(!Strings.camelToKebab(extra).equals(extra)){
                    idStore.addStatement("mindustry.gen.EntityMapping.nameMap.put($S, $L::new)", Strings.camelToKebab(extra), def.name);
                    if(modMeta != null){
                        idStore.addStatement("mindustry.gen.EntityMapping.nameMap.put($S, $L::new)", modMeta.name + "-" + Strings.camelToKebab(extra), def.name);
                    }
                }
            });
            //return mapping
            def.builder.addMethod(MethodSpec.methodBuilder("classId").addAnnotation(Override.class)
//                            .returns(int.class).addModifiers(Modifier.PUBLIC).addStatement("return " + def.classID).build());
                .returns(int.class).addModifiers(Modifier.PUBLIC).addStatement("return " + rootPackageName + ".gen." + classPrefix() + "EntityMapping.getId(getClass())").build());
        }
        MethodSpec.Builder idGet = MethodSpec.methodBuilder("getId").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeName.get(int.class)).addParameter(TypeName.get(Class.class), "name");
        idGet.addStatement("return mindustry.gen.EntityMapping.customIdMap.findKey(name.getSimpleName(),false,-1)");

        idBuilder.addMethod(idStore.build());
        idBuilder.addMethod(idGet.build());

        write(idBuilder);
    }

    @Nullable
    void generateMethod(Selement<?> type, boolean pooled, Seq<GroupDefinition> groups, Builder builder, Seq<Svar> syncedFields, Seq<Svar> allFields, ModEntityIO io, boolean hasIO, Seq<Smethod> smethods, String methodName) throws Exception{
        MethodSpec.Builder method = generateMethod(type, pooled, groups, syncedFields, allFields, io, hasIO, smethods, methodName);
        if(method == null) return;
        builder.addMethod(method.build());
    }

    @Nullable
    MethodSpec.Builder generateMethod(Selement<?> type, boolean pooled, Seq<GroupDefinition> groups, Seq<Svar> syncedFields, Seq<Svar> allFields, ModEntityIO io, boolean hasIO, Seq<Smethod> smethods, String methodName) throws Exception{
        smethods.sort(Structs.comps(Structs.comparingFloat(m -> m.has(Annotations.MethodPriority.class) ? m.annotation(Annotations.MethodPriority.class).value() : 0), Structs.comparing(Selement::name)));
        smethods = smethods.copy();

        //representative method
        Smethod first = smethods.first();
        boolean customInternal = false;

        //skip internal impl
        boolean anyReplaceInternal = smethods.contains(m -> m.has(ReplaceInternalImpl.class));
        if(first.has(Annotations.InternalImpl.class) && !anyReplaceInternal){
            return null;
        }else if(anyReplaceInternal){
            if(smethods.count(m -> m.has(ReplaceInternalImpl.class) && !m.has(InternalImpl.class)) > 1){
                err("Type " + type + " has multiple components replacing method " + methodName + ".");
            }
            customInternal = true;
        }

        //build method using same params/returns
        MethodSpec.Builder mbuilder = MethodSpec.methodBuilder(first.name()).addModifiers(first.is(Modifier.PRIVATE) ? Modifier.PRIVATE : Modifier.PUBLIC);
        //if(isFinal || entry.value.contains(s -> s.has(Final.class))) mbuilder.addModifiers(Modifier.FINAL);
        if(smethods.contains(s -> s.has(Annotations.CallSuper.class)))
            mbuilder.addAnnotation(Annotations.CallSuper.class); //add callSuper here if necessary
        if(first.is(Modifier.STATIC)){
//            System.out.println("Static: " + first.descString());
//            System.out.println(methodBlocks.keys().toSeq());
//            System.out.println(methodBlocks.get(first.descString()));
//            System.out.println(methodBlocks.get(first.descString().replace(packageName + ".", "")));
//            methodBlocks.get(smethods.first().descString())
            mbuilder.addModifiers(Modifier.STATIC);
        }
        mbuilder.addTypeVariables(first.typeVariables().map(TypeVariableName::get));
        mbuilder.returns(first.retn());
        mbuilder.addExceptions(first.thrownt());
        for(Svar var : first.params()){

            mbuilder.addParameter(var.tname(), var.name());
        }

        //only write the block if it's a void method with several entries
        boolean writeBlock = first.ret().toString().equals("void") && smethods.size > 1;

        if((smethods.first().is(Modifier.ABSTRACT) || smethods.first().is(Modifier.NATIVE)) && smethods.size == 1 && !smethods.first().has(Annotations.InternalImpl.class)){
            err(smethods.first().up().getSimpleName() + "#" + smethods.first() + " is an abstract method and must be implemented in some component", type);
        }

        //SPECIAL CASE: inject group add/remove code
        if(first.name().equals("add") || first.name().equals("remove")){
            mbuilder.addStatement("if(added == $L) return", first.name().equals("add"));

            for(GroupDefinition def : groups){

                //remove/add from each group, assume imported
                if(def.mod){
//                    mbuilder.addStatement("$L.gen.$LGroups.$L.$L(this)", rootPackageName, classPrefix(), def.name, first.name());

                    if(first.name().equals("add")){
                        //remove/add from each group, assume imported
                        mbuilder.addStatement("$L = $L.gen.$LGroups.$L.addIndex(this)", def.indexableEntityField, rootPackageName, classPrefix(), def.name);
                    }else{
                        //remove/add from each group, assume imported
                        mbuilder.addStatement("$L.gen.$LGroups.$L.removeIndex(this, $L);", rootPackageName, classPrefix(), def.name, def.indexableEntityField);

                        mbuilder.addStatement("$L = -1", def.indexableEntityField);
                    }
                }else{
                    if(first.name().equals("add")){
                        //remove/add from each group, assume imported
                        mbuilder.addStatement("$L = Groups.$L.addIndex(this)", def.indexableEntityField, def.name);
                    }else{
                        //remove/add from each group, assume imported
                        mbuilder.addStatement("Groups.$L.removeIndex(this, $L);", def.name, def.indexableEntityField);

                        mbuilder.addStatement("$L = -1", def.indexableEntityField);
                    }
                }
            }
        }

        if(hasIO && !customInternal){
            //SPECIAL CASE: I/O code
            //note that serialization is generated even for non-serializing entities for manual usage
            if((first.name().equals("read") || first.name().equals("write"))){
                io.write(mbuilder, first.name().equals("write"));
            }

            //SPECIAL CASE: sync I/O code
            if((first.name().equals("readSync") || first.name().equals("writeSync"))){
                io.writeSync(mbuilder, first.name().equals("writeSync"), allFields);
            }

            //SPECIAL CASE: sync I/O code for writing to/from a manual buffer
            if((first.name().equals("readSyncManual") || first.name().equals("writeSyncManual"))){
                io.writeSyncManual(mbuilder, first.name().equals("writeSyncManual"), syncedFields);
            }

            //SPECIAL CASE: interpolate method implementation
            if(first.name().equals("interpolate")){
                io.writeInterpolate(mbuilder, syncedFields);
            }

            //SPECIAL CASE: method to snap to target position after being read for the first time
            if(first.name().equals("snapSync")){
                mbuilder.addStatement("updateSpacing = 16");
                mbuilder.addStatement("lastUpdated = $T.millis()", Time.class);
                for(Svar field : syncedFields){
                    //reset last+current state to target position
                    mbuilder.addStatement("$L = $L", field.name() + ModEntityIO.lastSuf, field.name() + ModEntityIO.targetSuf);
                    mbuilder.addStatement("$L = $L", field.name(), field.name() + ModEntityIO.targetSuf);
                }
            }

            //SPECIAL CASE: method to snap to current position so interpolation doesn't go wild
            if(first.name().equals("snapInterpolation")){
                mbuilder.addStatement("updateSpacing = 16");
                mbuilder.addStatement("lastUpdated = $T.millis()", Time.class);
                for(Svar field : syncedFields){
                    //reset last+current state to target position
                    mbuilder.addStatement("$L = $L", field.name() + ModEntityIO.lastSuf, field.name());
                    mbuilder.addStatement("$L = $L", field.name() + ModEntityIO.targetSuf, field.name());
                }
            }
        }

        ObjectSet<String> ignoreClasses = new ObjectSet<>();
        smethods.each(m -> {
            if(m.has(IgnoreImplementation.class)){
                if(m.has(UseOnlyImplementation.class)){
                    err("One method can not use IgnoreImplementation and UseOnlyImplementation annotation", m);
                }
                types(m.annotation(IgnoreImplementation.class), IgnoreImplementation::value).map(Stype::fullName).each(ignoreClasses::add);
            }
        });
        if(smethods.contains(m -> m.has(UseOnlyImplementation.class))){

            if(smethods.count(m -> m.has(UseOnlyImplementation.class)) > 1){
                err("Type " + type + " has multiple components replacing method " + methodName + ".");
            }
            Smethod root = smethods.find(m -> m.has(UseOnlyImplementation.class));
            final Seq<String> interfaces = types(root.annotation(UseOnlyImplementation.class), UseOnlyImplementation::value).map(Stype::fullName);

            smethods.retainAll(m -> m == root || interfaces.contains(interfaceName -> {
                String mirrorName = interfaceToComp(interfaceName);
                String methodTypeName = m.up().toString().replace("mmc.entities.compByAnuke", "mindustry.gen");
                return methodTypeName.equals(mirrorName);
            }));
//                    root.e.getAnnotationMirrors()
//                    entry.value.filter(m->m==root|| Structs.contains(classes,c->m.up().toString().contains(c)));
        }else{
            Seq<String> ignoreSeq = ignoreClasses.iterator().toSeq();
            smethods.removeAll(m -> ignoreSeq.contains(interfaceName -> {
                String mirrorName = interfaceToComp(interfaceName);
                String methodTypeName = m.up().toString().replace("mmc.entities.compByAnuke", "mindustry.gen");
                return methodTypeName.equals(mirrorName);
            }));
//                    entry.value.filter(m->m.up())
        }
        for(Smethod elem : smethods){
            String descStr = elem.descString();

            if(elem.is(Modifier.ABSTRACT) || elem.is(Modifier.NATIVE) || !(methodBlocks.containsKey(descStr) || methodBlocks.containsKey(descStr.replace(packageName + ".", ""))))
                continue;

            //get all statements in the method, copy them over
            String str = methodBlocks.get(descStr, methodBlocks.get(descStr.replace(packageName + ".", "")));
            //name for code blocks in the methods
            String blockName = elem.up().getSimpleName().toString().toLowerCase().replace("comp", "");

            //skip empty blocks
            if(str.replace("{", "").replace("\n", "").replace("}", "").replace("\t", "").replace(" ", "").isEmpty()){
                continue;
            }

            //wrap scope to prevent variable leakage
            if(writeBlock){
                //replace return; with block break
                if(!elem.has(ModAnnotations.GlobalReturn.class)){
                    str = str.replace("return;", "break " + blockName + ";");
                }
                mbuilder.addCode(blockName + ": {\n");
            }

            //trim block
            str = str.substring(2, str.length() - 1);

            //make sure to remove braces here
            mbuilder.addCode(str);

            //end scope
            if(writeBlock) mbuilder.addCode("}\n");
        }

        //add free code to remove methods - always at the end
        //this only gets called next frame.
        if(first.name().equals("remove") && pooled){
            mbuilder.addStatement("mindustry.gen.Groups.queueFree(($T)this)", Pool.Poolable.class);
        }
        return mbuilder;

    }

    private void generateGroups() throws Exception{
        Seq<GroupDefinition> groupDefs = this.groupDefs.select(GroupDefinition::mod);
        if(groupDefs.isEmpty()) return;

        TypeSpec.Builder groupsBuilder = TypeSpec.classBuilder(classPrefix() + "Groups").addModifiers(Modifier.PUBLIC);

        MethodSpec.Builder groupInit = MethodSpec.methodBuilder("init").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        for(GroupDefinition group : groupDefs){
            //class names for interface/group
            ClassName itype = group.baseType;
            ClassName groupc = ClassName.bestGuess("mindustry.entities.EntityGroup");

            //add field...
            groupsBuilder.addField(ParameterizedTypeName.get(
                ClassName.bestGuess("mindustry.entities.EntityGroup"), itype), group.name, Modifier.PUBLIC, Modifier.STATIC);

//            groupInit.addStatement("$L = new $T<>($L.class, $L, $L)", group.name, groupc, itype, group.spatial, group.mapping);
            groupInit.addStatement("$L = new $T<>($L.class, $L, $L, (e, pos) -> { if(e instanceof $L.$L ix) ix.$L(pos); })",
                group.name, groupc, itype, group.spatial, group.mapping, packageName, group.indexableEntityClass, group.indexableEntityMethod);
        }

        //write the groups
        groupsBuilder.addMethod(groupInit.build());

        MethodSpec.Builder groupClear = MethodSpec.methodBuilder("clear").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        for(GroupDefinition group : groupDefs){
            groupClear.addStatement("$L.clear()", group.name);
        }

        //write clear
        groupsBuilder.addMethod(groupClear.build());

        //add method for pool storage
        groupsBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(Seq.class, Poolable.class), "freeQueue", Modifier.PRIVATE, Modifier.STATIC).initializer("new Seq<>()").build());

        //method for freeing things
        MethodSpec.Builder groupFreeQueue = MethodSpec.methodBuilder("queueFree")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(Poolable.class, "obj")
            .addStatement("freeQueue.add(obj)");

        groupsBuilder.addMethod(groupFreeQueue.build());

        //add method for resizing all necessary groups
        MethodSpec.Builder groupResize = MethodSpec.methodBuilder("resize")
            .addParameter(TypeName.FLOAT, "x").addParameter(TypeName.FLOAT, "y").addParameter(TypeName.FLOAT, "w").addParameter(TypeName.FLOAT, "h")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        MethodSpec.Builder groupUpdate = MethodSpec.methodBuilder("update")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        //free everything pooled at the start of each updaet
        groupUpdate
            .addStatement("for($T p : freeQueue) $T.free(p)", Poolable.class, Pools.class)
            .addStatement("freeQueue.clear()");

        //method resize
        for(GroupDefinition group : groupDefs){
            if(group.spatial){
                groupResize.addStatement("$L.resize(x, y, w, h)", group.name);
                groupUpdate.addStatement("$L.updatePhysics()", group.name);
            }
        }

//        groupUpdate.addStatement("all.update()");

        for(GroupDefinition group : groupDefs){
            if(group.collides){
                groupUpdate.addStatement("$L.collide()", group.name);
            }
        }

        groupsBuilder.addMethod(groupResize.build());
        groupsBuilder.addMethod(groupUpdate.build());

        write(groupsBuilder, allGroups.map(s -> new Stype((TypeElement)s.up())).asSet().toSeq().flatMap(s -> getImports(s.e)).addAll("import mindustry.gen.*;"));
        return;
    }

    private void thirdRound() throws Exception{
        //round 3: generate actual classes and implement interfaces

        //write base classes
        for(TypeSpec.Builder b : baseClasses){
            write(b, imports.toSeq());
        }

        //implement each definition
        for(EntityDefinition def : definitions){

            ObjectSet<String> methodNames = def.components.flatMap(type -> type.methods().map(Smethod::simpleString)).<String>as().asSet();

            //add base class extension if it exists
            if(def.extend != null){
                def.builder.superclass(def.extend);
            }

            //get interface for each component
            for(Stype comp : def.components){

                //implement the interface
                Stype inter = allInterfaces.find(i -> i.name().equals(interfaceName(comp)));
                if(inter == null){
                    err("Failed to generate interface for", comp);
                    return;
                }

                def.builder.addSuperinterface(inter.tname());

                //generate getter/setter for each method
//                String substring = def.name.substring(def.name.lastIndexOf(".") + 1);

                for(Smethod method : inter.methods()){
//                    Log.info("method5: @--@",Seq.withArrays(def.builder.methodSpecs.stream().map(f->f.name).toArray()).toString(", "),method);
                    String var = method.name();
                    FieldSpec field = Seq.with(def.fieldSpecs).find(f -> f.name.equals(var));
                    //make sure it's a real variable AND that the component doesn't already implement it somewhere with custom logic
                    if(field == null || methodNames.contains(method.simpleString())) continue;

                    //getter
                    if(!method.isVoid()){
                        def.builder.addMethod(MethodSpec.overriding(method.e).addStatement("return " + var).build());
                    }

                    //setter
                    if(method.isVoid() && !Seq.with(field.annotations).contains(f -> f.type.toString().equals("@mindustry.annotations.Annotations.ReadOnly"))){
//                        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.name()).addStatement("this." + var + " = " + var).addParameter(field.type, var);
//                       if(method) builder.addModifiers()
//                        def.builder.addMethod(builder.build());
                        MethodSpec.Builder statement = MethodSpec.overriding(method.e).addStatement("this." + var + " = " + var);
                        statement.parameters.clear();
                        statement.addParameter(field.type, var);
                        def.builder.addMethod(statement.build());
//                        def.builder.addMethod(MethodSpec.overriding(method.e).addStatement("this." + var + " = " + var).build());
                    }
                }
            }

            write(def.builder, imports.toSeq());
        }

        if(false){
            //store nulls
            TypeSpec.Builder nullsBuilder = TypeSpec.classBuilder(classPrefix() + "Nulls").addModifiers(Modifier.PUBLIC).addModifiers(Modifier.FINAL);
            ObjectSet<String> nullList = ObjectSet.with("unit", "blockUnit");
            //create mock types of all components
            for(Stype interf : allInterfaces){
                //indirect interfaces to implement methods for
                Seq<Stype> dependencies = interf.allInterfaces().add(interf);
                Seq<Smethod> methods = dependencies.flatMap(Stype::methods);
                methods.sortComparing(Object::toString);

                //optionally add superclass
                Stype superclass = dependencies.map(this::interfaceToComp).find(s -> s != null && s.annotation(Annotations.Component.class).base());
                //use the base type when the interface being emulated has a base
                TypeName type = superclass != null && interfaceToComp(interf).annotation(Annotations.Component.class).base() ? tname(baseName(superclass)) : interf.tname();

                //used method signatures
                ObjectSet<String> signatures = new ObjectSet<>();

                //create null builder
                String baseName = interf.name().substring(0, interf.name().length() - 1);

                //prevent Nulls bloat
                if(!nullList.contains(Strings.camelize(baseName))){
                    continue;
                }

                String className = "Null" + baseName;
                TypeSpec.Builder nullBuilder = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.FINAL);

                nullBuilder.addSuperinterface(interf.tname());
                if(superclass != null) nullBuilder.superclass(tname(baseName(superclass)));

                for(Smethod method : methods){
                    String signature = method.toString();
                    if(!signatures.add(signature)) continue;

                    Stype compType = interfaceToComp(method.type());
                    MethodSpec.Builder builder = MethodSpec.overriding(method.e).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
                    int index = 0;
                    for(ParameterSpec spec : builder.parameters){
                        Reflect.set(spec, "name", "arg" + index++);
                    }
                    builder.addAnnotation(Annotations.OverrideCallSuper.class); //just in case

                    if(!method.isVoid()){
                        String methodName = method.name();
                        switch(methodName){
                            case "isNull":
                                builder.addStatement("return true");
                                break;
                            case "id":
                                builder.addStatement("return -1");
                                break;
                            case "toString":
                                builder.addStatement("return $S", className);
                                break;
                            default:
                                Svar variable = compType == null || method.params().size > 0 ? null : compType.fields().find(v -> v.name().equals(methodName));
                                String desc = variable == null ? null : variable.descString();
                                if(variable == null || !varInitializers.containsKey(desc)){
                                    builder.addStatement("return " + getDefault(method.ret().toString()));
                                }else{
                                    String init = varInitializers.get(desc);
                                    builder.addStatement("return " + (init.equals("{}") ? "new " + variable.mirror().toString() : "") + init);
                                }
                        }
                    }
                    nullBuilder.addMethod(builder.build());
                }

                nullsBuilder.addField(FieldSpec.builder(type, Strings.camelize(baseName)).initializer("new " + className + "()").addModifiers(Modifier.FINAL, Modifier.STATIC, Modifier.PUBLIC).build());

                write(nullBuilder);
            }

            write(nullsBuilder);
        }
    }

    Seq<String> getImports(Element elem){
//        System.out.println("elem: "+elem.asType().toString());
        return Seq.with(trees.getPath(elem).getCompilationUnit().getImports()).map(Object::toString);
    }

    /**
     * @return interface for a component type
     */
    String interfaceName(Stype comp){
        String suffix = "Comp";
        if(!comp.name().endsWith(suffix)) err("All components must have names that end with 'Comp'", comp.e);

        //example: BlockComp -> IBlock
        return comp.name().substring(0, comp.name().length() - suffix.length()) + "c";
    }

    /**
     * @return interface for a component type
     */
    @SuppressWarnings("unused")
    String interfaceName(String name){
        String suffix = "Comp";
        if(!name.endsWith(suffix)) err("All components must have names that end with 'Comp': " + name);

        //example: BlockComp -> IBlock
        return name.substring(0, name.length() - suffix.length()) + "c";
    }

    /**
     * @return base class name for a component type
     */
    String baseName(Stype comp){
        String suffix = "Comp";
        if(!comp.name().endsWith(suffix)) err("All components must have names that end with 'Comp'", comp.e);

        return comp.name().substring(0, comp.name().length() - suffix.length());
    }

    @Nullable
    Stype interfaceToComp(Stype type){
//        print("interfaceToComp to @",type.fullName());
        //example: IBlock -> BlockComp
        String name = type.name().substring(0, type.name().length() - 1) + "Comp";
//        System.out.println(componentNames.keys().toSeq());
        return componentNames.get(name);
    }

    @Nullable
    String interfaceToComp(String type){
//        print("interfaceToComp to @",type.fullName());
        //example: IBlock -> BlockComp
        return type.substring(0, type.length() - 1) + "Comp";
    }

    /**
     * @return all components that a entity def has
     */
    Seq<Stype> allComponents(Selement<?> type){
        if(!defComponents.containsKey(type)){
            //get base defs
            Seq<Stype> interfaces = type.has(MindustryEntityDef.class) ? types(type.annotation(MindustryEntityDef.class), MindustryEntityDef::value) : types(type.annotation(Annotations.EntityDef.class), Annotations.EntityDef::value);
            Seq<Stype> components = new Seq<>();
            for(Stype i : interfaces){
                Stype comp = interfaceToComp(i);
                if(comp != null){
                    components.add(comp);
                }else{
                    throw new IllegalArgumentException("Type '" + i + "' is not a component interface!");
                }
            }

            ObjectSet<Stype> out = new ObjectSet<>();
            for(Stype comp : components){
                //get dependencies for each def, add them
                out.add(comp);
                out.addAll(getDependencies(comp));
            }

            defComponents.put(type, out.toSeq());
        }

        return defComponents.get(type);
    }

    Seq<Stype> getDependencies(Stype component){
        if(!componentDependencies.containsKey(component)){
            ObjectSet<Stype> out = new ObjectSet<>();
            //add base component interfaces
            out.addAll(component.interfaces().select(this::isCompInterface).map(this::interfaceToComp));
            //remove self interface
            out.remove(component);

            //out now contains the base dependencies; finish constructing the tree
            ObjectSet<Stype> result = new ObjectSet<>();
            for(Stype type : out){
                result.add(type);
                result.addAll(getDependencies(type));
            }

            if(component.annotation(Annotations.BaseComponent.class) == null){
                result.addAll(baseComponents);
            }

            //remove it again just in case
            out.remove(component);
            componentDependencies.put(component, result.toSeq());
        }

        return componentDependencies.get(component);
    }

    boolean isCompInterface(Stype type){
        return interfaceToComp(type) != null;
    }

    String createName(Selement<?> elem){
        Seq<Stype> comps = types(elem.annotation(Annotations.EntityDef.class), Annotations.EntityDef::value).map(this::interfaceToComp);
        comps.sortComparing(Selement::name);
        return comps.toString("", s -> s.name().replace("Comp", ""));
    }

    <T extends Annotation> Seq<Stype> types(T t, Cons<T> consumer){
        try{
            consumer.get(t);
        }catch(MirroredTypesException e){
            return Seq.with(e.getTypeMirrors()).map(Stype::of);
        }
        throw new IllegalArgumentException("Missing types.");
    }

    class GroupDefinition{
        //region zelaux
        public final String indexableEntityClass, indexableEntityMethod, indexableEntityField;
        final String name;
        final ClassName baseType;
        final Seq<Stype> components;
        final boolean spatial, mapping, collides, mod;
        final ObjectSet<Selement> manualInclusions = new ObjectSet<>();
        //endregion

        public GroupDefinition(String name, ClassName baseType, Seq<Stype> components, boolean spatial, boolean mapping, boolean collides, boolean mod){
            this.name = name;
            this.baseType = baseType;
            this.components = components;
            this.spatial = spatial;
            this.mapping = mapping;
            this.collides = collides;
            this.mod = mod;
            if(mod){
                indexableEntityClass = classPrefix() + "IndexableEntity___" + name;
                indexableEntityMethod = "set" + classPrefix() + "Index___" + name;
                indexableEntityField = "index" + classPrefix() + "__" + name;
            }else{
                indexableEntityClass = "IndexableEntity__" + name;
                indexableEntityMethod = "setIndex__" + name;
                indexableEntityField = "index__" + name;
            }
        }

        @SuppressWarnings("unused")
        public GroupDefinition(String name, ClassName bestType, Seq<Stype> components, boolean spatial, boolean mapping, boolean collides){
            this(name, bestType, components, spatial, mapping, collides, false);
        }

        public boolean mod(){
            return mod;
        }

        @Override
        public String toString(){
            return name;
        }
    }

    class EntityDefinition{
        final Seq<GroupDefinition> groups;
        final Seq<Stype> components;
        final Seq<FieldSpec> fieldSpecs;
        final TypeSpec.Builder builder;
        @SuppressWarnings("rawtypes")
        final Selement naming;
        final String name;
        final @Nullable
        TypeName extend;
//        int classID;

        public EntityDefinition(String name, TypeSpec.Builder builder, @SuppressWarnings("rawtypes") Selement naming, TypeName extend, Seq<Stype> components, Seq<GroupDefinition> groups, Seq<FieldSpec> fieldSpec){
            this.builder = builder;
            this.name = name;
            this.naming = naming;
            this.groups = groups;
            this.components = components;
            this.extend = extend;
            this.fieldSpecs = fieldSpec;
        }

        @Override
        public String toString(){
            return "Definition{" +
                   "groups=" + groups +
                   "components=" + components +
                   ", base=" + naming +
                   '}';
        }
    }
}
