# MindusryModCore

---
`MindusryModCore` is the core of Mindustry's Java mod for easy modding.
### Modules
- [annotations](annotations)
- [blocks](blocks)
- [core](core)
- [customBlockShape](customBlockShape)
- [graphics](graphics)
- [gradle plugin](mindustryModGradlePlugin)
- [plugins](plugins)
- [tiledStructured](tiledStructured)
- [tools](tools)
- [utils](utils)
### Mods that uses MindustryModCore
- [internal test mod](tests)
- [Zelaux/ByteLogic](https://github.com/Zelaux/ByteLogic)
## Authors
- [Zelaux](https://github.com/Zelaux), [nekit508](https://github.com/nekit508)
--------
## Gradle strict(better to use [gradle plugin](mindustryModGradlePlugin)):
(replace MODULE_NAME with the required module name)
```groovy
repositories{
    maven{ url  'https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository' }//repo for Arc & Mindustry
    maven{ url  'https://raw.githubusercontent.com/Zelaux/Repo/master/repository' }//repo for MindustryModCore & ArcLibrary
}

dependencies {
        implementation 'com.github.Zelaux.MindustryModCore:MODULE_NAME:VERSION'
}
```
