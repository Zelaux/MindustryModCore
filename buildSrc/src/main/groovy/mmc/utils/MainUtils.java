package mmc.utils;

import mmc.*;
import org.gradle.api.*;
import org.gradle.api.plugins.*;
import org.jetbrains.annotations.*;

public class MainUtils{

    public static String defaultMindustryPath(){
        String mindustryDataDir = System.getenv("MINDUSTRY_DATA_DIR");
        if(mindustryDataDir != null) return mindustryDataDir;
        return MockOS.getAppDataDirectoryString("Mindustry");
    }


    @NotNull
    public static Object findVersion(ExtraPropertiesExtension extraProperties, String message, String... versionNames){
        Object foundVersion = null;
        for(String versionName : versionNames){
            if(extraProperties.has(versionName)){
                foundVersion = extraProperties.get(versionName);
            }
            if(foundVersion != null) break;
        }
        if(foundVersion == null){
            throw new IllegalArgumentException(message);
        }
        return foundVersion;
    }

    public static MindustryModCoreExtension getMyExtension(Project project, Project fallback){
        MindustryModCoreExtension myExtension = project.getExtensions().findByType(MindustryModCoreExtension.class);
        if(myExtension != null) return myExtension;
        return fallback.getExtensions().getByType(MindustryModCoreExtension.class);
    }
}
