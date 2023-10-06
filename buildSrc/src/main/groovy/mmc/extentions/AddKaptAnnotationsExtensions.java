package mmc.extentions;

import org.gradle.api.*;

public interface AddKaptAnnotationsExtensions extends AbstractExtension{
    default void addKaptAnnotations(){
        Project project = getProject();
        var plugins = project.getPlugins();
        var exts = project.getExtensions();
        var tasks = project.getTasks();

        // Apply 'java', 'kotlin-jvm', and 'kotlin-kapt' plugins.
        plugins.apply("java");
        plugins.apply("kotlin");
        plugins.apply("kotlin-kapt");

        // Add the `entityAnno{}` extension


        project.afterEvaluate(p -> {
            // Configure KAPT extension.
            AddKaptAnnotationsExtension__initKaptImpl.initKapt(exts, tasks);
        });
    }

}
