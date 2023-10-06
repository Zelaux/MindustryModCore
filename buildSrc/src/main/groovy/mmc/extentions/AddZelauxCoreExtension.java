package mmc.extentions;

import groovy.lang.*;
import org.gradle.api.*;
import org.gradle.api.plugins.*;

import static mmc.ClosureFactory.stringToStringClosure;
import static mmc.utils.MainUtils.findVersion;

public interface AddZelauxCoreExtension extends AbstractExtension{
    /**
     * Adds zelaux repository and arcLibraryModule(moduleName) function
     */
    default void addZelauxCoreExtension(){
        Project project = getProject();
        ExtraPropertiesExtension extraProperties = project.getExtensions().getExtraProperties();
        String modCoreModuleFunction = String.valueOf(
            extraProperties.getProperties().getOrDefault("mmc.functions.zelauxModCore", "modCoreModule")
        );
        project.getRepositories().maven(it -> {
            it.setUrl("https://raw.githubusercontent.com/Zelaux/Repo/master/repository");
        });
        Closure<String> arcLibraryVersion = stringToStringClosure(name -> {
            //module path to full submodule name
            String[] split = name.split(":");
            name = split[split.length - 1];
            Object version = findVersion(extraProperties, "`modCoreVersion` is not specified", "zelauxModCoreVersion", "modCoreVersion");
            return "com.github.Zelaux.ZelauxModCore:" + name + ":" + version;
        });
        extraProperties.set(modCoreModuleFunction, arcLibraryVersion);
    }
}
