package mma.annotations.remote;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mma.annotations.ModAnnotations;
import mindustry.annotations.Annotations;
import mindustry.annotations.BaseProcessor;
import mindustry.annotations.util.Smethod;
import mindustry.annotations.util.Stype;
import mindustry.annotations.util.Svar;
import mindustry.annotations.util.TypeIOResolver;

import javax.lang.model.element.Modifier;

public class ModTypeIOResolver extends TypeIOResolver {
    public static ClassSerializer resolve(BaseProcessor processor)  {
        ClassSerializer out = new ClassSerializer(new ObjectMap<>(), new ObjectMap<>(), new ObjectMap<>());
        Seq<Stype> types = processor.types(Annotations.TypeIOHandler.class);
        types.addAll(processor.types(ModAnnotations.TypeIOHandler.class));
        Seq<Smethod> usedMethods = new Seq<>();
        ObjectMap<String,Stype> typeMap=new ObjectMap<>();
        for (Stype type : types) {
            Log.info("type: @",type.fullName());
            typeMap.put(type.fullName(),type);
        }
        for (Stype stype : types.copy()) {
            Log.info("stype: @",stype.fullName());
            for (Stype superclass : stype.allSuperclasses()) {
                String superFullname = superclass.fullName();
                Log.info("superclass: @", superFullname);
                if (superFullname.contains("TypeIO") && !typeMap.containsKey(superFullname)){
                    typeMap.put(superFullname,superclass);
                }
            }
        }
        types.clear();
        types.addAll(typeMap.values());
        for(Stype type : types){
            //look at all TypeIOHandler methods

            Seq<Smethod> methods = type.methods().select(method->{
                if (usedMethods.find((um->um.name().equals(method.name())))==null){
                    usedMethods.add(method);
                    return true;
                }
                return false;
            });

            /*Seq<String> word=new Seq<>();
            methods.each(m->word.add(m.name()+"-"+m.fullName()));
//            processor.err("===="+word.toString(", ")+"====");
            word.clear();
//            methods.addAll(type.superclass().methods());
            Seq<Smethod> superMethods=type.superclass().methods();
            Seq<Smethod> finalMethods = methods;
            methods.addAll(superMethods);
//            methods.addAll(superMethods.select(sm-> finalMethods.find(m-> m.fullName().equals(sm.fullName()))==null));
//            methods.each(m->word.add(m.name()+"-"+m.fullName()));
//            processor.err("2===="+word.toString(", ")+"====2");*/
            for(Smethod meth : methods){
                if(meth.is(Modifier.PUBLIC) && meth.is(Modifier.STATIC)){
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
                }
            }
        }

        return out;
    }


    /** makes sure type names don't contain 'gen' */
    private static String fix(String str){
//        if (true)return str;
        return str.replace("mindustry.gen.", "");
    }

}
