1. Go to `/settings.gradle` and add these lines:
   ```gradle
   pluginManagement{
       repositories{
           gradlePluginPortal()
           maven{url 'https://www.jitpack.io'}
       }
   }
   
   if(JavaVersion.current().ordinal() < JavaVersion.VERSION_17.ordinal()){
       throw new GradleException("JDK 17 is a required minimum version. Yours: ${System.getProperty('java.version')}")
   }
   ```
   This is done so that Gradle can find this plugin, and to enforce usage of Java 17+ for compiling.
2. Go to `/gradle.properties` and add these lines:
   ```properties
   mindustryVersion = v146
   #same as mindustryVersion
   arcVersion = v146
   #latest release of Zelaux/MindustryModCore repository.
   modCoreVersion = v2.0.0

   #used to decrease compile-time penalty and not use the entire Kotlin JVM standard libraries, because they're literally pointless in this context.*/
   kapt.include.compile.classpath = false
   kotlin.stdlib.default.dependency = false
   ``` 
3. Go to `/gradle.properties`, and in the property `org.gradle.jvmargs`, replace `--add-exports` with `--add-opens` and remove `--illegal-access=permit` so it looks like below:
   ```properties
   org.gradle.jvmargs = \
   --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
   --add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
   --add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED \
   --add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED \
   --add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED \
   --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
   --add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
   --add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
   --add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED \
   --add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED \
   --add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED \
   --add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED
   ```
   This is done to grant necessary internal API accesses for the annotation processor.
4. Go to `/build.gradle` and replace this line:
   ```gradle
   apply plugin: "java"
   ```
   With these:
   ```gradle
   plugins{
       id 'java'
       id 'com.github.Zelaux.MindustryModCore' version "$modCoreVersion"
   }
   ```
   This is the core part of the usage.
5. Go to `/build.gradle` and replace these lines:
   ```gradle
   targetCompatibility = 8
   sourceCompatibility = JavaVersion.VERSION_16
   ```
   With these:
   ```gradle
   sourceCompatibility = 17
   tasks.withType(JavaCompile).configureEach{
       sourceCompatibility = 17
       options.release = 8

       options.incremental = true
       options.encoding = 'UTF-8'
   }
   ```
   This is to allow compiling with Java 17 syntaxes while targeting Java 8 bytecodes.
6. Go to `/build.gradle` and replace these lines:
   ```gradle
   ext{
       //the build number that this mod is made for
       mindustryVersion = 'v145'
       jabelVersion = "93fde537c7"
       sdkRoot = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
   }

   //java 8 backwards compatibility flag
   allprojects{
       tasks.withType(JavaCompile){
           options.compilerArgs.addAll(['--release', '8'])
       }
   }
   ```
   With these:
   ```gradle
   ext{
       sdkRoot = System.getenv("ANDROID_SDK_ROOT") ?: System.getenv("ANDROID_HOME")
   }
   ```
7. Go to `/build.gradle` and replace these lines:
   ```gradle
   dependencies{
       compileOnly "com.github.Anuken.Arc:arc-core:$mindustryVersion"
       compileOnly "com.github.Anuken.Mindustry:core:$mindustryVersion"
   
       annotationProcessor "com.github.Anuken:jabel:$jabelVersion"
   }
   ```
   With these (without the comments, of course):
   ```gradle
   dependencies{
       compileOnly "com.github.Anuken.Arc:arc-core:$arcVersion" //i
       compileOnly "com.github.Anuken.Mindustry:core:$mindustryVersion" //i

       annotationProcessor "com.github.Anuken:jabel:$jabelVersion" //ii
   
       compileOnly "com.github.Zelaux.MindustryModCore:annotations:$modCoreVersion" //iii
       kapt "com.github.Zelaux.MindustryModCore:annotations:$modCoreVersion" //iv
   }
   ```
    1. Adds `Mindustry` and `Arc` as a compile classpath.
    2. Lets you use Java 9+ syntaxes while still targeting Java 8 bytecode (which is necessary), mostly because Java is stupid.
    3. Adds the annotation processor classpath into your project, without bundling them into the final `.jar`.
    4. Registers the annotation processor to the compiler. _Why KAPT?_ Because KAPT is fast and generally friendly to incremental compilation, especially if your project is decoupled into several modules (like [Confictura](https://github.com/GlennFolker/Confictura)).
8. Go to `/build.gradle` and remove these lines:
   ```gradle
   //force arc version
   configurations.all{
       resolutionStrategy.eachDependency { details ->
           if(details.requested.group == 'com.github.Anuken.Arc'){
               details.useVersion "$mindustryVersion"
           }
       }
   }
   ```
