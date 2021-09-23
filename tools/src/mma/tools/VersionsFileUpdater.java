package mma.tools;

import arc.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class VersionsFileUpdater {
    public static void main(String[] args) {
        try {
            Process proc = Runtime.getRuntime().exec("git log --pretty=format:\"%H:%s\" -1");
            proc.waitFor();

//            String result = new String(.readNBytes(Integer.MAX_VALUE));
            String result = new BufferedReader(new InputStreamReader(proc.getInputStream())).readLine();
            Log.info("result :@", result);
//            proc.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        arc.files.Fi.get("versions").walk(fi -> {
            try {
                new URL("https://jitpack.io/com/github/Zelaux/ZelauxModCore/" + fi.readString() + "/build.log").openStream();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
    }
}
