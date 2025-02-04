package mmc.annotations.serialization;

import arc.files.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.pooling.*;
import arc.util.serialization.*;
import com.squareup.javapoet.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import lombok.*;
import mindustry.annotations.util.*;
import mindustry.annotations.util.TypeIOResolver.*;
import mmc.annotations.*;
import mmc.annotations.SupportedAnnotationTypes;
import mmc.annotations.ModAnnotations.*;
import mmc.annotations.remote.*;
import mmc.annotations.serialization.ObjectIO.*;
import mmc.annotations.util.*;
import org.intellij.lang.annotations.*;
import org.jetbrains.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.element.Modifier;
import javax.tools.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.List;
import java.util.stream.*;

@SupportedAnnotationTypes(Serialize.class)
public class SerializeProcessor extends ModBaseProcessor{
    static Seq<Stype> types;
    ClassSerializer resolve;

    private static void writeIOPackage(String writeString, String fileName) throws IOException{
        String packageName = rootPackageName + ".io";
//        JavaFile file = JavaFile.builder(packageName, builder.build()).skipJavaLangImports(true).build();
        saveStringFile(packageName, writeString, fileName);
    }

    private static void saveStringFile(String packageName, String writeString, String fileName) throws IOException{
        JavaFileObject object = filer.createSourceFile(packageName + "." + fileName);
        Writer stream = object.openWriter();
        stream.write(writeString);
        stream.close();
    }

    private static String javaText(@Language("JAVA") String javaCode){
        return javaCode;
    }

    private static StringTemplate plainText(@Language("TEXT") String text){
        return StringTemplate.compile(text);
    }

    private static void collectEachChild(Seq<ClassSymbol> list, Symbol symbol){
        List<Symbol> enclosedElements = symbol.getEnclosedElements();
        if(enclosedElements.size() == 0 || symbol instanceof ClassSymbol){
            if(symbol instanceof ClassSymbol){
                list.add((ClassSymbol)symbol);
            }
            /*byte[] bytes = new byte[indent];
            Arrays.fill(bytes, (byte)' ');
            bytes[indent-1]='-';
            System.out.print(new String(bytes));
            System.out.print(symbol);
            System.out.println();*/
            return;
        }
        for(Symbol element : enclosedElements){
            collectEachChild(list, element);
        }
    }

