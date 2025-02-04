package mmc.annotations.ModAnnotations;

import java.lang.annotation.*;

/**
 * Sets properties path
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Deprecated
public @interface AnnotationPropertiesPath{
    /**
     * @example "core/annotation.properties"
     * @default "annotation.properties"
     */
    String propertiesPath();
}
