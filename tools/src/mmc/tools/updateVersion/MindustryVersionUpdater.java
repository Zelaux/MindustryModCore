package mmc.tools.updateVersion;

import arc.files.*;
import arc.func.*;
import arc.struct.*;
import arc.util.*;
import mmc.tools.parsers.*;

import java.io.*;
import java.util.zip.*;

public class MindustryVersionUpdater{
    static String mindustryVersion;
    static String arcVersion;
    static Seq<String> argsSeq;

    public static void main(String[] args){
        argsSeq = Seq.with(args);
        mindustryVersion = argsSeq.find(s -> s.startsWith("v_"));
        arcVersion = argsSeq.find(s -> s.startsWith("arc_"));
        if(mindustryVersion == null){
            System.out.println("Please put mindustry version in args!!!");
            System.exit(1);
            return;
        }
        mindustryVersion = mindustryVersion.substring("v_".length());
        arcVersion = arcVersion != null ? arcVersion.substring("arc_".length()) : mindustryVersion;
//git log --pretty=format:"%H:%s"

        LibrariesDownloader.download(mindustryVersion, arcVersion);

        runTask("Checking Anuke's comps for " + mindustryVersion, AnukeCompDownloader::run);

        createSpriteZip();
        runTask("ModPacker update", ModPackingUpdater::run);

        runTask("Annotations update", AnnotationsUpdater::run);
//        runTask("Entity groups update", EntityGroupsUpdater::run);


    }

    private static void runTask(String name, Cons2<String, String[]> task){
        System.out.println(name);
        long nanos = System.nanoTime();
        task.get(mindustryVersion, argsSeq.toArray(String.class));
        System.out.println(Strings.format("Time taken: @s", Time.nanosToMillis(Time.timeSinceNanos(nanos)) / 1000f));
        System.out.println();

    }

    private static void createSpriteZip(){
        try{
            System.out.println("Creating mindustrySprites.zip");
            long nanos = System.nanoTime();
            ZipOutputStream stream = new ZipOutputStream(Fi.get("core/mindustrySprites.zip").write());
            Fi dir = Fi.tempDirectory("tmp/mindustrySprites");
//            Fi dir ;
            LibrariesDownloader.coreZip().list()[0].child("core").child("assets-raw").child("sprites").copyTo(dir);
            LibrariesDownloader.coreZip().list()[0].child("core").child("assets").child("sprites").copyTo(dir);
            dir = dir.list()[0];
            String prefix = dir.absolutePath() + "/";

            Cons<Fi> fiCons = fi -> {
                if(!fi.extension().equals("png") && !fi.extension().equals("jpg")) return;
                try{
                    writeZip(stream, fi, fi.absolutePath().substring(prefix.length()));
                }catch(Exception e){
                    throw new RuntimeException(e);
                }
            };
            dir.walk(fiCons);
//            dir.emptyDirectory();
//            dir = dir.list()[0];
//            dir.walk(fiCons);

            stream.close();
//            Fi.get("core/mindustrySprites.zip").copyTo(Fi.get("tools/src/mma/tools/gen/mindustrySprites.zip"));
            System.out.println(Strings.format("Time taken: @s", Time.nanosToMillis(Time.timeSinceNanos(nanos)) / 1000f));
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private static void writeZip(ZipOutputStream stream, Fi fi, String name) throws IOException{
        stream.putNextEntry(new ZipEntry(name));
        stream.write(fi.readBytes());
        stream.closeEntry();
    }
}
