package mma;

import arc.*;
import arc.graphics.g2d.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.mod.*;
import mindustry.world.*;
import mma.annotations.*;
import mma.annotations.ModAnnotations.*;
import mma.core.*;
import mma.gen.*;

import static mma.ModVars.*;

@ModAnnotations.ModAssetsAnnotation
@CreateMindustrySerialization
public class MMAMod extends Mod{
    protected boolean disableBlockOutline = false;

    public MMAMod(){
//        ModEntityMapping.init();
        modInfo = Vars.mods.getMod(getClass());
    }

    public static TextureRegion getIcon(){
        if(modInfo == null || modInfo.iconTexture == null) return Core.atlas.find("nomap");
        return new TextureRegion(modInfo.iconTexture);
    }

    protected void modContent(Content content){

        if(content instanceof MappableContent){
            ModContentRegions.loadRegions((MappableContent)content);
        }
    }

    protected void created(Content content){

        if(content instanceof UnlockableContent){
            checkTranslate((UnlockableContent)content);
        }
        if(content instanceof Block && disableBlockOutline){
            ((Block)content).outlineIcon = false;
        }
    }

    public void init(){
        if(!loaded) return;
//        Seq<Content> all = Seq.with(content.getContentMap()).<Content>flatten().select(c -> c.minfo.mod == modInfo).as();
        ModContentLoader.eachModContent(this::modContent);
    }

    public void loadContent(){
        modInfo = Vars.mods.getMod(this.getClass());
        new ModContentLoader((load) -> {
            load.load();
        },this::created);
        loaded = true;
    }
}
