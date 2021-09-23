package mma.tools;

import arc.files.Fi;
import arc.util.Log;
import arc.util.Structs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

public class VersionsFileUpdater {
    public static void main(String[] args) throws Exception {
        Fi versions = Fi.get("versions");

        Process proc = Runtime.getRuntime().exec("git log --pretty=format:\"%H:%s\" -1");
        proc.waitFor();

//            String result = new String(.readNBytes(Integer.MAX_VALUE));
        String result = new BufferedReader(new InputStreamReader(proc.getInputStream())).readLine();
        String version = Structs.find(args, v -> v.startsWith("v"));
        if (version == null) {
            throw new RuntimeException("cannot find version from " + Arrays.toString(args));
        }
        versions.child(version+".txt").writeString(result.substring(0,6));
//        Log.info("result :@", result);

        versions.walk(fi -> {
            try {
                new URL("https://jitpack.io/com/github/Zelaux/ZelauxModCore/" + fi.readString() + "/build.log").openStream();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
    }
}
