package mmc.extentions;

import groovy.lang.*;
import mmc.*;
import org.gradle.api.*;
import org.gradle.api.plugins.*;

import static mmc.ClosureFactory.stringToStringClosure;
import static mmc.utils.MainUtils.findVersion;

public interface AddMindustryCoreExtension extends AbstractExtension{
    /**
     * Adds zelaux repository and modCoreModule(moduleName) function
     */
    default void addMindustryModCore(){
        Project project = getProject();
        ExtraPropertiesExtension extraProperties = project.getExtensions().getExtraProperties();
        String modCoreModuleFunction = PropertyConfigurations.zelauxModCoreName.get(project);
        project.getRepositories().maven(it -> {
            it.setUrl("https://raw.githubusercontent.com/Zelaux/Repo/master/repository");
        });
        Closure<String> arcLibraryVersion = stringToStringClosure(name -> {
            //module path to full submodule name
            String[] split = name.split(":");
            name = split[split.length - 1];
            Object version = findVersion(extraProperties, "`modCoreVersion` or `mindustryModCoreVersion` is not specified",  "modCoreVersion","mindustryModCoreVersion");
            return "com.github.Zelaux.MindustryModCore:" + name + ":" + version;
        });
        extraProperties.set(modCoreModuleFunction, arcLibraryVersion);
    }
}
