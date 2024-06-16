package mmc.extentions.defaultjar;


import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.jvm.tasks.Jar;

public class DeployTask extends Zip {
    @TaskAction
    public void run(){
        Project project = getProject();
        TaskContainer tasks = project.getTasks();
        Zip jar = (Zip) tasks.getByPath("jar");
        Zip androidJar = (Zip) tasks.getByPath("androidJar");


        from(project.zipTree(jar.getArchiveFile().get()));
        from(project.zipTree(androidJar.getArchiveFile().get()));
        doLast(it->{
            project.delete(del->{
                del.delete(jar.getArchiveFile(),androidJar.getArchiveFile());
            });
        });
    }

}
