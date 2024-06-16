package mmc.extentions;

import groovy.lang.*;
import org.gradle.api.*;
import org.gradle.api.plugins.*;

import static mmc.ClosureFactory.stringToStringClosure;
import static mmc.utils.MainUtils.findVersion;

public interface AddArcLibraryExtension extends AbstractExtension{
    /**
     * Adds zelaux repository and arcLibraryModule(moduleName) function
     * */
    default void addArcLibrary(){
        Project project = getProject();
        ExtraPropertiesExtension extraProperties = project.getExtensions().getExtraProperties();
        project.getRepositories().maven(it->{
            it.setUrl("https://raw.githubusercontent.com/Zelaux/Repo/master/repository");
        });
        Closure<String> arcLibraryVersion = stringToStringClosure(name -> {
            //module path to full submodule name
            name = String.join("-", name.split(":"));
            Object version = findVersion(extraProperties, "`arcLibraryVersion` is not specified", "arcLibraryVersion");
            return "com.github.Zelaux.ArcLibrary:" + name + ":" + version;
        });
        extraProperties.set("arcLibraryModule", arcLibraryVersion);
        extraProperties.set("arcLibModule", arcLibraryVersion);
    }
}
