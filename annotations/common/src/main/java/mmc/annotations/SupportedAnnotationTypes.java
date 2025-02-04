package mmc.annotations;

import javax.annotation.processing.*;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used to indicate what annotation interfaces an
 * annotation processor supports.  The {@link
 * Processor#getSupportedAnnotationTypes} method can construct its
 * result from the value of this annotation, as done by {@link
 * AbstractProcessor#getSupportedAnnotationTypes}.  Only {@linkplain
 * Processor#getSupportedAnnotationTypes strings conforming to the
 * grammar} should be used as values.
 *
 * @author Joseph D. Darcy
 * @author Scott Seligman
 * @author Peter von der Ah&eacute;
 * @since 1.6
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface SupportedAnnotationTypes {
    /**
     * {@return the names of the supported annotation interfaces}
     */
    Class<? extends Annotation> [] value();
}
