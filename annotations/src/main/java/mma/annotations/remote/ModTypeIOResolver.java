package mma.annotations.remote;

import arc.func.Cons;
import arc.struct.*;
import arc.util.*;
import mindustry.annotations.Annotations;
import mindustry.annotations.Annotations;
import mindustry.annotations.util.Smethod;
import mindustry.annotations.util.Stype;
import mindustry.annotations.util.Svar;
import mindustry.annotations.util.TypeIOResolver;
import mma.annotations.ModBaseProcessor;

import javax.lang.model.element.Modifier;

public class ModTypeIOResolver extends TypeIOResolver {
    static Seq<Smethod> methods = new Seq<>();
    static ObjectMap<Stype, Seq<Smethod>> methodsMap = new ObjectMap<>();

    public static ClassSerializer resolve(ModBaseProcessor processor) {
        methods.clear();
        ClassSerializer out = new ClassSerializer(new ObjectMap<>(), new ObjectMap<>(), new ObjectMap<>());
        Seq<Stype> types = processor.types(Annotations.TypeIOHandler.class);
        ObjectMap<String,Stype> typeMap = new ObjectMap<>();
        Cons<Stype> addNewType = nt -> {
            if (!typeMap.containsKey(nt.fullName())){
                typeMap.put(nt.fullName(),nt);
            }
        };
        for (Stype stype : types) {
            addNewType.get(stype);
            Seq<Stype> allSuperclasses = stype.allSuperclasses();
//            allSuperclasses.reverse();
            for (Stype superclass : allSuperclasses) {
                String superFullname = superclass.fullName();
                if (superFullname.equals(Object.class.getName()))continue;
                addNewType.get(superclass);
            }
        }
        types.set(typeMap.values().toSeq());
        boolean debug=false;
        if(processor.annotationsSettings().getBool("debug")){
            debug=true;
        }
//        types.reverse();
        for (Stype type : types) {
            if (debug){
                System.out.println(Strings.format("use methods from @",type.fullName()));
            }
            //look at all TypeIOHandler methods
            for (Smethod method : type.methods()) {
                addMethod(type, method);
            }
        }
        Seq<Stype> keys = methodsMap.keys().toSeq();
        keys.sort((t1, t2) -> {
            boolean c1 = t1.allSuperclasses().contains(a -> a.fullName().equals(t2.fullName()));
            boolean c2 = t2.allSuperclasses().contains(a -> a.fullName().equals(t1.fullName()));
            return Boolean.compare(c1, c2);
        });
        for (Stype key : keys) {
            processMethods(out, key, methodsMap.get(key));
        }
        return out;
    }

    private static void processMethods(ClassSerializer out, Stype type, Seq<Smethod> methods) {
        for (Smethod meth : methods) {
//            Log.info("meth: @",meth);
            if (meth.is(Modifier.PUBLIC) && meth.is(Modifier.STATIC)) {
                Seq<Svar> params = meth.params();
                //2 params, second one is type, first is writer
                if (params.size == 2 && params.first().tname().toString().equals("arc.util.io.Writes")) {
                    out.writers.put(fix(params.get(1).tname().toString()), type.fullName() + "." + meth.name());
                } else if (params.size == 1 && params.first().tname().toString().equals("arc.util.io.Reads") && !meth.isVoid()) {
                    //1 param, one is reader, returns type
                    out.readers.put(fix(meth.retn().toString()), type.fullName() + "." + meth.name());
                } else if (params.size == 2 && params.first().tname().toString().equals("arc.util.io.Reads") && !meth.isVoid() && meth.ret().equals(meth.params().get(1).mirror())) {
                    //2 params, one is reader, other is type, returns type - these are made to reduce garbage allocated
                    out.mutatorReaders.put(fix(meth.retn().toString()), type.fullName() + "." + meth.name());
                }
            }
//            Log.info("readers added: @", copy);
        }
    }

    private static void addMethod(Stype parent, Smethod smethod) {
        if (methodsMap.values().toSeq().contains(seq -> seq.contains(other -> other.name().equals(smethod.name()))))
            return;
        methodsMap.get(parent, Seq::new).add(smethod);
    }

    /**
     * makes sure type names don't contain 'gen'
     */
    private static String fix(String str) {
//        if (true)return str;
        return str
                .replace("mindustry.gen.", "")
                .replace("mma.gen.", "")
                .replace(ModBaseProcessor.rootPackageName + ".gen.", "");
    }

}
