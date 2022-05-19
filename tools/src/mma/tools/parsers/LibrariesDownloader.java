package mma.tools.parsers;

import arc.files.*;
import arc.struct.*;
import arc.util.*;
import mma.tools.*;
import org.apache.commons.io.*;
import org.eclipse.jgit.api.CreateBranchCommand.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;

import java.io.*;
import java.net.*;

public class LibrariesDownloader{
    public static Fi core(){
        return Fi.get("compDownloader").child("sources.zip");
    }

    public static Fi arc(){
        return Fi.get("compDownloader").child("arcSources.zip");
    }

    public static ZipFi coreZip(){
        return new ZipFi(core());
    }

    public static ZipFi arcZip(){
        return new ZipFi(arc());
    }

    public static Fi coreZipRoot(){
        return new ZipFi(core()).list()[0];
    }

    public static Fi arcZipRoot(){
        return new ZipFi(arc()).list()[0];
    }

    public static void download(String mindustryVersion, String argVersion){
        boolean downloadNew = false;
        try{

            Fi version = new Fi("compDownloader").child("version.txt");
            Fi sourcesFi = core();
            Fi arcFi = arc();
            if(!version.exists() || !version.readString().equals(mindustryVersion)){
                downloadNew = true;
                version.writeString(mindustryVersion);
            }
            boolean errorInSources = false, errorInArc = false;
            try{
                coreZip();
            }catch(Exception e){
                errorInSources = true;
            }
            try{
                arcZip();
            }catch(Exception e){
                errorInArc = true;
            }
            if(downloadNew || !sourcesFi.exists() || errorInSources){
                Log.info("Downloading new core version");
                Time.mark();
                FileUtils.copyURLToFile(new URL("https://codeload.github.com/Anuken/Mindustry/zip/refs/tags/" + mindustryVersion), sourcesFi.file(), 10000, 10000);
                Log.info("Time to download: @ms", Time.elapsed());
            }else{
                Log.info("Game version and core version are the same");
            }
            if(downloadNew || !arcFi.exists() || errorInArc){
                Log.info("Downloading new arc version");
                Time.mark();
                FileUtils.copyURLToFile(new URL("https://codeload.github.com/Anuken/Arc/zip/refs/tags/" + argVersion), arcFi.file(), 10000, 10000);
                Log.info("Time to download: @ms", Time.elapsed());
            }else{
                Log.info("Game version and arc version are the same");
            }
        }catch(IOException e){
            Log.err(e);
        }
    }

    public static void downloadV7(String mindustryVersion, String arcVersion){
        boolean downloadNew = false;
        boolean shouldWriteFile = false;
        try{

            Fi version = new Fi("compDownloader").child("version.txt");
            Fi sourcesFi = core();
            Fi arcFi = arc();
            if(!version.exists() || !version.readString().equals(mindustryVersion)){
                downloadNew = shouldWriteFile = true;
            }
            boolean errorInSources = false, errorInArc = false;
            try{
                coreZip();
            }catch(Exception e){
                errorInSources = true;
            }
            try{
                arcZip();
            }catch(Exception e){
                errorInArc = true;
            }
            if(downloadNew || !sourcesFi.exists() || errorInSources){
                Log.info("Downloading new core version");
                Time.mark();
                Fi directory = sourcesFi.sibling("mindustry");
                directory.deleteDirectory();
                downloadV7Mindustry(directory, "https://github.com/Anuken/MindustryJitpack.git", mindustryVersion,true);

                System.out.println("Zipping folder");
                ZipTools.makeZip(directory, sourcesFi);
                directory.deleteDirectory();
                directory.delete();
                directory.deleteDirectory();

//                String url1 = "https://jitpack.io/com/github/anuken/mindustryjitpack/core/" + mindustryVersion + "/core-" + mindustryVersion + "-sources.jar";
//                System.out.println(url1);
//                FileUtils.copyURLToFile(new URL(url1), sourcesFi.file(), 10000, 10000);
//                FileUtils.copyURLToFile(new URL("https://jitpack.io/com/github/anuken/mindustryjitpack/core/" + mindustryVersion + "/core-" + mindustryVersion + ".jar"), sourcesFi.sibling("sources-jar.zip").file(), 10000, 10000);
                Log.info("Time to download: @ms", Time.elapsed());
            }else{
                Log.info("Game version and core version are the same");
            }
            if(downloadNew || !arcFi.exists() || errorInArc){
                Log.info("Downloading new arc version");
                Time.mark();
                Fi directory = sourcesFi.sibling("arc");
                directory.deleteDirectory();
                downloadV7Mindustry(directory, "https://github.com/Anuken/Arc.git", arcVersion,false);

                System.out.println("Zipping folder");
                ZipTools.makeZip(directory, arcFi);
                directory.deleteDirectory();
                directory.delete();
                directory.deleteDirectory();
//                downloadV7Mindustry(directory, "https://github.com/Anuken/MindustryJitpack.git", mindustryVersion);
//                FileUtils.copyURLToFile(new URL("https://codeload.github.com/Anuken/Arc/zip/refs/tags/" + arcVersion), arcFi.file(), 10000, 10000);
                Log.info("Time to download: @ms", Time.elapsed());
            }else{
                Log.info("Game version and arc version are the same");
            }
            if(shouldWriteFile){
                version.writeString(mindustryVersion);
            }
        }catch(Exception e){
            throw new RuntimeException(e);
//            Log.err(e);
        }
    }

    private static void downloadV7Mindustry(Fi directory, String repository, String commitHash, boolean equalsHash){
        try{
            directory.deleteDirectory();
            System.out.println("Cloning repository");
            Git git = Git.cloneRepository().setURI(repository)/*.setBranch("main")*/.setDirectory(directory.file()).call();
            System.out.println("Collecting commits");
            Seq<RevCommit> commits = new Seq<>();
            for(RevCommit commit : git.log().all().call()){
                commits.addAll(commit);
//              System.out.print(new Date(commit.getCommitTime()).toString());
//              System.out.print("(");
//              System.out.print(commit.getCommitTime());
//              System.out.print("): ");
//              System.out.print(commit.getFullMessage());
//              System.out.print("(");
//              System.out.print(commit.name());
//              System.out.println(")");
            }

            //                You have to use setCreateBranch to create a branch:
            RevCommit startPoint = commits.find(r -> equalsHash ? r.name().equals(commitHash) : r.name().startsWith(commitHash));

            Ref ref = git.checkout().
            setCreateBranch(true).
            setName("branchName").
            setUpstreamMode(SetupUpstreamMode.TRACK).
            setStartPoint(startPoint).
            call();
        }catch(Exception e){
            throw new RuntimeException(e);
        }

//                git.checkout().setForce(true).addPath("test").setCreateBranch(true).setName("test").setStartPoint(startPoint).call();

        /*git.checkout().setCreateBranch(true).setName("test").call();*/
    }
}
