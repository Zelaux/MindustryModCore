package mmc.extentions;

import mmc.MindustryModCoreExtension;
import mmc.extentions.defaultjar.DeployTask;
import mmc.extentions.defaultjar.JarAndroidTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.TaskContainer;

public interface SetupDefaultJarTasks extends AbstractExtension {
    default void setupDefaultEnvironment(Project target) {
        TaskContainer tasks = getProject().getTasks();


        AbstractCopyTask jar = (AbstractCopyTask) tasks.getByName("jar");
        MindustryModCoreExtension extension = target.getExtensions().getByType(MindustryModCoreExtension.class);
        jar.doFirst(it->{
            jar.exclude(extension.getProjectInto().rootPackage.get()+"/entities/comp/**");
        });


        tasks.register("androidJar", JarAndroidTask.class,it->{
            it.setGroup("build");
            it.dependsOn("jar");
        });
        tasks.register("deploy", DeployTask.class, it->{
            it.setGroup("build");
            it.dependsOn("jar");
            it.dependsOn("androidJar");
        });


    }

    default void setupDefaultEnvironment() {
        setupDefaultEnvironment(getProject());
    }
}
