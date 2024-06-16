package mmc.annotations.dependencies;

import arc.files.*;
import arc.util.*;
import arc.util.serialization.*;
import com.squareup.javapoet.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mmc.annotations.ModAnnotations.*;
import mmc.annotations.*;
import mmc.annotations.SupportedAnnotationTypes;

import javax.annotation.processing.*;
import javax.lang.model.element.*;


@SupportedAnnotationTypes({DependenciesAnnotation.class})
public class DependenciesProc extends ModBaseProcessor{

    @Override
    public void process(RoundEnvironment env) throws Exception{
        TypeSpec.Builder builder = TypeSpec.classBuilder(classPrefix() + "Dependencies").addModifiers(Modifier.PUBLIC, Modifier.FINAL);


        //valid method
        MethodSpec.Builder valid = MethodSpec.methodBuilder("valid").addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(TypeName.get(boolean.class));
        Mods.ModMeta modMeta = modInfo();
        valid.addStatement("boolean valid=true");
        valid.beginControlFlow("try");

        for(String dependency : modMeta.dependencies){
            valid.addStatement("valid&=has($S)", dependency);
            String name = Strings.kebabToCamel(dependency);
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            String[] methodTypes={"has","exists","enabled"};
            for(String methodType : methodTypes){
                builder.addMethod(MethodSpec.methodBuilder(methodType+ name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(boolean.class)
                .beginControlFlow("try")
                .addStatement("return $L($S)",methodType, dependency)
                .nextControlFlow("catch(Exception e)")
                .addStatement("e.printStackTrace()")
                .addStatement("return false")
                .endControlFlow()
                .build());
            }
        }
        valid.nextControlFlow("catch(Exception e)");
        valid.addStatement("e.printStackTrace()");
        valid.addStatement("valid=false");
        valid.endControlFlow();
        valid.addStatement("return valid");
        builder.addMethod(valid.build());

        //exists method
        addExitsMethod(builder);
        addHasMethod(builder);
        addEnabledMethod(builder);

        write(builder);
//        super.process(env);
    }

    private void addExitsMethod(TypeSpec.Builder builder){
        ClassName fiClass = ClassName.get(Fi.class);
        ClassName zipFiClass = ClassName.get(ZipFi.class);
        ClassName modMetaClass = ClassName.get(ModMeta.class);
        ClassName jvalClass = ClassName.get(Jval.class);

        MethodSpec.Builder existsBuilder = MethodSpec.methodBuilder("exists").addParameter(String.class, "mod").addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(TypeName.get(boolean.class));
        existsBuilder.addStatement("arc.util.serialization.Json json = new arc.util.serialization.Json()");
        existsBuilder.beginControlFlow("for ($T fi : mindustry.Vars.modDirectory.list())", fiClass);
        existsBuilder.addStatement("$T zip = fi.isDirectory() ? fi : (new $T(fi))", fiClass, zipFiClass);
        existsBuilder.addStatement("$T metaf =zip.child(\"mod.json\").exists() ? zip.child(\"mod.json\") : zip.child(\"mod.hjson\")", fiClass);
        existsBuilder.addStatement("if (!metaf.exists()) continue");
        existsBuilder.addStatement("if (json.fromJson($T.class, $T.read(metaf.readString()).toString($T.Jformat.plain)).name.equals(mod)) return true",
        modMetaClass, jvalClass, jvalClass
        );
        existsBuilder.endControlFlow();
        existsBuilder.addStatement("return false");
        builder.addMethod(existsBuilder.build());
    }
    private void addHasMethod(TypeSpec.Builder builder){
        MethodSpec.Builder existsBuilder = MethodSpec.methodBuilder("has").addParameter(String.class, "mod").addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(TypeName.get(boolean.class));
        existsBuilder.addStatement("return exists(mod) && enabled(mod)");
        builder.addMethod(existsBuilder.build());
    }
    private void addEnabledMethod(TypeSpec.Builder builder){
        MethodSpec.Builder existsBuilder = MethodSpec.methodBuilder("enabled").addParameter(String.class, "mod").addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(TypeName.get(boolean.class));
        existsBuilder.addStatement("return arc.Core.settings.getBool(\"mod-\"+mod+\"-enabled\",false)");
        builder.addMethod(existsBuilder.build());
    }
}
