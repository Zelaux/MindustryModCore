package mma.tools;

import arc.util.Log;

import java.io.IOException;
import java.net.URL;

public class VersionsFileUpdater {
    public static void main(String[] args) {
        try {
            //здесь "sleep 15" и есть ваша консольная команда
            Process proc = Runtime.getRuntime().exec("git log --pretty=format:\"%H:%s\" -1");
            proc.waitFor();
            String result = new String(proc.getInputStream().readAllBytes());
            Log.info("result :@",result);
//            proc.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        arc.files.Fi.get("versions").walk(fi->{
            try {
                new URL("https://jitpack.io/com/github/Zelaux/ZelauxModCore/" + fi.readString() + "/build.log").openStream();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
    }
}
