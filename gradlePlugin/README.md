# MindustryModGradle Gradle Plugin

- [Usage](#usage)
- [Property Configurations](#properties)
- [task jarMindustry](#jarMindustry)
- [mindustryModCore extension](#extension)
  - [addZelauxCore](#extension_addZelauxCore)
  - [addArcLibrary](#extension_addArcLibrary)
  - [setupAnnotationProject](#extension_setupAnnotationProject)
  - [addKaptAnnotations](#extension_addKaptAnnotations)

## <a name="usage"></a> Usage

Coming Soon

[//]: # (TODO)

----

## <a name="properties"></a> Property Configuration

- `mmc.tasks.jarMindustry`(default `jarMindustry`)
   
   changes name for [task jarMindustry](#jarMindustry) 
- `mmc.functions.zelauxModCore`(default `modCoreModule`)

    changes name for function `modCoreModule(String module)`(see [addZelauxCore](#extension_addZelauxCore)) 

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

- <a name="extensions_addZelauxCore"></a>`addMindustryModCore()`
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
- <a name="extension_addArcLibrary"></a>`addArcLibrary()`
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
- <a name="extension_setupAnnotationProject"></a>`setupAnnotationProject()`

    Prepare project to write AnnotationProcessors 
    - Adds task `writeAnnotationProcessors`<br>invokes when you try to compile this project(creates `META-INF/services/javax.annotation.processing.Processor`)
- <a name="extension_addKaptAnnotations"></a>`addKaptAnnotations()`
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