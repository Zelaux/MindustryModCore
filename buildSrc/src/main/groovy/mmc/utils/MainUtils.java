package mmc.utils;

import mmc.*;
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
            foundVersion = extraProperties.get(versionName);
            if(foundVersion != null) break;
        }
        if(foundVersion == null){
            throw new IllegalArgumentException(message);
        }
        return foundVersion;
    }

}
