package mmc;

import lombok.*;
import mmc.extentions.*;
import org.gradle.api.*;
import org.gradle.api.model.*;
import org.gradle.api.tasks.*;

public class MindustryModCoreExtension implements
    AbstractExtension,
    AddKaptAnnotationsExtensions,
    AddArcLibraryExtension,
    SetupAnnotationProjectExtension,
    AddMindustryCoreExtension,
    SetupSpriteGenerationTask,
    JarMindustryTaskExtension{
    public final Project project;
    @Getter(onMethod_ = {@Input})
    private final ProjectInfo projectInfo;

    public MindustryModCoreExtension(Project project){
        this.project = project;
        ObjectFactory objects = project.getObjects();
        projectInfo = new ProjectInfo(objects);
    }


    @SuppressWarnings("rawtypes")
    @Input
    public void projectInfo(Action<ProjectInfo> closure){
        closure.execute(projectInfo);
//        project.configure(projectInfo,closure);
    }


    @Override
    public Project getProject(){
        return project;
    }
}
