package mmc;

import mmc.extentions.*;
import org.gradle.api.*;
import org.gradle.api.file.*;
import org.gradle.api.model.*;
import org.gradle.api.plugins.*;
import org.gradle.api.provider.*;
import org.gradle.api.tasks.*;
import org.jetbrains.annotations.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class MindustryModCoreExtension implements
    AbstractExtension,
    AddKaptAnnotationsExtensions,
    AddArcLibraryExtension,
    SetupAnnotationProjectExtension,
    AddZelauxCoreExtension{
    public final Project project;

    private final AnnotationProperties annotationProperties;

    public MindustryModCoreExtension(Project project){
        this.project = project;
        ObjectFactory objects = project.getObjects();
        //noinspection rawtypes
        annotationProperties = new AnnotationProperties(){
            private final Property modInfoPath = fileProp(), asssertsPath = fileProp(), revisionsPath = fileProp(), classPrefix = strProp(), rootPackage = strProp();

            private Property<String> strProp(){
                return objects.property(String.class);
            }

            @NotNull
            private Property<RegularFile> fileProp(){
                return objects.fileProperty();
            }

            @Override
            public Property<RegularFile> modInfoPath(){
                return modInfoPath;
            }

            @Override
            public Property<RegularFile> assetsPath(){
                return asssertsPath;
            }

            @Override
            public Property<RegularFile> revisionsPath(){
                return revisionsPath;
            }

            @Override
            public Property<String> classPrefix(){
                return classPrefix;
            }

            @Override
            public Property<String> rootPackage(){
                return rootPackage;
            }
        };
    }

    static void addJarMindustry(Project project, ExtraPropertiesExtension extraProperties){
        String jarMindustry = String.valueOf(extraProperties.getProperties().getOrDefault("mmc.tasks.jarMindustry", "jarMindustry"));
        TaskContainer tasks = project.getTasks();
        tasks.register(jarMindustry, JarMindustryTask.class, task -> {
            task.setGroup(Objects.requireNonNull(tasks.getByName("jar").getGroup()));
            task.dependsOn(project.getTasksByName("jar", false));
        });
    }


    @Internal
    public AnnotationProperties getAnnotationProperties(){
        return annotationProperties;
    }

    public void jarMindustryTask(){
        addJarMindustry(project, project.getExtensions().getExtraProperties());
    }


    @Override
    public Project getProject(){
        return project;
    }
}
