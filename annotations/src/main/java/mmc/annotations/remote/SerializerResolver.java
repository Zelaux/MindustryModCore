package mmc.annotations.remote;

import arc.struct.Seq;
import mindustry.annotations.*;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

public class SerializerResolver {

    public static String locate(ExecutableElement elem, TypeMirror mirror, boolean write) {
        //generic type
        if ((mirror.toString().equals("T") && Seq.with(elem.getTypeParameters().get(0).getBounds()).contains(SerializerResolver::isEntity)) ||
            isEntity(mirror)) {
            return write ? "mindustry.io.TypeIO.writeEntity" : "mindustry.io.TypeIO.readEntity";
        }
        return null;
    }

    private static boolean isEntity(TypeMirror mirror) {
        return !mirror.toString().contains(".") ||
        (mirror.toString().startsWith("mindustry.gen.") ||
        mirror.toString().startsWith(BaseProcessor.packageName+".")) && !mirror.toString().startsWith("byte");
    }
}
