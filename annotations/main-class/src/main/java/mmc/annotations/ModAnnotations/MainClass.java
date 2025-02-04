package mmc.annotations.ModAnnotations;

import mindustry.mod.*;

import java.lang.annotation.*;

/**
 * Indicates the main class and writes it to mod.(h)json file
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface MainClass{
    Class<? extends Mod> value() default Mod.class;
}
