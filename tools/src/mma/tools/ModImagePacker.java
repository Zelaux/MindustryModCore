package mma.tools;

import arc.util.serialization.Json;
import arc.util.serialization.Jval;
import mindustry.ctype.Content;
import mma.ModVars;
import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import mma.core.ModContentLoader;
import mma.gen.ModContentRegions;
import mma.gen.ModEntityMapping;
import mindustry.Vars;
import mindustry.ctype.MappableContent;
import mindustry.ctype.UnlockableContent;
import mindustry.mod.Mods;
import mindustry.tools.ImagePacker;
import mma.tools.gen.MindustryImagePacker;

public class ModImagePacker extends MindustryImagePacker {
    static Mods.ModMeta modMeta;

    public ModImagePacker() {
        Json json = new Json();
        Fi metaf = Fi.get("../../../../").child("mod.hjson");
        modMeta = json.fromJson(Mods.ModMeta.class, Jval.read(metaf.readString()).toString(Jval.Jformat.plain));

        Vars.headless = true;
        ArcNativesLoader.load();

        ModVars.packSprites = true;

        Log.logger = new Log.NoopLogHandler();
        Vars.content = new ModContentLoader();
        init();
//        Vars.content.createBaseContent();
        Vars.content.createModContent();
        ModContentLoader.eachModContent(this::checkContent);

        Log.logger = new Log.DefaultLogHandler();
        Fi.get("../../../assets-raw/sprites_out").walk((path) -> {
            if (path.extEquals("png")) {
                cache.put(path.nameWithoutExtension(), new PackIndex(path));
            }
        });
        Core.atlas = new TextureAtlas() {
            @Override
            public AtlasRegion find(String name) {
                if (!cache.containsKey(name)) {
                    GenRegion region = new GenRegion(name, null);
                    region.invalid = true;
                    return region;
                }

                PackIndex index = cache.get(name);
                if (index.pixmap == null) {
                    index.pixmap = new Pixmap(index.file);
                    index.region = new GenRegion(name, index.file) {{
                        width = index.pixmap.width;
                        height = index.pixmap.height;
                        u2 = v2 = 1f;
                        u = v = 0f;
                    }};
                }
                return index.region;
            }

            @Override
            public AtlasRegion find(String name, TextureRegion def) {
                if (!cache.containsKey(name)) {
                    return (AtlasRegion) def;
                }
                return find(name);
            }

            @Override
            public AtlasRegion find(String name, String def) {
                if (!cache.containsKey(name)) {
                    return find(def);
                }
                return find(name);
            }

            @Override
            public boolean has(String s) {
                return cache.containsKey(s);
            }
        };
        Core.atlas.setErrorRegion("error");

        Draw.scl = 1f / Core.atlas.find("scale_marker").width;

        Vars.content.each(c -> {
            if (c instanceof MappableContent) ModContentRegions.loadRegions((MappableContent) c);
        });
        Time.mark();
        runGenerators();
        Log.info("&ly[Generator]&lc Total time to generate: &lg@&lcms", Time.elapsed());
        Log.info("&ly[Disposing]&lc Start");
        Time.mark();
        Log.info("&ly[Disposing]&lc Total time: @", Time.elapsed());
        ModVars.packSprites = false;
    }

    protected void runGenerators() {
        new ModGenerators();
    }

    protected void init() {
        ModEntityMapping.init();
    }

    public static void main(String[] args) throws Exception {
        new ModImagePacker();
    }


    protected void checkContent(Content content) {
        if (content instanceof MappableContent){
            ModContentRegions.loadRegions((MappableContent) content);
        }
    }
    public static String full(String name) {
        return modMeta.name + "-" + name;
    }
}
