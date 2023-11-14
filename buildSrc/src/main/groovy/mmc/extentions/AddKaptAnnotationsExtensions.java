package mmc.extentions;

import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.util.*;
import kotlin.*;
import mmc.*;
import mmc.utils.*;
import org.gradle.api.*;
import org.gradle.api.file.*;
import org.gradle.api.plugins.*;
import org.gradle.api.provider.*;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.bundling.*;
import org.jetbrains.kotlin.gradle.dsl.*;
import org.jetbrains.kotlin.gradle.internal.*;
import org.jetbrains.kotlin.gradle.plugin.*;

import java.lang.reflect.*;

public interface AddKaptAnnotationsExtensions extends AbstractExtension{
    @Input
    default void addKaptAnnotations(){
        Project project = getProject();
        addKaptAnnotations(project);
    }
    @Input
    default void addKaptAnnotations(Project project,Project... extra){
        addKaptAnnotations(project);
        for(Project project1 : extra){
            addKaptAnnotations(project1);
        }
    }
    @Input
    default void addKaptAnnotations(Project project){
        PluginContainer plugins = project.getPlugins();
        /*try{
            plugins.getAt(MindustryModGradle.class);
        }catch(UnknownPluginException e){
            plugins.apply(MindustryModGradle.class);
        }*/
        ExtensionContainer extentions = project.getExtensions();
        TaskContainer tasks = project.getTasks();

        // Apply 'java', 'kotlin-jvm', and 'kotlin-kapt' plugins.
        plugins.apply("java");
        /*plugins.apply("kotlin");*/
        plugins.apply(KotlinPluginWrapper.class);
        /*plugins.apply("kotlin-kapt");*/
        plugins.apply(Kapt3GradleSubplugin.class);

        // Add the `entityAnno{}` extension


        project.afterEvaluate(p -> {
            // Configure KAPT extension.org.jetbrains.kotlin.gradle.plugin.KaptExtension
            org.jetbrains.kotlin.gradle.plugin.KaptExtension kaptExt = extentions.getByType(org.jetbrains.kotlin.gradle.plugin.KaptExtension.class);
            kaptExt.setKeepJavacAnnotationProcessors(true);


            // Exclude fetched and generation source classes.
            for(Jar task : tasks.withType(Jar.class)){
                task.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
            }
            ProjectInfo projectInfo = MainUtils.getMyExtension(project,getProject()).getProjectInfo();

            // Add annotation processor options.

            kaptExt.arguments(args -> {
                Class<ProjectInfo> infoClass = ProjectInfo.class;
                Field[] fields = infoClass.getDeclaredFields();
                StringMap map = new StringMap();
                map.put("ROOT_DIRECTORY", project.getRootDir().getAbsolutePath());
                for(Field field : fields){
                    field.setAccessible(true);
                    Property<?> o = Reflect.get(projectInfo, field);
                    if(!o.isPresent()) continue;
                    /*if(o instanceof DirectoryProperty){
                        args.arg(field.getName(), ((DirectoryProperty)o).get().getAsFile().getAbsolutePath());

                    }*/
                    map.put(field.getName(), String.valueOf(o.get()));
                }
//                addFile("modInfoPath", projectInfo.modInfoPath);
//                addFile("assetsPath", projectInfo.assetsPath);
//                addFile("assetsRawPath", projectInfo.assetsRawPath);
//                addFile("revisionsPath", projectInfo.revisionsPath);
//                addString("classPrefix", projectInfo.classPrefix);
//                addString("rootPackage", projectInfo.rootPackage);
                for(Entry<String, String> entry : map){
                    args.arg(entry.key, entry.value);
                }
                return Unit.INSTANCE;
            });

            // Prevent running these tasks to speed up compile-time.
            if(PropertyConfigurations.disableKotlinTasks.get(project).equals("true")){
                tasks.getByName("kaptGenerateStubsKotlin", t -> {
                    t.onlyIf(spec -> false);
                    t.getOutputs().upToDateWhen(spec -> true);
                });
                tasks.getByName("compileKotlin", t -> {
                    t.onlyIf(spec -> false);
                    t.getOutputs().upToDateWhen(spec -> true);
                });
            }
        });
    }

}
