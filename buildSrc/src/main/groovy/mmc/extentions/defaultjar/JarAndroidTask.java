package mmc.extentions.defaultjar;

import arc.struct.Seq;
import arc.struct.Sort;
import arc.util.Strings;
import arc.util.Structs;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Comparator;

public class JarAndroidTask extends Zip {

    @TaskAction
    public void run() throws IOException, InterruptedException {

        String sdkRoot = System.getenv("ANDROID_HOME");
        if (sdkRoot == null) {
            sdkRoot = System.getenv("ANDROID_SDK_ROOT");
        }
        if (sdkRoot == null || !new File(sdkRoot).exists())
            throw new GradleException("No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.");

        File[] files = new File("$sdkRoot/platforms/").listFiles();
        Sort.instance().sort(files, Comparator.reverseOrder());
        assert files != null;
        File platformRoot = Structs.find(files, f -> new File(f, "android.jar").exists());

        if (platformRoot == null)
            throw new GradleException("No android.jar found. Ensure that you have an Android platform installed.");

        Seq<File> files1 = new Seq<>();
        files1.add(new File(platformRoot, "android.jar"));
        getProject().getConfigurations().getByName("compileClasspath").forEach(files1::add);
        getProject().getConfigurations().getByName("runtimeClasspath").forEach(files1::add);
        //collect dependencies needed for desugaring
        String dependencies = files1.toString(" ", it -> "--classpath " + it.getPath());

        String androidName = getArchiveBaseName().getOrElse(getProject().getName() + "Android.jar");
        TaskContainer tasks = getProject().getTasks();

        String desktopName = ((Zip) tasks.getByName("jar")).getArchiveBaseName().getOrElse(getProject().getName() + "Desktop.jar");
        ProcessBuilder builder = new ProcessBuilder();
        //dex and desugar files - this requires d8 in your PATH
        builder.command("d8 " + dependencies + " --min-api 14 --output " + androidName + " " + desktopName);
        builder.directory(new File(getProject().getBuildDir(), "libs"));
        ProcessGroovyMethods.waitForProcessOutput(builder.start(),(OutputStream) System.out, System.err);


    }
}
