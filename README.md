ZelauxModCore([last version](versions/lastVersion.txt))
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
You can create `annotations.propecties` file in root directory and change some [settings](annotations/src/main/java/mmc/annotations/AnnotationSetting.java "look at this enum")
### Guide to using the [ZelauxMindustryModTemplate](https://github.com/Zelaux/ZelauxMindustryModTamplate) Guide
- Replace the `YourMod` prefix for all classes with the name of your mod.
- Set your `classPrefix` for generated classes in `annotations.properties`
- If you want a package like `com.company.CompanyName` you must add line bellow with our package name to the `annotations.properties`
```properties
rootPackage=YOUR_PACKAGE_NAME
```
- Do not forget to change package in `tools/build.gradle`(the variable named `imagePackerPath`)
## Authors
- Zelaux
# PC Build Guide

* 1.Download IntelliJ IDEA.

* 2.Clone this repository.

* 3.When importing is end, go to IntelliJ console and type:

| Windows     | MacOSX        | Linux         |
|-------------|---------------|---------------|
| gradlew jar | ./gradlew jar | ./gradlew jar |

* 4.When compilation is end, your build will be in "build/libs"
Download
--------

Depend via Gradle(replace MODULE_NAME with the required module name):
```groovy
dependencies {
        implementation 'com.github.Zelaux.ZelauxModCore:MODULE_NAME:VERSION'
}
```
