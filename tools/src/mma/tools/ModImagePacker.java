package mma.tools;

import arc.files.Fi;
import arc.util.serialization.Json;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.MappableContent;
import mindustry.mod.Mods;
import mma.core.ModContentLoader;
import mma.gen.ModContentRegions;
import mma.gen.ModEntityMapping;
import mma.tools.gen.MindustryImagePacker;

public class ModImagePacker extends MindustryImagePacker {
    static Mods.ModMeta modMeta;

    public ModImagePacker() {
        try {
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        new ModImagePacker();
    }

    public static String full(String name) {
        return modMeta.name + "-" + name;
    }

    @Override
    protected void start() throws Exception {
        disableIconProcessing=true;

        Json json = new Json();
        Fi metaf = Fi.get("../../../../").child("mod.hjson");
        modMeta = json.fromJson(Mods.ModMeta.class, Jval.read(metaf.readString()).toString(Jval.Jformat.plain));

        Vars.headless = true;
        super.start();
        Vars.headless = false;
    }

    @Override
    protected void preCreatingContent() {
        super.preCreatingContent();
        ModEntityMapping.init();
    }

    @Override
    protected void runGenerators() {
        new ModGenerators();
    }

    @Override
    protected void load() {
        ModContentLoader.eachModContent(this::checkContent);
    }

    protected void checkContent(Content content) {
        if (content instanceof MappableContent) {
            ModContentRegions.loadRegions((MappableContent) content);
        }
    }
}
