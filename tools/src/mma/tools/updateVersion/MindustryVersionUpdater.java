package mma.tools.updateVersion;

import arc.files.Fi;
import arc.func.Cons;
import arc.func.Cons2;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mma.tools.parsers.LibrariesDownloader;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MindustryVersionUpdater {
    static String mindustryVersion;
    static Seq<String> argsSeq;

    public static void main(String[] args) {
        argsSeq = Seq.with(args);
        mindustryVersion = argsSeq.find(s -> s.startsWith("v"));
        if (mindustryVersion == null) {
            System.out.println("Please put mindustry version in args!!!");
            System.exit(1);
            return;
        }
//git log --pretty=format:"%H:%s"

        LibrariesDownloader.download(mindustryVersion);

        runTask("Checking Anuke's comps for " + mindustryVersion,AnukeCompDownloader::run);

        runTask("ModPacker update",ModPackingUpdater::run);

        runTask("Annotations update",AnnotationsUpdater::run);

        createSpriteZip();

    }

    private static void runTask(String name, Cons2<String, String[]> task) {
        System.out.println(name);
        long nanos = System.nanoTime();
        task.get(mindustryVersion,argsSeq.toArray(String.class));
        System.out.println(Strings.format("Time taken: @s", Time.nanosToMillis(Time.timeSinceNanos(nanos)) / 1000f));
        System.out.println();

    }

    private static void createSpriteZip() {
        try {
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
                if (!fi.extension().equals("png") && !fi.extension().equals("jpg")) return;
                try {
                    writeZip(stream, fi, fi.absolutePath().substring(prefix.length()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            dir.walk(fiCons);
//            dir.emptyDirectory();
//            dir = dir.list()[0];
//            dir.walk(fiCons);

            stream.close();
            System.out.println(Strings.format("Time taken: @s", Time.nanosToMillis(Time.timeSinceNanos(nanos)) / 1000f));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeZip(ZipOutputStream stream, Fi fi, String name) throws IOException {
        stream.putNextEntry(new ZipEntry(name));
        stream.write(fi.readBytes());
        stream.closeEntry();
    }
}
