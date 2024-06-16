package mmc;

import groovy.lang.*;
import mmc.utils.*;
import org.gradle.api.*;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.*;

import static mmc.ClosureFactory.stringToStringClosure;
import static mmc.utils.MainUtils.findVersion;

public class MindustryModGradle implements Plugin<Project> {


    public static MavenArtifactRepository githubRepo(RepositoryHandler handler, String user, String repo) {
        return handler.maven(it1 -> {
            String url = "https://raw.githubusercontent.com/" + user + "/" + repo + "/master/repository";
            it1.setUrl(url);
        });
    }

    @Override
    public void apply(Project project) {
        ExtensionContainer extensions = project.getExtensions();
        final ExtraPropertiesExtension extraProperties = extensions.getExtraProperties();
        extraProperties.set("mindustryDefaultPath", ClosureFactory.fromSupplier(MainUtils::defaultMindustryPath));

        extensions.create("mindustryModCore", MindustryModCoreExtension.class, project);

//        addJarMindustry(project, extraProperties);
        /*String arcLibraryModule (String repo){
            //module path to full module repo
            if(repo.contains(':')) repo = repo.split(':').join("-")
            return "com.github.Zelaux.ArcLibrary:$repo:$arcLibraryVersion"
        }
        String arcModule (String repo){
        }*/

        RepositoryHandler repositories = project.getRepositories();
        GroovyObject repositoriesGroove = (GroovyObject) project.getRepositories();
        ExtensionAware repo= (ExtensionAware) repositories;
        repo.getExtensions().getExtraProperties().set("githubRepo", new ClosureFactory.SimpleClosure<MavenArtifactRepository>() {
            public MavenArtifactRepository doCall(String user, String repo) {
                return githubRepo(repositories,user,repo);
            }
        });
        /*try {
            Method method = MindustryModGradle.class.getMethod("githubRepo", RepositoryHandler.class, String.class, String.class);
            Decorator.decorateMethod(repositoriesGroove, method);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
//        repositoriesGroove.invokeMethod("githubRepo",new Object[]{"Zelaux","Repo"});
//        if (true)throw null;
//        repositories.maven(it -> {
//            it.setUrl("https://raw.githubusercontent.com/Anuken/MindustryMaven/master/repository");
//        });
        extraProperties.set("arcModule", stringToStringClosure(name -> {
            //skip to last submodule
            String[] split = name.split(":");
            name = split[split.length - 1];
            Object arcVersion = findVersion(extraProperties, "`arcVersion` or `mindustryVersion` is not specified", "mindustryVersion", "arcVersion");
            return "com.github.Anuken.Arc:" + name + ":" + arcVersion;
        }));
        extraProperties.set("mindustryModule", stringToStringClosure(name -> {
            //skip to last submodule
            String[] split = name.split(":");
            name = split[split.length - 1];
            Object version = findVersion(extraProperties, "`arcVersion` or `mindustryVersion` is not specified", "mindustryVersion", "arcVersion");
            return "com.github.Anuken.Mindustry:" + name + ":" + version;
        }));
    }
}
