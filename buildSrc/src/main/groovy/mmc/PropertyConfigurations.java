package mmc;

import org.gradle.api.*;

public interface PropertyConfigurations{
    PropertyConfiguration jarMindustryTaskName = prop("mmc.tasks.jarMindustry", "jarMindustry");
    PropertyConfiguration disableKotlinTasks = prop("mmc.disableKotlinTasks", "false");
    PropertyConfiguration zelauxModCoreName = prop("mmc.functions.mindustryModCore", "modCoreModule");

    static PropertyConfiguration prop(String name, String defaultValue){
        return new PropertyConfiguration(name, defaultValue);
    }

    class PropertyConfiguration{
        private final String name;
        private final String defaultValue;

        private PropertyConfiguration(String name, String defaultValue){
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public String get(Project project){
            return String.valueOf(project
                .getExtensions()
                .getExtraProperties()
                .getProperties()
                .getOrDefault(name, defaultValue));
        }
    }
}

