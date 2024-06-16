package mmc.annotations;

import mindustry.annotations.*;
import mindustry.mod.*;
import mmc.local.annotations.LocalAnnotations.*;

import java.lang.annotation.*;

@SuppressWarnings("ALL")
@LocalAnnotation
public class ModAnnotations extends Annotations{
    //region misc. utils


    /** Automatically loads block regions annotated with this. */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface ALoad{
        /**
         * The region name to load. Variables can be used:
         * "@" -> block name
         * "@size" -> block size
         * "#" "#1" "#2" -> index number, for arrays
         */
        String value();

        /** 1D Array length, if applicable. */
        int length() default 1;

        /** 2D array lengths. */
        int[] lengths() default {};

        /** Fallback strings used to replace "@" (the block name) if the region isn't found. */
        String[] fallback() default {};
    }
    //endregion
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
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface GenerateDefaultImplementation{
    }

    /** The return statement of a method with this annotation will not be replaced. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface GlobalReturn{
    }

    /**
     * Ignores implementations from selected components for current method
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
    //region io
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultTypeIOHandler{
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
    }


    /**
     * Indicates the main class and writes it to mod.(h)json file
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface MainClass{
        Class<? extends Mod> value() default Mod.class;
    }

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

    /**
     * Sets AnnotationSettings
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
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

    /**
     * Generates MindustrySerialization
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface CreateMindustrySerialization{
    }

    /**
     * Generates serializers and deserializers
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Serialize{
        public String prefix() default "NIL";
    }
}
