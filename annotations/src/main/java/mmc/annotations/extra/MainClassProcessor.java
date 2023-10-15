package mmc.annotations.extra;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import mindustry.annotations.util.*;
import mindustry.mod.*;
import mmc.annotations.*;
import mmc.annotations.ModAnnotations.*;
import mmc.annotations.SupportedAnnotationTypes;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.lang.annotation.*;

@SupportedAnnotationTypes(
    mmc.annotations.ModAnnotations.MainClass.class
)
public class MainClassProcessor extends ModBaseProcessor{
    @Override
    public void process(RoundEnvironment env) throws Exception{
        Seq<Stype> types = types(MainClass.class);
        if(types.isEmpty()) return;
        if(types.size > 1){
            err("You have more than one Main class(" + types.toString(",", Stype::fullName) + ")");
            return;
        }
        Stype mainClass = types.get(0);
        Stype innerMainClass = types(mainClass.annotation(MainClass.class), MainClass::value).get(0);
        if(!innerMainClass.fullName().equals(Mod.class.getName())){
            mainClass = innerMainClass;
        }
        if(mainClass.e.getModifiers().contains(Modifier.PRIVATE)){
            err("Main class cannot be private", mainClass);
            return;
        }
        if(!mainClass.allSuperclasses().contains(t -> t.fullName().equals(Mod.class.getName()))){
            err("Main should be instance of " + Mod.class.getName(), mainClass);
        }

        String descriptor = mainClass.fullName();
        Fi path = findPath(annotationsSettings(AnnotationSettingsEnum.modInfoPath, "\n"));
        if(path == null){
            err("Cannot find mod.(h)json file", mainClass);
            return;
        }

        String string = path.readString();
        int index = string.indexOf("\"main\"");
        if(index == -1){
            index = string.indexOf("'main'");
        }
        if(index == -1){
            index = string.indexOf("main");
        }
        if(index == -1){
            processNonExist(path, string, descriptor);
            return;
        }
        int start = string.indexOf(":", index);
        int end = string.indexOf("\n", start);
        if(end == -1) end = string.length();
        path.writeString(string.substring(0, start) + ": \"" + descriptor + '"' + string.substring(end));
    }

    <T extends Annotation> Seq<Stype> types(T t, Cons<T> consumer){
        try{
            consumer.get(t);
        }catch(MirroredTypesException e){
            return Seq.with(e.getTypeMirrors()).map(Stype::of);
        }
        throw new IllegalArgumentException("Missing types.");
    }

    private void processNonExist(Fi path, String string, String descriptor){
        if(string.endsWith("}")){
            path.writeString(string.substring(0, string.length() - 1) + "\n,\"main\": \"" + descriptor + "\"\n}");
            return;
        }
        path.writeString(string + "\n,\"main\": \"" + descriptor + '"');
    }

    private Fi findPath(String rawPath){
        if(!rawPath.equals("\n")){
            Fi child = rootDirectory.child(rawPath);
            if(!child.exists()){
                err("You wrote non-existent path: " + child.absolutePath());
                return null;
            }
            return child;
        }
        String[] ext = {"hjson", "json"};
        String[] names = {"mod", "plugin"};
        String[] paths = {
            "",
            "assets",
            "core/assets",
        };
        for(String path : paths){

            Fi folder = rootDirectory.child(path);
            if(folder.exists()){
                for(String name : names){
                    for(String extension : ext){
                        Fi file = folder.child(name + "." + extension);
                        if(file.exists()) return file;
                    }
                }
            }
        }
        return null;
    }
}
