package mmc.annotations.ModAnnotations;

import java.lang.annotation.*;

/**
 * Indicates an anuke's entity definition
 */
@Retention(RetentionPolicy.SOURCE)
public @interface MindustryEntityDef{
    /** List of component interfaces */
    Class[] value();

    /** Whether the class is final */
    boolean isFinal() default true;

    /** If true, entities are recycled. */
    boolean pooled() default false;

    /**
     * Whether to serialize (makes the serialize method return this value).
     * If true, this entity is automatically put into save files.
     * If false, no serialization code is generated at all.
     */
    boolean serialize() default true;

    /** Whether to generate IO code. This is for advanced usage only. */
    boolean genio() default true;

    /** Whether I made a massive mistake by merging two different class branches */
    boolean legacy() default false;
}
