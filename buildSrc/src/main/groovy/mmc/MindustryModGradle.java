package mmc;

import groovy.lang.*;
import mmc.utils.*;
import org.gradle.api.*;
import org.gradle.api.plugins.*;
import org.jetbrains.annotations.*;

import java.util.function.*;

import static mmc.ClosureFactory.stringToStringClosure;
import static mmc.utils.MainUtils.findVersion;

public class MindustryModGradle implements Plugin<Project>{


    @Override
    public void apply(Project project){
        ExtensionContainer extensions = project.getExtensions();
        final ExtraPropertiesExtension extraProperties = extensions.getExtraProperties();
        extraProperties.set("mindustryDefaultPath", ClosureFactory.fromSupplier(MainUtils::defaultMindustryPath));

        extensions.create("mindustryModCore", MindustryModCoreExtension.class, project);

//        addJarMindustry(project, extraProperties);
        /*String arcLibraryModule (String name){
            //module path to full module name
            if(name.contains(':')) name = name.split(':').join("-")
            return "com.github.Zelaux.ArcLibrary:$name:$arcLibraryVersion"
        }
        String arcModule (String name){
        }*/
        project.getRepositories().maven(it -> {
            it.setUrl("https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository");
        });
        extraProperties.set("arcModule", stringToStringClosure(name -> {
            //skip to last submodule
            String[] split = name.split(":");
            name = split[split.length - 1];
            Object arcVersion = findVersion(extraProperties, "`arcVersion` or `mindustryVersion` is not specified", "mindustryVersion", "arcVersion");
            return "com.github.Anuken.Arc:" + name + ":" + arcVersion;
        }));
        extraProperties.set("mindustryModule", stringToStringClosure(name -> {
            //skip to last submodule
            String[] split = name.split(":");
            name = split[split.length - 1];
            Object version = findVersion(extraProperties, "`arcVersion` or `mindustryVersion` is not specified", "mindustryVersion", "arcVersion");
            return "com.github.Anuken.Mindustry:" + name + ":" + version;
        }));
    }
}
