package mma.annotations;

import mindustry.annotations.*;

import java.lang.annotation.*;

public class ModAnnotations extends Annotations{
    //Zelaux annotations

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

    @Retention(RetentionPolicy.SOURCE)
    public @interface EntitySuperClass{
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EntitySuperInterface{
    }


    @Retention(RetentionPolicy.SOURCE)
    public @interface ModAssetsAnnotation{
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DependenciesAnnotation{
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface AnnotationProcessor{
    }


}
