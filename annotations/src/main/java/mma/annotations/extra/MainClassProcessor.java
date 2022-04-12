package mma.annotations.extra;

import arc.files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.JsonWriter.*;
import mindustry.annotations.util.*;
import mma.annotations.ModAnnotations.*;
import mma.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;

@SupportedAnnotationTypes({
"mma.annotations.ModAnnotations.MainClass",
})
public class MainClassProcessor extends ModBaseProcessor{
    @Override
    public void process(RoundEnvironment env) throws Exception{
        Seq<Stype> types = types(MainClass.class);
        if(types.isEmpty()) return;
        if(types.size > 1){
            err("There cannot be more than one main class");
            return;
        }
        Stype mainClass = types.get(0);
        if(mainClass.e.getModifiers().contains(Modifier.PRIVATE)){
            err("Main class cannot be private");
            return;
        }
        String descriptor = mainClass.fullName();
        Fi path = findPath(mainClass.annotation(MainClass.class).modInfoPath());
        if(path == null) return;

        String string = path.readString();
        int index = string.indexOf("main");
        if(index == -1){
            index = string.indexOf("\"main\"");
        }
        if(index == -1){
            index = string.indexOf("'main'");
        }
        if(index == -1){
            processNonExist(path, string, descriptor);
            return;
        }
        int start = string.indexOf(":", index);
        int end = string.indexOf("\n", start);
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

    private void processNonExist(Fi path, String string, String descriptor){
        if(string.endsWith("}")){
            path.writeString(string.substring(0, string.length() - 1) + "\n,\"main\": \"" + descriptor + "\"\n}");
            return;
        }
        path.writeString(string + "\n,\"main\": \"" + descriptor+'"');
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
        err("Cannot find 'mod.(h)json' file");
        return null;
    }
}
