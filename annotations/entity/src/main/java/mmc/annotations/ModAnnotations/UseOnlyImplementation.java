package mmc.annotations.ModAnnotations;

import java.lang.annotation.*;

/**
 * Selects implementations for current method
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface UseOnlyImplementation{
    Class[] value();
}
