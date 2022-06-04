package mma.annotations.extra;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import mindustry.annotations.util.*;
import mindustry.mod.*;
import mma.annotations.*;
import mma.annotations.ModAnnotations.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.lang.annotation.*;

@SupportedAnnotationTypes({
"mma.annotations.ModAnnotations.MainClass",
})
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
        if (!innerMainClass.fullName().equals(Mod.class.getName())){
            mainClass=innerMainClass;
        }
        if(mainClass.e.getModifiers().contains(Modifier.PRIVATE)){
            err("Main class cannot be private", mainClass);
            return;
        }/*
        if(!mainClass.allSuperclasses().contains(t -> t.fullName().equals(Mod.class.getName()))){
            err("Main should be instance of " + Mod.class.getName(), mainClass);
        }*/

        String descriptor = mainClass.fullName();
        Fi path = findPath(annotationsSettings(AnnotationSetting.modInfoPath, "\n"));
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
//        System.out.println("end: " + end);
//        Log.info("'@'", string.charAt(end));
//        Log.info("'@ '__", string.charAt(end-1));
//        Log.info("'@ '__", (int)string.charAt(end-1));
//        13
//        0xd
//        Log.info("'\u000B '__", (int)string.charAt(end-1));
//        Log.info("'@ '__", new JsonValue(string.charAt(end-1)+"").toJson(OutputType.json));
//        System.out.println(Character.getDirectionality(string.charAt(end )));
//        System.out.println(Character.getDirectionality(string.charAt(end - 1)));
//        System.out.println(Character.getDirectionality(string.charAt(end - 2)));
//        System.out.println(Character.getDirectionality(string.charAt(end - 3)));
        /*int i=0;
        while(Character.getDirectionality(string.charAt(end-i)==7 && start<(end-i)){
            char c = string.charAt(end - i );
            Log.info("char: \"@\"(@)(@)",c,(int)c,Character.getDirectionality(string.charAt(end-i-1)));
            i++;
        }
        if(string.charAt(end-i - 1) == ','){
            end=end-1-i;
            System.out.println("end2: " + end);
        }*/
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
                err("You wrote non-existent path");
                return null;
            }
            return child;
        }
        String[] paths = {
        "mod.json", "mod.hjson", "assets/mod.json", "assets/mod.hjson", "core/assets/mod.json", "core/assets/mod.hjson"
        };
        for(String path : paths){

            Fi file = rootDirectory.child(path);
            if(file.exists()) return file;
        }
        return null;
    }
}
