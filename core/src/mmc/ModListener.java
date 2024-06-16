package mmc;

import arc.ApplicationCore;
import arc.Core;
import arc.struct.Seq;
import mindustry.ClientLauncher;
import mindustry.Vars;

import static mmc.ModVars.*;

public class ModListener extends ApplicationCore {
    public static Seq<Runnable> updaters=new Seq<>();
    public static void addRun(Runnable runnable){
        updaters.add(runnable);
    }
    public static void load(){
//        Log.info("\n @",ui);
        listener=new ModListener();
        if(Vars.platform instanceof ClientLauncher){
            ((ClientLauncher)Vars.platform).add(listener);
            neededInit =false;
        }else {
            Core.app.addListener(listener);
        }
    }

    @Override
    public void dispose() {
        if (!loaded)return;
        super.dispose();
    }

    @Override
    public void pause() {
        if (!loaded)return;
        super.pause();
    }

    @Override
    public void resize(int width, int height) {
        if (!loaded)return;
        super.resize(width,height);
    }

    @Override
    public void resume() {
        if (!loaded)return;
        super.resume();
    }

    @Override
    public void setup() {
    }

    @Override
    public void init() {
        if (!loaded)return;
        super.init();
    }

    public void update() {
        if (!loaded)return;
        updaters.each(Runnable::run);
        super.update();
    }

}
