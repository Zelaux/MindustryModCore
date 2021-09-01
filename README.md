ZelauxModCore([last version](lastVersion.txt))
[![](https://jitpack.io/v/Zelaux/ZelauxModCore.svg)](https://jitpack.io/#Zelaux/ZelauxModCore)
========
`ZelauxModCore` is a Mindustry java mod core for.
### Usage/Examples
###### Using annotations

## Mindustry Mod core By Zelaux

## Authors
- Zelaux
- 
# Build Guide
## PC

* 1.Download intelijIDEA.

* 2.Clone this repository.

* 3.When importing is end, go to Intelij console and type:

Windows      |  MacOSX       | Linux
------------ | ------------- | -------------
gradlew jar  | ./gradlew jar | ./gradlew jar

* 4.When compilation is end, your build will be in "build/libs"
Download
--------

Depend via Gradle:
```groovy
dependencies {
        implementation 'com.github.Zelaux.ZelauxModCore:-SNAPSHOT'
}
```

And don't forget to add the dependency to mod. (h).json
```hjson
dependencies: ["gas-library-java"]
```
