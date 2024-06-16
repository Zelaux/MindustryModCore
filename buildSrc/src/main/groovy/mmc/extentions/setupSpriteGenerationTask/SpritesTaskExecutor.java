package mmc.extentions.setupSpriteGenerationTask;

import arc.files.*;
import arc.util.*;
import mmc.*;
import org.gradle.api.*;
import org.gradle.api.plugins.*;
import org.gradle.api.provider.*;
import org.gradle.api.tasks.*;

import java.util.concurrent.*;

public class SpritesTaskExecutor{

    public static void generateSprites(Project project, String imagePackerPath){
        sprites(project, imagePackerPath, false);
    }

    public static void processSprites(Project project, String imagePackerPath){
        sprites(project, imagePackerPath, !project.hasProperty("disableAntialias"));
    }

    private static void sprites(Project project, String imagePackerPath, boolean withAlias){
        ProjectInfo projectInfo = project.getExtensions().getByType(MindustryModCoreExtension.class).getProjectInfo();
        final Fi
            assets = fileIfPresent(project, projectInfo.assetsPath, "projectInfo.assetsPath"),
            assetsRaw = fileIfPresent(project, projectInfo.assetsRawPath, "projectInfo.assertsRawPath"),
            genFolder = assetsRaw.child("sprites_out/generated");

        project.delete(assetsRaw.child("sprites_out").absolutePath());
        project.copy(it -> {
            it.from(assetsRaw.child("sprites").absolutePath());
            it.from(assets.child("sprites-override").absolutePath());
            it.into(assetsRaw.child("sprites_out").absolutePath());
        });

        genFolder.mkdirs();

        project.javaexec(javaexec -> {
            javaexec.setMaxHeapSize("4048m");
            javaexec.args("-Xmx4048m");

            SourceSetContainer sourceSets =
                project.getExtensions().getByType(JavaPluginExtension.class)
                    .getSourceSets();
            javaexec.setClasspath(sourceSets.getByName("main").getRuntimeClasspath());

            javaexec.getMainClass().set(imagePackerPath);
            javaexec.setStandardOutput(System.out);
            javaexec.setStandardInput(System.in);
            javaexec.setWorkingDir(genFolder.absolutePath());
        });

        project.copy(copySpec -> {
            copySpec.from(assetsRaw.child("sprites_out/ui/icons").absolutePath());
            copySpec.into(assetsRaw.child("sprites_out/ui/").absolutePath());
        });
        project.delete(it -> {
            it.delete(assetsRaw.child("sprites_out/ui/icons").absolutePath());
            it.delete(assets.child("sprites").absolutePath());
        });
        if(withAlias){
            ExecutorService executor = Executors.newFixedThreadPool(16);
            long startMillis = System.currentTimeMillis();
            assetsRaw.child("sprites_out").walk(file -> {
                if(file.isDirectory()) return;
                String filePath = file.absolutePath();
                if(!filePath.endsWith(".png")) return;
                if(filePath.matches(".*[\\\\/]ui[\\\\/].*(icon-[^\\\\/]*)")) return;
                if(filePath.endsWith(".9.png")) return;
                executor.submit(() -> {
                    FileAntialiasing.antialias(file);
                });
            });
            Threads.await(executor);
            long endMillis = System.currentTimeMillis();
            Log.info("Time taken to AA: " + (endMillis - startMillis) / 1000f + "s");
        }
        Log.info("Sprites coping...");
        long startMillis = System.currentTimeMillis();
        project.copy(it -> {
            it.from(assetsRaw.child("sprites_out").absolutePath());
            it.into(assets.child("sprites").absolutePath());
        });
        long endMillis = System.currentTimeMillis();
        Log.info("Time taken to coping sprites: " + (endMillis - startMillis) / 1000f + "s");
    }

    private static Fi fileIfPresent(Project project, Property<String> property, String name){
        if(!property.isPresent()){
            throw new IllegalArgumentException("mindustryModCore." + name + " required");
        }

        return new Fi(project.getRootDir()).child(property.get());
    }
}
