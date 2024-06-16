package mmc.extentions.setupAnnotations;

import mmc.ClosureFactory.*;
import org.gradle.api.*;
import org.gradle.api.file.*;
import org.gradle.api.provider.*;
import org.gradle.api.tasks.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

import static org.codehaus.groovy.runtime.ResourceGroovyMethods.*;

public class WriteAnnotationProcessorsTask extends DefaultTask{
    static final Logger logger = LoggerFactory.getLogger(WriteAnnotationProcessorsTask.class);
    private final Property<RegularFile> targetFolder;

    public WriteAnnotationProcessorsTask(){
        targetFolder = getProject().getObjects().fileProperty();
        targetFolder.set(() -> {

            Project project = getProject();
            SourceSetContainer sourceSets = (SourceSetContainer)project.getExtensions().getByName("sourceSets");

            SourceSet main = sourceSets.getByName("main");
            SourceDirectorySet resources = main.getResources();
            Set<File> srcDirs = resources.getSrcDirs();
            if(!srcDirs.isEmpty()){
                File first = srcDirs.stream().sorted().findFirst().get();
                if(srcDirs.size() > 1){
                    logger.warn("Found multiple resources sources. Chosen '" + first + "' (use writeAnnotationProcessors.targetFolder to specified other)");
                }
                return first;
            }
            throw new IllegalArgumentException("please set writeAnnotationProcessors.targetFolder");
        });
        setOnlyIf(it -> {
            return true;
        });

        getOutputs().upToDateWhen(spec -> false);
    }/*
    @Internal
    public Property<RegularFile> getTargetFolder(){
        return targetFolder;
    }*/

    @TaskAction
    public void write() throws IOException{
        Project project = getProject();
        SourceSetContainer sourceSets = (SourceSetContainer)project.getExtensions().getByName("sourceSets");

        SourceSet main = sourceSets.getByName("main");
        SourceDirectorySet javaSources = main.getJava();
        StringBuilder fileBuilder = new StringBuilder();
        for(File srcDir : javaSources.getSrcDirs()){
            if(!srcDir.exists()) continue;
            //noinspection rawtypes
            eachFileRecurse(srcDir, groovy.io.FileType.FILES, new SimpleClosure(){
                public void doCall(File file) throws IOException{
                    String text = getText(file);
                    boolean isProcessor = text.contains(" extends ModBaseProcessor") ||
                                          (text.contains(" extends AbstractProcessor") && !text.contains("abstract class")) ||
                                          text.contains("@ModAnnotations.AnnotationProcessor") ||
                                          text.contains("@AnnotationProcessor");

                    if(file.getName().endsWith(".java") && isProcessor){
                        fileBuilder.append(file.getPath().substring(srcDir.getPath().length() + 1)).append("\n");
                    }
                }
            });
        }

        File processorFile = new File(targetFolder(), "META-INF/services/javax.annotation.processing.Processor");
        //noinspection ResultOfMethodCallIgnored
        processorFile.getParentFile().mkdirs();
        if(processorFile.exists()){
            //noinspection ResultOfMethodCallIgnored
            processorFile.delete();
        }
        setText(processorFile, fileBuilder.toString().replace(".java", "").replace("/", ".").replace("\\", "."));
    }

    @NotNull
    private File targetFolder(){
        return new File(getProject().getBuildDir(), "writeAnnotationProcessors");
    }
}
