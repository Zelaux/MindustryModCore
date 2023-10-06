package mmc.extentions

import mmc.AnnotationProperties
import mmc.MindustryModCoreExtension
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar


class AddKaptAnnotationsExtension__initKaptImpl{
    static void initKapt(ExtensionContainer extentions, TaskContainer tasks){
        def kaptExt = extentions.getByName("kapt");
        kaptExt.setKeepJavacAnnotationProcessors(true);


        // Exclude fetched and generation source classes.
        for(def task : tasks.withType(Jar.class)){
            task.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
            /*task.exclude(ext.getGenSrcPackage().get().replace('.', '/') + "/**"
            );*/
        }
        AnnotationProperties annotationProperties = extentions.getByType(MindustryModCoreExtension.class).getAnnotationProperties()

        // Add annotation processor options.
        kaptExt.arguments(args -> {
            def addFile = { String name, RegularFileProperty property ->
                if(property.isPresent()){
                    args.arg(name, property.get().asFile.toString())
                }
            }
            def addString = { String name, Property<String> property ->
                if(property.isPresent()){
                    args.arg(name, property.get())
                }
            }
            annotationProperties.assetsPath().isPresent()
            addFile("modInfoPath", annotationProperties.modInfoPath())
            addFile("assetsPath", annotationProperties.assetsPath())
            addFile("revisionsPath", annotationProperties.revisionsPath())
            addString("classPrefix", annotationProperties.classPrefix())
            addString("rootPackage", annotationProperties.rootPackage())
        });

        // Prevent running these tasks to speed up compile-time.
        tasks.getByName("kaptGenerateStubsKotlin", t -> {
            t.onlyIf(spec -> false);
            t.getOutputs().upToDateWhen(spec -> true);
        });
        tasks.getByName("compileKotlin", t -> {
            t.onlyIf(spec -> false);
            t.getOutputs().upToDateWhen(spec -> true);
        });
    }
}
