package mma.tools;

import arc.files.Fi;
import arc.util.Log;
import arc.util.Structs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

public class VersionsFileUpdater {
    public static void main(String[] args) throws Exception {
        Fi versions = Fi.get("versions");

        Process proc = Runtime.getRuntime().exec("git log --pretty=format:\"%H:%s\" -1");
        proc.waitFor();

//            String result = new String(.readNBytes(Integer.MAX_VALUE));
        String result = new BufferedReader(new InputStreamReader(proc.getInputStream())).readLine();
        if (result.matches("[\"].*[\"]")){
            result=result.substring(1,result.length()-1);
        }
        String version = Structs.find(args, v -> v.startsWith("v"));
        if (version == null) {
            throw new RuntimeException("cannot find version from " + Arrays.toString(args));
        }
        String substring = result.substring(0, 11);
        Log.info("result(@), version(@)",result, substring);
        versions.child(version + ".txt").writeString(substring);
        new URL("https://jitpack.io/com/github/Zelaux/ZelauxModCore/" +substring + "/build.log").openStream();
    }
}
