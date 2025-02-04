package mmc.annotations.ModAnnotations;

import java.lang.annotation.*;

/**
 * Sets rootDirectory path
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RootDirectoryPath{
    /**
     * rootDirectory path(path from folder where gradle build folder is located)
     * @default "../"
     */
    String rootDirectoryPath() default "\n";
}