    @NotNull
    private static <T> Jval printToJval(T object, Class<T> clazz) throws IllegalAccessException{
        Jval jval = Jval.newObject();
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields){
            if(java.lang.reflect.Modifier.isPublic(field.getModifiers())){
                jval.add(field.getName(), field.get(object).toString());
            }
        }
        return jval;
    }

    @Override
    public void process(RoundEnvironment env) throws Exception{

//        System.out.println(Modules.instance(((JavacProcessingEnvironment)env).getContext()));

        List<String> list =
            allClasses()
                .stream()
                .filter(it -> it.contains(".ByteWrites"))
                .sorted(Structs.comparing(it -> it.startsWith("arclibrary.utils.io")))
                .collect(Collectors.toList());
        if(rounds == 1){
            types = types(Serialize.class);
        }
        if(list.isEmpty()){
            if(rounds > 1){
                err("Strange moment");
                throw new RuntimeException("Strange moment");
            }
            rounds++;
            generateByteWritesAndByteReads();
            return;
        }

        ClassName byteWrites = ClassName.bestGuess(list.get(0));
        ClassName byteReads = ClassName.get(byteWrites.packageName(), "ByteReads");

        resolve = ModTypeIOResolver.resolve(this);
        ObjectMap<String, Seq<Stype>> map = new ObjectMap<>();
        for(Stype type : types){
            String prefix = type.annotation(Serialize.class).prefix();
            map.get(prefix.equals("NIL") ? classPrefix() : prefix, Seq::new).add(type);
        }
        for(Entry<String, Seq<Stype>> entry : map){
            generateSerializer(entry.key, entry.value, byteWrites, byteReads);
        }
//        write(compilationUnit);
    }

    private void generateByteWritesAndByteReads() throws Exception{
        //region writes
        val byteWritesCode = plainText("package ${myPackage}.io;\n" +
                                       "\n" +
                                       "import arc.util.io.*;\n" +
                                       "\n" +
                                       "import java.io.*;\n" +
                                       "\n" +
                                       "/**\n" +
                                       " * Uses to write something to clear bytes\n" +
                                       " * */\n" +
                                       "public class ByteWrites extends Writes{\n" +
                                       "    public final ReusableByteOutStream r = new ReusableByteOutStream(8192);\n" +
                                       "\n" +
                                       "    public ByteWrites(){\n" +
                                       "        super(null);\n" +
                                       "        output = new DataOutputStream(r);\n" +
                                       "    }\n" +
                                       "    public void reset(){\n" +
                                       "        r.reset();\n" +
                                       "    }\n" +
                                       "\n" +
                                       "    public byte[] getBytes(){\n" +
                                       "        return  r.toByteArray();\n" +
                                       "    }\n" +
                                       "}\n");
        //endregion
        //region reads
        val byteReadsCode = plainText("package ${myPackage}.io;\n" +
                                      "\n" +
                                      "import arc.util.io.*;\n" +
                                      "\n" +
                                      "import java.io.*;\n" +
                                      "/**\n" +
                                      " * Uses to read something from clear bytes\n" +
                                      " * */\n" +
                                      "public class ByteReads extends Reads{\n" +
                                      "    public final ReusableByteInStream r = new ReusableByteInStream();\n" +
                                      "\n" +
                                      "    public ByteReads(){\n" +
                                      "        super(null);\n" +
                                      "        input = new DataInputStream(r);\n" +
                                      "    }\n" +
                                      "\n" +
                                      "    public ByteReads(byte[] bytes){\n" +
                                      "        this();\n" +
                                      "        setBytes(bytes);\n" +
                                      "    }\n" +
                                      "\n" +
                                      "    public static ReusableByteInStream setBytes(Reads reads, byte[] bytes){\n" +
                                      "        if(reads instanceof ByteReads){\n" +
                                      "            ByteReads byteReads = (ByteReads)reads;\n" +
                                      "            byteReads.setBytes(bytes);\n" +
                                      "            return byteReads.r;\n" +
                                      "        }\n" +
                                      "        ReusableByteInStream reusableByteInStream = new ReusableByteInStream();\n" +
                                      "        reads.input = new DataInputStream(reusableByteInStream);\n" +
                                      "        reusableByteInStream.setBytes(bytes);\n" +
                                      "        return reusableByteInStream;\n" +
                                      "    }\n" +
                                      "\n" +
                                      "    public void setBytes(byte[] bytes){\n" +
                                      "        r.setBytes(bytes);\n" +
                                      "    }\n" +
                                      "}\n");
        //endregion
        writeIOPackage(byteReadsCode.toString("myPackage", rootPackageName), "ByteReads");
        writeIOPackage(byteWritesCode.toString("myPackage", rootPackageName), "ByteWrites");
    }

    Seq<String> getImports(Stype type){
//        System.out.println("elem: "+elem.asType().toString());
        return Seq.with(trees.getPath(type.e).getCompilationUnit().getImports()).map(Object::toString);
    }

    public void generateSerializer(String serializerPrefix, Seq<Stype> types, ClassName byteWrites, ClassName byteReads) throws Exception{
        ObjectMap<String, Stype> typeMap = types.asMap(Stype::fullName);

        ObjectSet<String> imports = new ObjectSet<>();
        TypeSpec.Builder builder = TypeSpec.classBuilder(serializerPrefix + "Serializer").addModifiers(Modifier.PUBLIC, Modifier.FINAL);
//        builder.addJavadoc("Requires: \"com.github.Zelaux.ArcLibrary:utils-io:$version\"");
//        CompilationUnit compilationUnit = new CompilationUnit();

//        ClassOrInterfaceDeclaration declaration = compilationUnit.addClass("Serializer", Keyword.PUBLIC, Keyword.FINAL);

        for(Entry<String, Stype> entry : typeMap){
            Stype type = entry.value;
            imports.addAll(getImports(type));
            resolve.writers.get(type.fullName(), () -> serializerPrefix + "Serializer." + writeMethod(type));
            resolve.readers.get(type.fullName(), () -> serializerPrefix + "Serializer." + readMethod(type));
        }
        builder.addField(FieldSpec.builder(Pool.class, "writesPool", Modifier.STATIC, Modifier.FINAL)
            .initializer(CodeBlock.of(
                "new $L<$T>(){\n" +
                "    @Override\n" +
                "    protected $T newObject(){\n" +
                "        return new $T();\n" +
                "    }\n" +
                "}", Pool.class.getCanonicalName(), byteWrites, byteWrites, byteWrites
            ))
            .build());
        builder.addField(FieldSpec.builder(Pool.class, "readsPool", Modifier.STATIC, Modifier.FINAL)
            .initializer(CodeBlock.of(
                "new $L<$T>(){\n" +
                "    @Override\n" +
                "    protected $T newObject(){\n" +
                "        return new $T();\n" +
                "    }\n" +
                "}", Pool.class.getCanonicalName(), byteReads, byteReads, byteReads
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
                                                    "return bytes", byteWrites, byteWrites, writeMethod(type)));

            MethodSpec.Builder fromBytesMethod = MethodSpec.methodBuilder(fromBytesMethod(type))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(byte[].class, "bytes")
                .returns(type.tname());
            fromBytesMethod.addStatement(CodeBlock.of("$T obtain = ($T)readsPool.obtain();\n" +
                                                      "obtain.setBytes(bytes);\n" +
                                                      "$T rootObject = $L(obtain);\n" +
                                                      "readsPool.free(obtain);\n" +
                                                      "return rootObject", byteReads, byteReads, type.tname(), readMethod(type)));
            MethodSpec.Builder fromBytesMethod2 = MethodSpec.methodBuilder(fromBytesMethod(type))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(byte[].class, "bytes")
                .addParameter(type.tname(), "rootObject")
                .returns(type.tname());
            fromBytesMethod2.addStatement(CodeBlock.of("$T obtain = ($T)readsPool.obtain();\n" +
                                                       "obtain.setBytes(bytes);\n" +
                                                       "$L(obtain,rootObject);\n" +
                                                       "readsPool.free(obtain);\n" +
                                                       "return rootObject", byteReads, byteReads, readMethod(type)));

            builder.addMethod(writeMethod.build());
            builder.addMethod(toBytesMethod.build());
            builder.addMethod(readMethod.build());
            builder.addMethod(fromBytesMethod.build());
            builder.addMethod(fromBytesMethod2.build());
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
