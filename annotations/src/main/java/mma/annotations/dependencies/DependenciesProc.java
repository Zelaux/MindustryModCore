package mma.annotations.dependencies;

import arc.files.*;
import arc.util.serialization.*;
import arc.util.serialization.Jval.*;
import com.squareup.javapoet.*;
import mindustry.io.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mma.annotations.ModAnnotations.*;
import mma.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;


@SupportedAnnotationTypes("mma.annotations.ModAnnotations.DependenciesAnnotation")
public class DependenciesProc extends ModBaseProcessor{
    @Override
    public ModMeta modInfoNull(){
        String path = types(DependenciesAnnotation.class).first().annotation(DependenciesAnnotation.class).modInfoPath();
        Fi directory = this.rootDirectory;
        if(!path.equals("\n")){
            directory = rootDirectory.child(path);
        }
        String[] paths = {
        "mod.json",
        "mod.hjson",
        "plugin.json",
        "plugin.hjson",
        };
        Fi file = null;
        for(int i = 0; i < paths.length * 2; i++){
            boolean coreAssets = i >= paths.length;
            int index = i % paths.length;
            if(coreAssets){
                file = directory.child("core/assets").child(paths[index]);
            }else{

                file = directory.child(paths[index]);
            }
            if(file.exists()){
                break;
            }
            file = null;
        }
        if(file == null) return null;

        return JsonIO.json.fromJson(ModMeta.class, Jval.read(file.readString()).toString(Jformat.plain));
    }

    @Override
    public void process(RoundEnvironment env) throws Exception{
        TypeSpec.Builder builder = TypeSpec.classBuilder(classPrefix() + "Dependencies").addModifiers(Modifier.PUBLIC, Modifier.FINAL);


        //valid method
        MethodSpec.Builder valid = MethodSpec.methodBuilder("valid").addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(TypeName.get(boolean.class));
        Mods.ModMeta modMeta = modInfo();
        valid.addStatement("boolean valid=true");
        valid.beginControlFlow("try");

        for(String dependency : modMeta.dependencies){
            valid.addStatement("valid&=exists($S)", dependency);
        }
        valid.nextControlFlow("catch(Exception e)");
        valid.addStatement("e.printStackTrace()");
        valid.addStatement("valid=false");
        valid.endControlFlow();
        valid.addStatement("return valid");
        builder.addMethod(valid.build());
        //exists method
        MethodSpec.Builder existsBuilder = MethodSpec.methodBuilder("exists").addParameter(String.class, "mod").addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(TypeName.get(boolean.class));
        existsBuilder.addStatement("arc.util.serialization.Json json = new arc.util.serialization.Json()");
        existsBuilder.beginControlFlow("for (arc.files.Fi fi : mindustry.Vars.modDirectory.list())");
        existsBuilder.addStatement("arc.files.Fi zip = fi.isDirectory() ? fi : (new arc.files.ZipFi(fi))");
        existsBuilder.addStatement("arc.files.Fi metaf =zip.child(\"mod.json\").exists() ? zip.child(\"mod.json\") : zip.child(\"mod.hjson\")");
        existsBuilder.addStatement("if (!metaf.exists()) continue");
        existsBuilder.addStatement("if (json.fromJson(mindustry.mod.Mods.ModMeta.class, arc.util.serialization.Jval.read(metaf.readString()).toString(arc.util.serialization.Jval.Jformat.plain)).name.equals(mod)) return arc.Core.settings.getBool(\"mod-\"+mod+\"-enabled\",false)");
        existsBuilder.endControlFlow();
        existsBuilder.addStatement("return false");
        builder.addMethod(existsBuilder.build());

        write(builder);
//        super.process(env);
    }
}
