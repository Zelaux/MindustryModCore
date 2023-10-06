package mmc;

import org.gradle.api.Named;
import org.gradle.api.file.*;
import org.gradle.api.internal.provider.*;
import org.gradle.api.provider.*;

import javax.inject.*;

public interface AnnotationProperties {
    Property<RegularFile> modInfoPath();

    Property<RegularFile> assetsPath();

    Property<RegularFile> revisionsPath();

    Property<String> classPrefix();

    Property<String> rootPackage();
}
