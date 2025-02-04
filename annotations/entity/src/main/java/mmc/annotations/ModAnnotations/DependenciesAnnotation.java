package mmc.annotations.ModAnnotations;

import java.lang.annotation.*;

/**
 * Generates YourPrefixDependencies class to verify the validity of dependencies
 * Use YourPrefixDependencies.valid() for checking
 */
@Retention(RetentionPolicy.SOURCE)
public @interface DependenciesAnnotation{
}
