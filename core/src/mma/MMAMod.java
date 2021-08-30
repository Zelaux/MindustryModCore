package mma;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.MappableContent;
import mindustry.ctype.UnlockableContent;
import mindustry.mod.Mod;
import mma.annotations.ModAnnotations;
import mma.core.ModContentLoader;
import mma.gen.ModContentRegions;

import static mindustry.Vars.content;
import static mma.ModVars.*;

@ModAnnotations.ModAssetsAnnotation
public class MMAMod extends Mod {

    public MMAMod() {
//        ModEntityMapping.init();
        modInfo = Vars.mods.getMod(getClass());
    }

    public static TextureRegion getIcon() {
        if (modInfo == null || modInfo.iconTexture == null) return Core.atlas.find("nomap");
        return new TextureRegion(modInfo.iconTexture);
    }

    protected void modContent(Content content) {

        if (content instanceof UnlockableContent) {
            checkTranslate((UnlockableContent) content);
        }
        if (content instanceof MappableContent){
            ModContentRegions.loadRegions((MappableContent) content);
        }
    }

    public void init() {
        if (!loaded) return;
//        Seq<Content> all = Seq.with(content.getContentMap()).<Content>flatten().select(c -> c.minfo.mod == modInfo).as();
        ModContentLoader.eachModContent(this::modContent);
    }

    public void loadContent() {
        modInfo = Vars.mods.getMod(this.getClass());
        new ModContentLoader((load) -> {
            load.load();
        });
        loaded = true;
    }
}
