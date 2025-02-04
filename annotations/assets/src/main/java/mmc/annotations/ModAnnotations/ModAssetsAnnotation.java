package mmc.annotations.ModAnnotations;

import java.lang.annotation.*;

/**
 * Starts the generation for assets' classes such as Tex, Music, Sounds etc.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface ModAssetsAnnotation{
    String separator() default "-";
}
