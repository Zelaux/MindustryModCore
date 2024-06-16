# MindustryModGradle Gradle Plugin

- [Usage](#usage)
- [Property Configurations](#properties)
- [task jarMindustry](#jarMindustry)
- [mindustryModCore extension](#extension)
  - [addZelauxCore](#extension_addMindustryModCore)
  - [addArcLibrary](#extension_addArcLibrary)
  - [setupAnnotationProject](#extension_setupAnnotationProject)
  - [addKaptAnnotations](#extension_addKaptAnnotations)
- [repositiories](#custom-repositories)
  - [githubRepo](#repos_githubRepo)

## <a name="usage"></a> Usage
#### How to add plugin to your mod
For project that forked from [Anuke's mod template](https://github.com/Anuken/MindustryJavaModTemplate) use [this guide](FromAnuken.md)
#### Then:
- Add this property block in `/build.gradle` wherever you like (as long as it's done in project evaluation, that is):
   ```gradle
   mindustryModCore{
       //...
   }
   ```
    Recommended to set [project info](#extensions_projectInfo)<br/>
   [More information about extension](#extensions)
    
- Add the line `EntityRegistry.register();` (from the `genPackage`) in your mod class' `loadContent()` method.
- Refer to [usage](/USAGE.md) for more detailed entity annotation usages.
- Compile and use the mod as the guide in the mod template says.


----

## <a name="properties"></a> Property Configuration
Configuration in `gradle.properties` 
- ### `mmc.tasks.jarMindustry`(default `jarMindustry`)
   
   changes name for [task jarMindustry](#jarMindustry) 
- ### `mmc.functions.mindustryModCore`(default `modCoreModule`)

    changes name for function `modCoreModule(String module)`(see [addMindustryModCore](#extension_addMindustryModCore)) 
- ### `mmc.disableKotlinTasks`(default `false`)
    make build faster by disabling kotlin compile tasks
    <br/>**WARNING: do not set to `true` in mods that uses kotlin**
----

## <a name="jarMindustry"></a> task jarMindustry

depends on jar.
Copies a file to mindustry folders specified in `outputDirectories.txt`(each line is separated folder) file.
If file not exists copies in default mindustry path.

`classic` - default mindustry path
### Comments
Comments starts with `#` or `//`
### Example `outputDirectories.txt`
```
C:\Users\Zelaux\Desktop\Mindustry\server\config\mods
#C:\Users\Zelaux\Desktop\Mindustry\client\mods
classic
```

----

## <a name="extensions"></a> `mindustryModCore` extension
- ###  <a name="extensions_projectInfo"></a>`projectInfo{ }`
- `rootDirectory` - directory from which other paths are calculated.
- `assertsPath` - path to your `assets` folder from `rootDirectory`.
- `assertsRawPath` - path to your `assets-raw` folder from `rootDirectory`.
- `modInfoPath` - path to your `mod.(h)json` or `plugin.(h)json` from `rootDirectory`.
- `rootPackage` - your main package.
- `revisionsPath` - path to store revision for entity generation.
- `classPrefix` - sets class prefix for some generated class like `Call`, `EntityMapping`, etc.
    ##### Example from [xstabux/Omaloon](https://github.com/xstabux/Omaloon):
    ```gradle
    mindustryModCore{
        projectInfo{
            rootDirectory = rootDir
            assetsPath = "assets"
            assetsRawPath = "assets"
            rootPackage = "omaloon"
            modInfoPath = "mod.json"
            revisionsPath = "revisions"
            classPrefix = "OL"
        }
    }
    ```
- ### <a name="extensions_addZelauxCore"></a>`addMindustryModCore()`
  - adds repository for [MindustryModCore](https://github.com/Zelaux/ArcLibrary)
  - adds method `modCoreModule(String module)`
  <h5>Example:</h5>
  
    ```gradle
        mindustryModCore{
            addMindustryModCore()
        }  
        dependencies{
            implementation modCoreModule("core")
            implementation modCoreModule("utils")
            implementation modCoreModule("graphics")
        }
    ```
- ### <a name="extension_addArcLibrary"></a>`addArcLibrary()`
  - adds repository for [ArcLibrary](https://github.com/Zelaux/ArcLibrary)
  - adds method `arcLibraryModule(String module)`
  <h5>Example:</h5>
  
    ```gradle
        mindustryModCore{
            addArcLibrary()
        }  
        dependencies{
            implementation arcLibraryModule("utils-io")
            implementation arcLibraryModule("graphics-dashDraw")
        }
    ```
- ### <a name="extension_setupAnnotationProject"></a>`setupAnnotationProject()`

    Prepare project to write AnnotationProcessors 
    - Adds task `writeAnnotationProcessors`<br>invokes when you try to compile this project(creates `META-INF/services/javax.annotation.processing.Processor`)
- ### <a name="extension_addKaptAnnotations"></a>`addKaptAnnotations()`
  Prepare project to use kapt annotation processors
    <h5>Example</h5>
    
    ```gradle
    mindustryModCore{
        addMindustryModCore()
        addKaptAnnotations()
    }
    dependencies{
        kapt modCoreModule("annotations")
        kapt project(":annotations")
    }
    ```
----

## <a name="repos">Custom repositories</a> 
- ###  <a name="repos_githubRepo"></a>
  Added custom repository for maven repositories on github
  <h5>Example:</h5>

  ```gradle
  repositories{
    githubRepo("Anuken","MindustryMaven")
    githubRepo("Zelaux","Repo")
  }
  ```


----
