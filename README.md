ZelauxModCore([last version](versions/v133.txt))
[![](https://jitpack.io/v/Zelaux/ZelauxModCore.svg)](https://jitpack.io/#Zelaux/ZelauxModCore)
========
`ZelauxModCore` is the core of Mindustry's Java mod for easy modding.
### Modules
- annotations
- core
- tools
- plugins
### Usage/Examples
###### Using annotations
You can create annotations.propecties file in root directory and change some [setting](annotations/src/main/java/mma/annotations/AnnotationSetting.java "look at this enum")
## Authors
- Zelaux
# PC Build Guide

* 1.Download intelijIDEA.

* 2.Clone this repository.

* 3.When importing is end, go to Intelij console and type:

Windows      |  MacOSX       | Linux
------------ | ------------- | -------------
gradlew jar  | ./gradlew jar | ./gradlew jar

* 4.When compilation is end, your build will be in "build/libs"
Download
--------

Depend via Gradle(replace MODULE_NAME with the required module name):
```groovy
dependencies {
        implementation 'com.github.Zelaux.ZelauxModCore:MODULE_NAME:-SNAPSHOT'
}
```
