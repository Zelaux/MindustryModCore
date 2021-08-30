package mma;

import arc.Core;
import arc.util.Log;
import arc.util.Strings;
import mindustry.ctype.ContentList;
import mindustry.ctype.UnlockableContent;
import mindustry.mod.ModListing;
import mindustry.mod.Mods;
import mma.core.ModUI;

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

    protected abstract void showException(Throwable ex);

    public static void modLog(String text, Object... args) {
        Log.info("[@] @", modInfo == null ? "braindustry-java" : modInfo.name, Strings.format(text, args));
    }
    public static String modName() {
        return modInfo == null ? "no name" : modInfo.name;
    }

    /**
     * Correct order:
     * new ModItems()
     * new ModStatusEffects()
     * new ModLiquids()
     * new ModBullets()
     * new ModUnitTypes()
     * new ModBlocks()
     * new ModPlanets()
     * new ModSectorPresets()
     * new ModTechTree()
     */
    public abstract ContentList[] getContentList();

    public abstract String getFullName(String name);

    public interface ThrowableRunnable {
        void run() throws Exception;
    }
}
