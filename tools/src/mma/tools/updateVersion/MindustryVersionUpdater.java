package mma.tools.updateVersion;

import arc.struct.Seq;
import arc.util.Log;
import mma.tools.parsers.LibrariesDownloader;

public class MindustryVersionUpdater {
    public static void main(String[] args) {
        Seq<String> argsSeq = Seq.with(args);
        String mindustryVersion = argsSeq.find(s -> s.startsWith("v"));
        if (mindustryVersion == null) {
            System.out.println("Please put mindustry version in args!!!");
            System.exit(1);
            return;
        }
        LibrariesDownloader.download(mindustryVersion);
        AnukeCompDownloader.run(mindustryVersion,args);
        ModPackingUpdater.run(mindustryVersion,args);
    }
}
