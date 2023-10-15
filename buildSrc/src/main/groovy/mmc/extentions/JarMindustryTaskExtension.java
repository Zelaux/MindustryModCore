package mmc.extentions;

import arc.struct.*;
import mmc.*;
import org.gradle.api.*;
import org.gradle.api.tasks.*;

import java.util.*;

public interface JarMindustryTaskExtension extends AbstractExtension{
    default void jarMindustryTask(Project project,Project... extra){
        for(Project proj : Seq.with(project).addAll(extra)){
            TaskContainer tasks = proj.getTasks();
            String jarMindustry = PropertyConfigurations.jarMindustryTaskName.get(proj);
            tasks.register(jarMindustry, JarMindustryTask.class, task -> {
                task.setGroup(Objects.requireNonNull(tasks.getByName("jar").getGroup()));
                task.dependsOn(proj.getTasksByName("jar", false));
            });
        }
    }

    default void jarMindustryTask(){
        jarMindustryTask(getProject());
    }
}
