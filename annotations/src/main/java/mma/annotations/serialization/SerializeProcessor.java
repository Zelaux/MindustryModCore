package mma.annotations.serialization;

import arc.files.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.pooling.*;
import com.squareup.javapoet.*;
import com.squareup.javapoet.TypeSpec.*;
import mindustry.annotations.util.*;
import mindustry.annotations.util.TypeIOResolver.*;
import mma.annotations.*;
import mma.annotations.SupportedAnnotationTypes;
import mma.annotations.ModAnnotations.*;
import mma.annotations.remote.*;
import mma.annotations.serialization.ObjectIO.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;

@SupportedAnnotationTypes(Serialize.class)
public class SerializeProcessor extends ModBaseProcessor{
    ClassSerializer resolve;

    @Override
    public void process(RoundEnvironment env) throws Exception{
        Seq<Stype> types = types(Serialize.class);
        resolve = ModTypeIOResolver.resolve(this);
        ObjectMap<String, Seq<Stype>> map = new ObjectMap<>();
        for(Stype type : types){
            String prefix = type.annotation(Serialize.class).prefix();
            map.get(prefix.equals("NIL") ? classPrefix() : prefix, Seq::new).add(type);
        }
        for(Entry<String, Seq<Stype>> entry : map){
            generateSerializer(entry.key, entry.value);
        }
//        write(compilationUnit);
    }

    Seq<String> getImports(Stype type){
//        System.out.println("elem: "+elem.asType().toString());
        return Seq.with(trees.getPath(type.e).getCompilationUnit().getImports()).map(Object::toString);
    }

    public void generateSerializer(String serializerPrefix, Seq<Stype> types) throws Exception{
        ObjectMap<String, Stype> typeMap = types.asMap(Stype::fullName);

        ObjectSet<String> imports = new ObjectSet<>();
        Builder builder = TypeSpec.classBuilder(serializerPrefix + "Serializer").addModifiers(Modifier.PUBLIC, Modifier.FINAL);
//        CompilationUnit compilationUnit = new CompilationUnit();

//        ClassOrInterfaceDeclaration declaration = compilationUnit.addClass("Serializer", Keyword.PUBLIC, Keyword.FINAL);

        for(Entry<String, Stype> entry : typeMap){
            Stype type = entry.value;
            imports.addAll(getImports(type));
            resolve.writers.get(type.fullName(), () -> serializerPrefix + "Serializer." + writeMethod(type));
            resolve.readers.get(type.fullName(), () -> serializerPrefix + "Serializer." + readMethod(type));
        }
        ClassName byteWrites = ClassName.get("mma.io", "ByteWrites");
        ClassName byteReads = ClassName.get("mma.io", "ByteReads");
        builder.addField(FieldSpec.builder(Pool.class,"writesPool",Modifier.STATIC,Modifier.FINAL)
        .initializer(CodeBlock.of(
        "new $L<$T>(){\n" +
        "    @Override\n" +
        "    protected $T newObject(){\n" +
        "        return new $T();\n" +
        "    }\n" +
        "}",Pool.class.getCanonicalName(),byteWrites,byteWrites,byteWrites
        ))
        .build());
        builder.addField(FieldSpec.builder(Pool.class,"readsPool",Modifier.STATIC,Modifier.FINAL)
        .initializer(CodeBlock.of(
        "new $L<$T>(){\n" +
        "    @Override\n" +
        "    protected $T newObject(){\n" +
        "        return new $T();\n" +
        "    }\n" +
        "}",Pool.class.getCanonicalName(),byteReads,byteReads,byteReads
        ))
        .build());
        for(Entry<String, Stype> entry : typeMap){
            Stype type = entry.value;

            Fi directory = rootDirectory.child(annotationsSettings(AnnotationSettingsEnum.revisionsPath, "annotations/src/main/resources/revisions")).child(type.name());
            Seq<FieldObject> fields = type.fields().map(var -> {
                return new FieldObject(var.name(), type.name(), var.tname().toString(), var.e.getModifiers().toArray(new Modifier[0]));
            });
            ObjectIO io = new ObjectIO(type.fullName(), type.cname(), fields, resolve, directory);

//            ClassName className = type.cname();

            MethodSpec.Builder writeMethod = MethodSpec.methodBuilder(writeMethod(type))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(Writes.class, "write");
            io.write(writeMethod, true);

            MethodSpec.Builder readMethod = MethodSpec.methodBuilder(readMethod(type))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(Reads.class, "read");
            io.write(readMethod, false);

            MethodSpec.Builder toBytesMethod = MethodSpec.methodBuilder(toBytesMethod(type))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(type.tname(), "rootObject")
            .returns(byte[].class);
            toBytesMethod.addStatement(CodeBlock.of("$T obtain = ($T)writesPool.obtain();\n" +
            "obtain.reset();\n" +
            "$L(obtain, rootObject);\n" +
            "byte[] bytes = obtain.getBytes();\n" +
            "obtain.reset();\n" +
            "writesPool.free(obtain);\n" +
            "return bytes",byteWrites,byteWrites,writeMethod(type)));

            MethodSpec.Builder fromBytesMethod = MethodSpec.methodBuilder(fromBytesMethod(type))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(byte[].class, "bytes")
            .returns(type.tname());
            fromBytesMethod.addStatement(CodeBlock.of("$T obtain = ($T)readsPool.obtain();\n" +
            "obtain.setBytes(bytes);\n"+
            "$T rootObject = $L(obtain);\n" +
            "readsPool.free(obtain);\n" +
            "return rootObject",byteReads,byteReads,type.tname(),readMethod(type)));

            builder.addMethod(writeMethod.build());
            builder.addMethod(toBytesMethod.build());
            builder.addMethod(readMethod.build());
            builder.addMethod(fromBytesMethod.build());

            //extra readMethod
            builder.addMethod(MethodSpec.methodBuilder(readMethod(type))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(Reads.class, "read")
            .addStatement("$L object = new $L()", type.fullName(), type.fullName())
            .addStatement("$L(read,object)", readMethod(type))
            .addStatement("return object")
            .returns(type.cname())
            .build());

        }

        write(builder, imports.toSeq());
    }

    private String toBytesMethod(Stype type){
        return "toBytes" + Strings.capitalize(type.name());
    }
    private String fromBytesMethod(Stype type){
        return "fromBytes" + Strings.capitalize(type.name());
    }
    private String writeMethod(Stype type){
        return "write" + Strings.capitalize(type.name());
    }

    private String readMethod(Stype type){
        return "read" + Strings.capitalize(type.name());
    }
}
