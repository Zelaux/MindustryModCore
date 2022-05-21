package mma.annotations;

import mindustry.annotations.*;

import java.lang.annotation.*;

@SuppressWarnings("ALL")
public class ModAnnotations extends Annotations{
    //Zelaux annotations
    /** Indicates an entity definition. */
    @Retention(RetentionPolicy.SOURCE)
    public @interface MindustryEntityDef{
        /** List of component interfaces */
        Class[] value();
        /** Whether the class is final */
        boolean isFinal() default true;
        /** If true, entities are recycled. */
        boolean pooled() default false;
        /** Whether to serialize (makes the serialize method return this value).
         * If true, this entity is automatically put into save files.
         * If false, no serialization code is generated at all. */
        boolean serialize() default true;
        /** Whether to generate IO code. This is for advanced usage only. */
        boolean genio() default true;
        /** Whether I made a massive mistake by merging two different class branches */
        boolean legacy() default false;
    }

    /** The return statement of a method with this annotation will not be replaced. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface GlobalReturn{
    }

    //Used for generate entity classes
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface IgnoreImplementation{
        Class[] value();
    }

    //Used for generate entity classes
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface UseOnlyImplementation{
        Class[] value();
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface EntitySuperClass{
    }


    @Retention(RetentionPolicy.SOURCE)
    public @interface ModAssetsAnnotation{
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DependenciesAnnotation{
        /**local path from project directory
         * */
        String modInfoPath() default "\n";
    }

    /*@Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface AddImplementationFrom{
        Class[] interfaces();

        String methodName();
    }*/


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface SuperMethod{
        String parentName();

        Class[] params() default {};
    }


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface ReplaceInternalImpl{
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface MainClass{
        /**local path from project directory
         * */
        String modInfoPath() default "\n";
    }
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface CreateMindustrySerialization{
    }
  /*  @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface BeforeInternalImpl{
    }*/

}
