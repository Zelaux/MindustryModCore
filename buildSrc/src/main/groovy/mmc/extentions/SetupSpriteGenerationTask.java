package mmc.extentions;

import mmc.extentions.setupSpriteGenerationTask.*;
import org.gradle.api.*;
import org.gradle.api.tasks.*;

public interface SetupSpriteGenerationTask extends AbstractExtension{
    default void setupSpriteGenerationTask(String className){
        setupSpriteGenerationTask(className,getProject());
    }
    default void setupSpriteGenerationTask(String className, Project project){
        TaskContainer tasks = project.getTasks();
        String group = "sprites";
        tasks.register("generateSprites", task -> {
            task.setGroup(group);
            task.doLast(it -> {
                SpritesTaskExecutor.generateSprites(project, className);
            });
//            task.dependsOn(project.getConfigurations().getByName("runtimeClasspath"));
            task.dependsOn(tasks.getByName("jar"));
        });
        tasks.register("processSprites", task -> {
            task.setGroup(group);
//            task.dependsOn(project.getConfigurations().getByName("runtimeClasspath"));
            task.dependsOn(tasks.getByName("jar"));
            task.doLast(it -> {
                SpritesTaskExecutor.processSprites(project, className);
            });
        });
    }
}
