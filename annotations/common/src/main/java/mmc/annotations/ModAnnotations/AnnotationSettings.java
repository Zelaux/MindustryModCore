package mmc.annotations.ModAnnotations;

import mmc.local.annotations.LocalAnnotations.*;

import java.lang.annotation.*;

/**
 * Sets AnnotationSettings
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@LocalAnnotation
public @interface AnnotationSettings{

    /**
     * Assets path(needs if are using assets classes generation)
     * @default "core/assets"
     */
    String assetsPath() default "\n";

    /**
     * path to mod.(h)json
     * @default rootProject directory
     */
    String modInfoPath() default "\n";

    /**
     * Assets raw path(needs if are using Tex.java generation)
     * a null value means that it is equal to assetsPath
     * @default "core/assets-raw"
     */
    String assetsRawPath() default "\n";

    /**
     * Revisions path(needs if  are using entity generation)
     * @default "annotations/src/main/resources/revisions"
     */
    String revisionsPath() default "\n";

    /**
     * Prefix for generated classes
     * @default capitalized root package name
     */
    String classPrefix() default "\n";

    /**
     * root package name
     * @default the name of the first folder in "core/src"
     */
    String rootPackage() default "\n";
}
