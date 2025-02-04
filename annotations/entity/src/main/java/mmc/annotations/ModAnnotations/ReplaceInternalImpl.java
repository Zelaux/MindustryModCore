package mmc.annotations.ModAnnotations;

import java.lang.annotation.*;

/**
 * Replaces internal implementation for InternalImpl methods such as write, read, add, etc.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ReplaceInternalImpl{
}
