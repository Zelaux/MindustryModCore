package mmc.utils;

import arc.func.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.content.TechTree.*;
import mindustry.ctype.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Needs for adding new content into existed TechTrees.
 * In Runnable you can use methods from {@link mindustry.content.TechTree}
 */
public class TechTreeContext{
    private static Cons<TechNode> contextSetter;

    /**
     * @param context parent node
     * */
    public static void contextNode(TechNode context, Runnable children){
        Objects.requireNonNull(context, "techNode cannot be null");
        initContextField();

        TechNode prev = TechTree.context();
        contextSetter.get(context);
        children.run();
        contextSetter.get(prev);
    }
    /**
     * @param contextContent parent content
     * */
    public static void contextNode(UnlockableContent contextContent, Runnable children){
        Objects.requireNonNull(contextContent, "contextContent cannot be null");
        TechNode techNode = contextContent.techNode;
        Objects.requireNonNull(techNode, "contextContent.techNode cannot be null");
        contextNode(techNode, children);
    }

    private static void initContextField(){
        if(contextSetter != null) return;
        try{
            Field contextField = TechTree.class.getDeclaredField("context");
            contextField.setAccessible(true);
            contextSetter = context -> Reflect.set(null, contextField, context);
        }catch(Exception e){
            throw new RuntimeException(e);
        }

    }

}
