package mma.annotations.remote;

import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
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
        types.addAll(processor.types(Annotations.TypeIOHandler.class));
        Seq<Smethod> usedMethods = new Seq<>();
        Seq<Stype> newTypes = new Seq<>();
        Cons<Stype> addNewType = nt -> {
            if (!newTypes.contains(t -> t.fullName().equals(nt.fullName()))) {
                newTypes.add(nt);
            }
        };
        for (Stype stype : types.copy()) {
            addNewType.get(stype);
            Seq<Stype> allSuperclasses = stype.allSuperclasses();
//            allSuperclasses.reverse();
            for (Stype superclass : allSuperclasses) {
                String superFullname = superclass.fullName();
                if (superFullname.contains("TypeIO")) {
                    addNewType.get(superclass);
                }
            }
        }
        types.set(newTypes);
//        types.reverse();
        for (Stype type : types) {
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
            ObjectMap<String, String> copy1 = out.readers.copy();
            ObjectMap<String, String> copy2 = out.writers.copy();
            ObjectMap<String, String> copy3 = out.mutatorReaders.copy();
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
            ObjectMap<String, String> copy = out.readers.copy();
            for (String key : copy1.keys()) {
                copy.remove(key);
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
