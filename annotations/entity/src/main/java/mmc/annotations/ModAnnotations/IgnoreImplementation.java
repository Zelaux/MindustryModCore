package mmc.annotations.ModAnnotations;

import java.lang.annotation.*;

/**
 * Ignores implementations from selected components for current method
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface IgnoreImplementation{
    Class[] value();
}
