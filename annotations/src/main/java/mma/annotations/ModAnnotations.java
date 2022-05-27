package mma.annotations;

import mindustry.annotations.*;

import java.lang.annotation.*;

@SuppressWarnings("ALL")
public class ModAnnotations extends Annotations{
    //Zelaux annotations

    //region not for public usage

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

    /**
     * Indicates as anuke's interfaces
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface EntitySuperClass{
    }
    //endregion

    //region entity interfaces

    /** The return statement of a method with this annotation will not be replaced. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface GlobalReturn{
    }

    /**
     * Ignores implementations for current method
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface IgnoreImplementation{
        Class[] value();
    }

    /**
     * Selects implementations for current method
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface UseOnlyImplementation{
        Class[] value();
    }

    /**
     * Marks the component method that should have an implementation of another method
     * but without an implementation from current class
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface SuperMethod{
        String parentName();

        Class[] params() default {};
    }

    /**
     * Replaces internal implementation for InternalImpl methods such as write, read, add, etc.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface ReplaceInternalImpl{
    }
    //endregion

    /**
     * Starts the generation for assets' classes such as Tex, Music, Sounds etc.
     */
    @Retention(RetentionPolicy.SOURCE)
    public @interface ModAssetsAnnotation{
    }

    /**
     * Generates YourPrefixDependencies class to verify the validity of dependencies
     * Use YourPrefixDependencies.valid() for checking
     */
    @Retention(RetentionPolicy.SOURCE)
    public @interface DependenciesAnnotation{
        /**
         * local path from project directory
         */
        String modInfoPath() default "\n";
    }


    /**
     * Indicates the main class and writes it to mod.(h)json file
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface MainClass{
        /**
         * local path from project directory
         */
        String modInfoPath() default "\n";
    }

    /**
     * Generates MindustrySerialization
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface CreateMindustrySerialization{
    }
}
