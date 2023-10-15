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
- [xstabux/Omaloon](https://github.com/xstabux/Omaloon)
- [Zelaux/ByteLogic](https://github.com/Zelaux/ByteLogic)
## Authors
- Zelaux
# PC Build Guide

1. Download IntelliJ IDEA.

2. Clone this repository.

3. When importing is end, go to IntelliJ console and type:

| Windows     | MacOSX & Linux |
|-------------|----------------|
| gradlew jar | ./gradlew jar  |

4. When compilation is end, your build will be in "build/libs"
Download
--------
## Gradle strict(better to use [gradle plugin](mindustryModGradlePlugin)):
(replace MODULE_NAME with the required module name)
```groovy
repositories{
    maven{ url  'https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository' }//repo for arc & mindustry
    maven{ url  'https://raw.githubusercontent.com/Zelaux/Repo/master/repository' }//repo for zelaux arc core
}

dependencies {
        implementation 'com.github.Zelaux.MindustryModCore:MODULE_NAME:VERSION'
}
```
