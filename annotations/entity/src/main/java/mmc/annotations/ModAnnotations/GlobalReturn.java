package mmc.annotations.ModAnnotations;

import java.lang.annotation.*;

/** The return statement of a method with this annotation will not be replaced. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface GlobalReturn{
}
