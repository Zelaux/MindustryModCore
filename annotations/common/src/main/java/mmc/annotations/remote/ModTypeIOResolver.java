package mmc.annotations.remote;

import arc.func.*;
import arc.struct.*;
import mindustry.annotations.*;
import mindustry.annotations.util.*;
import mindustry.annotations.util.TypeIOResolver.*;
import mmc.annotations.*;
import mmc.annotations.ModAnnotations.*;
import org.reflections.scanners.*;

import javax.lang.model.element.*;
import java.util.*;

public class ModTypeIOResolver{
    static Seq<Smethod> methods = new Seq<>();
    static ObjectMap<Stype, Seq<Smethod>> methodsMap = new ObjectMap<>();

    public static ClassSerializer resolve(ModBaseProcessor processor){
        methods.clear();
        ClassSerializer out = new ClassSerializer(new ObjectMap<>(), new ObjectMap<>(), new ObjectMap<>(), new ObjectMap<>());
        Seq<Stype> types = processor.types(Annotations.TypeIOHandler.class);
        if(types.isEmpty()){
            Set<String> strings = ModBaseProcessor.reflections().get(Scanners.TypesAnnotated.of(DefaultTypeIOHandler.class));
            for(String string : strings){
                types.add(new Stype(ModBaseProcessor.elementu.getTypeElement(string)));
            }
            if(strings.isEmpty()){
                TypeElement element = ModBaseProcessor.elementu.getTypeElement("mindustry.io.TypeIO");
                if(element==null){
                    throw new RuntimeException("Cannot find TypeIO handler. Mark your with @mindustry.annotations.Annotations.TypeIOHandler");
                }
                types.add(new Stype(element));
            }
        }
        Seq<Stype> nTypes = new Seq<>();
        ObjectSet<String> typesNames = new ObjectSet<>();
        Cons<Stype> addNewType = nt -> {
            if(typesNames.add(nt.fullName())){
                nTypes.add(nt);
            }
        };
        for(Stype stype : types){
            addNewType.get(stype);
            Seq<Stype> allSuperclasses = stype.allSuperclasses();
//            System.out.println(allSuperclasses);
            allSuperclasses.reverse();
//            System.out.println(allSuperclasses);
            for(Stype superclass : allSuperclasses){
                String superFullname = superclass.fullName();
                if(superFullname.equals(Object.class.getName())) continue;
                addNewType.get(superclass);
            }
        }
        types.set(nTypes);
        boolean debug = processor.annotationsSettings().getBool("debug");
        //        types.reverse();
        for(Stype type : types){
            if(debug){
                processor.debugLog("use methods from @", type.fullName());
            }
            //look at all TypeIOHandler methods
            for(Smethod method : type.methods()){
                addMethod(type, method);
            }
        }
        Seq<Stype> keys = methodsMap.keys().toSeq();
        keys.sort((t1, t2) -> {
            boolean c1 = t1.allSuperclasses().contains(a -> a.fullName().equals(t2.fullName()));
            boolean c2 = t2.allSuperclasses().contains(a -> a.fullName().equals(t1.fullName()));
            return Boolean.compare(c1, c2);
        });
        for(Stype key : keys){
            processMethods(out, key, methodsMap.get(key));
        }
        if(debug){
            processor.debugLog("writes: @", out.writers);
            processor.debugLog("netWriters: @", out.netWriters);
            processor.debugLog("readers: @", out.readers);
            processor.debugLog("mutatorReaders: @", out.mutatorReaders);
        }
        return out;
    }

    private static void processMethods(ClassSerializer out, Stype type, Seq<Smethod> methods){
        for(Smethod meth : methods){
            if(meth.is(Modifier.PUBLIC) && meth.is(Modifier.STATIC)){
                Seq<Svar> params = meth.params();
                //2 params, second one is type, first is writer
                if(params.size == 2 && params.first().tname().toString().equals("arc.util.io.Writes")){
                    //Net suffix indicates that this should only be used for sync operations
                    ObjectMap<String, String> targetMap = meth.name().endsWith("Net") ? out.netWriters : out.writers;

                    targetMap.put(fix(params.get(1).tname().toString()), type.fullName() + "." + meth.name());
                }else if(params.size == 1 && params.first().tname().toString().equals("arc.util.io.Reads") && !meth.isVoid()){
                    //1 param, one is reader, returns type
                    out.readers.put(fix(meth.retn().toString()), type.fullName() + "." + meth.name());
                }else if(params.size == 2 && params.first().tname().toString().equals("arc.util.io.Reads") && !meth.isVoid() && meth.ret().equals(meth.params().get(1).mirror())){
                    //2 params, one is reader, other is type, returns type - these are made to reduce garbage allocated
                    out.mutatorReaders.put(fix(meth.retn().toString()), type.fullName() + "." + meth.name());
                }
            }
//            Log.info("meth: @",meth);
            /*if(meth.is(Modifier.PUBLIC) && meth.is(Modifier.STATIC)){
                Seq<Svar> params = meth.params();
                //2 params, second one is type, first is writer
                if(params.size == 2 && params.first().tname().toString().equals("arc.util.io.Writes")){
                    out.writers.put(fix(params.get(1).tname().toString()), type.fullName() + "." + meth.name());
                }else if(params.size == 1 && params.first().tname().toString().equals("arc.util.io.Reads") && !meth.isVoid()){
                    //1 param, one is reader, returns type
                    out.readers.put(fix(meth.retn().toString()), type.fullName() + "." + meth.name());
                }else if(params.size == 2 && params.first().tname().toString().equals("arc.util.io.Reads") && !meth.isVoid() && meth.ret().equals(meth.params().get(1).mirror())){
                    //2 params, one is reader, other is type, returns type - these are made to reduce garbage allocated
                    out.mutatorReaders.put(fix(meth.retn().toString()), type.fullName() + "." + meth.name());
                }
            }*/
//            Log.info("readers added: @", copy);
        }
    }

    private static void addMethod(Stype parent, Smethod method){
        if(methodsMap.values().toSeq().contains(seq -> seq.contains(other -> {

            Seq<Svar> oparams = other.params();
            Seq<Svar> params = method.params();
            if(oparams.size == params.size){
                for(int i = 0; i < oparams.size; i++){
                    if(!oparams.get(i).tname().toString().equals(params.get(i).tname().toString())){
                        return false;
                    }
                }
            }else{
                return false;
            }
            return other.name().equals(method.name()) && other.retn().toString().equals(method.retn().toString());
        })))
            return;
        methodsMap.get(parent, Seq::new).add(method);
    }

    /**
     * makes sure type names don't contain 'gen'
     */
    private static String fix(String str){
//        if (true)return str;
        return str
            .replace("mindustry.gen.", "")
            .replace("mmc.gen.", "")
            .replace(ModBaseProcessor.rootPackageName + ".gen.", "");
    }

}
