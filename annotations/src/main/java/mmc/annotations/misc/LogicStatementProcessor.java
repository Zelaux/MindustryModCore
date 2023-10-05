package mmc.annotations.misc;

import arc.func.Prov;
import arc.struct.Seq;
import com.squareup.javapoet.*;
import mindustry.annotations.util.Stype;
import mindustry.annotations.util.Svar;
import mindustry.annotations.Annotations;
import mmc.annotations.*;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;

import static mmc.annotations.ModBaseProcessor.tname;

@SupportedAnnotationTypes(mindustry.annotations.Annotations.RegisterStatement.class)
public class LogicStatementProcessor extends ModBaseProcessor{

    @Override
    public void process(RoundEnvironment env) throws Exception{
        TypeSpec.Builder type = TypeSpec.classBuilder(classPrefix() + "LogicIO")
            .addModifiers(Modifier.PUBLIC);
        MethodSpec.Builder initBlock = MethodSpec.methodBuilder("init").addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        MethodSpec.Builder writer = MethodSpec.methodBuilder("write")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(Object.class, "obj")
            .addParameter(StringBuilder.class, "out");

//        MethodSpec.Builder reader = MethodSpec.methodBuilder("read")
//                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                .returns(tname("mindustry.logic.LStatement"))
//                .addParameter(String[].class, "tokens")
//                .addParameter(int.class, "length");

        Seq<Stype> types = types(Annotations.RegisterStatement.class);

        type.addField(FieldSpec.builder(
                ParameterizedTypeName.get(
                    ClassName.get(Seq.class),
                    ParameterizedTypeName.get(ClassName.get(Prov.class),
                        tname("mindustry.logic.LStatement"))), "allModStatements", Modifier.PUBLIC, Modifier.STATIC)
            .initializer("Seq.with(" + types.toString(", ", t -> t.toString() + "::new") + ")").build());
        boolean beganWrite = false, beganRead = false;
        initBlock.addStatement("mindustry.gen.LogicIO.allStatements.addAll(allModStatements)");
        initBlock.addStatement("arc.struct.ObjectMap<String, arc.func.Func<String[], LStatement>> customParsers=mindustry.logic.LAssembler.customParsers");
        for(Stype c : types){
            String name = c.annotation(Annotations.RegisterStatement.class).value();

            if(beganWrite){
                writer.nextControlFlow("else if(obj.getClass() == $T.class)", c.mirror());
            }else{
                writer.beginControlFlow("if(obj.getClass() == $T.class)", c.mirror());
                beganWrite = true;
            }

            //write the name & individual fields
            writer.addStatement("out.append($S)", name);

            Seq<Svar> fields = c.fields();
            fields.addAll(c.superclass().fields());

            initBlock.beginControlFlow("customParsers.put($S,tokens->", name);
            initBlock.addStatement("$T result = new $T()", c.mirror(), c.mirror());
            initBlock.addStatement("int length = tokens.length", c.mirror(), c.mirror());

            int index = 0;

            for(Svar field : fields){
                if(field.isAny(Modifier.TRANSIENT, Modifier.STATIC)) continue;

                writer.addStatement("out.append(\" \")");
                writer.addStatement("out.append((($T)obj).$L$L)", c.mirror(), field.name(),
                    Seq.with(typeu.directSupertypes(field.mirror())).contains(t -> t.toString().contains("java.lang.Enum")) ? ".name()" :
                        "");

                //reading primitives, strings and enums is supported; nothing else is
                initBlock.addStatement("if(length > $L) result.$L = $L(tokens[$L])",
                    index + 1,
                    field.name(),
                    field.mirror().toString().equals("java.lang.String") ?
                        "" : (field.tname().isPrimitive() ? field.tname().box().toString() :
                        field.mirror().toString()) + ".valueOf", //if it's not a string, it must have a valueOf method
                    index + 1
                );

                index++;
            }

            initBlock.addStatement("result.afterRead()");
            initBlock.addStatement("return result");
            initBlock.endControlFlow(")");
        }

//        reader.endControlFlow();
        writer.endControlFlow();

//        reader.addStatement("return null");

//        type.addStaticBlock(initBlock.build());
        type.addMethod(initBlock.build());
        type.addMethod(writer.build());
//        type.addMethod(reader.build());

        write(type);
    }
}
