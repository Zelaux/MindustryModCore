package mma.annotations;

import mindustry.annotations.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ModAnnotations extends Annotations {
    //Zelaux annotations

    /** The return statement of a method with this annotation will not be replaced. */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface GlobalReturn{
    }

//    @Retention(RetentionPolicy.SOURCE)
    public @interface EntitySuperClass {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EntitySuperInterface {
    }


    @Retention(RetentionPolicy.SOURCE)
    public @interface ModAssetsAnnotation {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DependenciesAnnotation {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface AnnotationProcessor {
    }


}
