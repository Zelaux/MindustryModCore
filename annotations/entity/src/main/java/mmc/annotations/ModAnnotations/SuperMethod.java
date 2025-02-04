package mmc.annotations.ModAnnotations;

import java.lang.annotation.*;

/**
 * Marks the component method that should have an implementation of another method
 * but without an implementation from current class
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface SuperMethod{
    String parentName();

    Class[] params() default {};
}
