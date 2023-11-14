package mmc;

import arc.*;
import arc.util.Log;
import arc.util.Strings;
import mindustry.*;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.*;
import mindustry.mod.Mods;
import mmc.core.ModUI;

public abstract class ModVars {
    public static Mods.LoadedMod modInfo;
    public static boolean loaded = false;
    public static boolean packSprites;
    public static boolean neededInit=true;
    static ModVars instance;
//    public static ModNetClient netClient;
//    public static ModNetServer netServer;
//    public static ModUI modUI;
//    public static ModLogic logic;
    public static ModListener listener;
//    public static MMAMod mod;

    public ModVars() {
        instance = this;
        instance.onLoad(ModListener::load);
    }

    protected abstract void onLoad(Runnable runnable);

    public static ModVars instance() {
        return instance;
    }

    public static void checkTranslate(UnlockableContent content) {
        content.localizedName = Core.bundle.get(content.getContentType() + "." + content.name + ".name", content.localizedName);
        content.description = Core.bundle.get(content.getContentType() + "." + content.name + ".description", content.description);
        content.details = Core.bundle.get(content.getContentType() + "." + content.name + ".details", content.details);
    }

    public static String fullName(String name) {
        if (packSprites) return name;
        if (modInfo == null) throw new IllegalArgumentException("modInfo cannot be null");
        return Strings.format("@-@", modInfo.name, name);
    }

    public static void inTry(ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            instance.showException(ex);
//            showException(ex);
        }
    }

    protected void showException(Throwable ex){
        if(Vars.headless){
            Events.run(ServerLoadEvent.class,()->ex.printStackTrace());
        }else{
            Events.run(ClientLoadEvent.class,()->ModUI.showExceptionDialog(ex));
        }
    }

    public static void modLog(String text, Object... args) {
        String prefix = modInfo==null?instance.getClass().getPackage().getName():modInfo.name;
        Log.info("[@] @", prefix, Strings.format(text, args));
    }
    public static String modName() {
        return modInfo == null ? "no name" : modInfo.name;
    }

    /**
     * Correct order:
     * ModItems.load()
     * ModStatusEffects.load()
     * ModLiquids.load()
     * ModBullets.load()
     * ModUnitTypes.load()
     * ModBlocks.load()
     * ModPlanets.load()
     * ModSectorPresets.load()
     * ModTechTree.load()
     */
    public abstract void loadContent();

//    public abstract String getFullName(String name);

    public interface ThrowableRunnable {
        void run() throws Exception;
    }
}
