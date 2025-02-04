package mmc.annotations.ModAnnotations;

import java.lang.annotation.*;

/**
 * Generates serializers and deserializers
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Serialize{
    public String prefix() default "NIL";
}
