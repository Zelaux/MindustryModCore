package mmc;

import lombok.*;
import lombok.experimental.*;
import org.gradle.api.file.*;
import org.gradle.api.model.*;
import org.gradle.api.provider.*;
import org.gradle.api.tasks.*;

import javax.inject.*;
import java.io.*;

@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@AllArgsConstructor
//@Getter
public class ProjectInfo{
    /** path relative to rootDirectory */
    Property<String> modInfoPath;
    /** path relative to rootDirectory */
    Property<String> assetsPath;
    /** path relative to rootDirectory */
    Property<String> assetsRawPath;
    /** path relative to rootDirectory */
    Property<String> revisionsPath;

    Property<String> rootPackage;
    Property<String> classPrefix;
    Property<String> ROOT_DIRECTORY;

    @Inject
    public ProjectInfo(ObjectFactory objects){
        this(
            objects.property(String.class),
            objects.property(String.class),
            objects.property(String.class),
            objects.property(String.class),
            objects.property(String.class),
            objects.property(String.class),
            objects.property(String.class)
        );
    }

    @Input
    public void setRootDirectory(File file){
        ROOT_DIRECTORY.set(file.getAbsolutePath());
    }

    @Input
    public void setModInfoPath(String modInfoPath){
        this.modInfoPath.set(modInfoPath);
    }

    @Input
    public void setAssetsPath(String assetsPath){
        this.assetsPath.set(assetsPath);
    }

    @Input
    public void setAssetsRawPath(String assetsRawPath){
        this.assetsRawPath.set(assetsRawPath);
    }

    @Input
    public void setRevisionsPath(String revisionsPath){
        this.revisionsPath.set(revisionsPath);
    }

    @Input
    public void setRootPackage(String rootPackage){
        this.rootPackage.set(rootPackage);
    }

    @Input
    public void setClassPrefix(String classPrefix){
        this.classPrefix.set(classPrefix);
    }
}
