package mmc.extentions;

import mmc.*;
import mmc.extentions.setupAnnotations.*;
import org.gradle.api.*;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.bundling.*;
import org.gradle.api.tasks.compile.*;
import org.gradle.language.jvm.tasks.*;

import java.io.*;
import java.util.*;

public interface SetupAnnotationProjectExtension extends AbstractExtension{
    default void setupAnnotationProject(){
        Project project = getProject();
        TaskContainer tasks = project.getTasks();
//        Jar jar = (Jar)tasks.getByName("jar");
        WriteAnnotationProcessorsTask processorsTask = tasks.register("writeAnnotationProcessors", WriteAnnotationProcessorsTask.class).get();
        tasks.getByName("processResources").dependsOn(processorsTask);
        File file = new File(getProject().getBuildDir(), "writeAnnotationProcessors");
        ((Copy)tasks.getByName("processResources")).from(file);

        tasks.withType(JavaCompile.class,it->{
        });
        project.getPlugins().apply("java-library");
        project.getRepositories().mavenCentral();
        project.getDependencies().add("implementation", "com.squareup:javapoet:1.12.1");

        tasks.withType(JavaCompile.class,type->{
            type.setTargetCompatibility("8");
            type.setSourceCompatibility("8");
            type.getOptions().setFork(true);

            //def part
            CompileOptions options = type.getOptions();
            options.setEncoding("UTF-8");
            options.getCompilerArgs().add("-Xlint:deprecation");
            ForkOptions forkOptions = options.getForkOptions();
            String[] args={
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
        /*
        compileKotlin{
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_1_8
    }
        *
        * */
//        jar.metaInf(copySpec -> {
//            File file = new File(getProject().getBuildDir(), "writeAnnotationProcessors");
//            copySpec.from(file);
//        });


    }

    default void setupAnnotationProject(Project project){
        if(project == getProject()){
            setupAnnotationProject();
            return;
        }
        project.getPlugins().apply(MindustryModGradle.class);
        project.getExtensions().getByType(MindustryModCoreExtension.class).setupAnnotationProject();
    }
}
