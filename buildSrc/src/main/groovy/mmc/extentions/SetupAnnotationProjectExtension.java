package mmc.extentions;

import mmc.extentions.setupAnnotations.*;
import org.gradle.api.*;
import org.gradle.api.plugins.*;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.compile.*;

import java.io.*;
import java.util.*;

public interface SetupAnnotationProjectExtension extends AbstractExtension{
    default void setupAnnotationProject(){
        setupAnnotationProject(getProject());
    }

    default void setupAnnotationProject(Project project){
        TaskContainer tasks = project.getTasks();
//        Jar jar = (Jar)tasks.getByName("jar");
        project.getPlugins().apply("java-library");
        WriteAnnotationProcessorsTask processorsTask = tasks.register("writeAnnotationProcessors", WriteAnnotationProcessorsTask.class).get();
        try{

            Task task = tasks.getByName("processResources");
            task.dependsOn(processorsTask);
            File file = new File(getProject().getBuildDir(), "writeAnnotationProcessors");
            ((Copy)task).from(file);
        }catch(UnknownTaskException e){
            tasks.whenTaskAdded(task -> {
                if(!task.getName().equals("processResources"))return;
//                Task task = tasks.getByName("processResources");
                task.dependsOn(processorsTask);
                File file = new File(getProject().getBuildDir(), "writeAnnotationProcessors");
                ((Copy)task).from(file);
            });
        }

        tasks.withType(JavaCompile.class, it -> {
        });
        project.getPlugins().apply("java-library");
        project.getRepositories().mavenCentral();
        project.getDependencies().add("implementation", "com.squareup:javapoet:1.12.1");
        tasks.withType(JavaCompile.class, type -> {
            type.setTargetCompatibility("8");
            JavaPluginExtension pluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
            pluginExtension.setSourceCompatibility(8);
            pluginExtension.setTargetCompatibility(8);
            type.setSourceCompatibility("8");
            type.getOptions().setFork(true);

            //def part
            CompileOptions options = type.getOptions();
            options.setEncoding("UTF-8");
            options.getCompilerArgs().add("-Xlint:deprecation");
            ForkOptions forkOptions = options.getForkOptions();
            String[] args = {
                "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
                "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                "--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
                "--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
                "--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
                "--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
                "--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                "--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED"
            };
            for(String arg : args){
                Objects.requireNonNull(forkOptions.getJvmArgs()).add(arg);
            }
        });
    }
}
