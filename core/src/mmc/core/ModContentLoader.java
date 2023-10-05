package mmc.core;

import arc.*;
import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType.*;
import mindustry.io.*;
import mindustry.mod.Mods.*;
import mindustry.type.*;
import mindustry.world.*;
import mmc.*;

import static arc.Core.files;
import static mindustry.Vars.*;

public class ModContentLoader extends ContentLoader{
    private static final Seq<Content> createdContent = new Seq<>();
    protected boolean loadModContent = false;
    private final ObjectMap<String, MappableContent>[] contentNameMap = new ObjectMap[ContentType.all.length];
    private final Seq<Content>[] contentMap = new Seq[ContentType.all.length];
    private MappableContent[][] temporaryMapper;
    private @Nullable
    LoadedMod currentMod;
    private @Nullable
    Content lastAdded;
    private final ObjectSet<Cons<Content>> initialization = new ObjectSet<>();
    private boolean isInitialization = false;

    public ModContentLoader(){
        for(ContentType type : ContentType.all){
            contentMap[type.ordinal()] = new Seq<>();
            contentNameMap[type.ordinal()] = new ObjectMap<>();
        }
    }

    public ModContentLoader(Cons<Content> cons){
        this();
        createModContent(cons);
    }

    public static void eachModContent(Cons<Content> cons){
        createdContent.each(cons);
    }

    /** Creates all base types. */
    @Override
    public void createBaseContent(){
        TeamEntries.load();
        Items.load();
        StatusEffects.load();
        Liquids.load();
        Bullets.load();
        UnitTypes.load();
        Blocks.load();
        Loadouts.load();
        Weathers.load();
        Planets.load();
        SectorPresets.load();
        SerpuloTechTree.load();
        ErekirTechTree.load();
    }

    /**
     * Creates mod content, if applicable.
     */
    @Override
    public void createModContent(){
        createModContent(c -> {
        });
    }

    public void createModContent(Cons<Content> contentCons){
        loadModContent = true;
        ContentLoader prev = Vars.content;
        createdContent.clear();
        Content[] prevContent = {null};
        Vars.content = new ContentLoaderWrapper(prev){
            @Override
            public void handleContent(Content content){
                content.minfo = new ModContentLoader.ModedModContentInfo();
                super.handleContent(content);
                if(!(content instanceof MappableContent)){
                    contentCons.get(content);
                }
                createdContent.add(content);
            }

            @Override
            public void handleMappableContent(MappableContent content){
                super.handleMappableContent(content);
                contentCons.get(content);
            }
        };
        ModVars.instance().loadContent();
        Vars.content = prev;
        loadModContent = false;
    }

    /** Logs content statistics. */
    @Override
    public void logContent(){
        //check up ID mapping, make sure it's linear (debug only)
        for(Seq<Content> arr : contentMap){
            for(int i = 0; i < arr.size; i++){
                int id = arr.get(i).id;
                if(id != i){
                    throw new IllegalArgumentException("Out-of-order IDs for content '" + arr.get(i) + "' (expected " + i + " but got " + id + ")");
                }
            }
        }

        Log.debug("--- CONTENT INFO ---");
        for(int k = 0; k < contentMap.length; k++){
            Log.debug("[@]: loaded @", ContentType.all[k].name(), contentMap[k].size);
        }
        Log.debug("Total content loaded: @", Seq.with(ContentType.all).mapInt(c -> contentMap[c.ordinal()].size).sum());
        Log.debug("-------------------");
    }

    /** Calls Content#init() on everything. Use only after all modules have been created. */
    @Override
    public void init(){
        isInitialization = true;
        initialize(Content::init);
        isInitialization = false;
        if(logicVars != null) logicVars.init();
        Events.fire(new ContentInitEvent());
    }

    /** Calls Content#loadIcon() and Content#load() on everything. Use only after all modules have been created on the client. */
    @Override
    public void load(){
        initialize(Content::loadIcon);
        initialize(Content::load);
    }

    /** Initializes all content with the specified function. */
    private void initialize(Cons<Content> callable){
        if(initialization.contains(callable)) return;

        for(ContentType type : ContentType.all){
            for(Content content : contentMap[type.ordinal()]){
                try{
                    callable.get(content);
                }catch(Throwable e){
                    if(content.minfo.mod != null){
                        Log.err(e);
                        mods.handleContentError(content, e);
                    }else{
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        initialization.add(callable);
    }

    /** Loads block colors. */
    @Override
    public void loadColors(){
        Pixmap pixmap = new Pixmap(files.internal("sprites/block_colors.png"));
        for(int i = 0; i < pixmap.width; i++){
            if(blocks().size > i){
                int color = pixmap.get(i, 0);

                if(color == 0 || color == 255) continue;

                Block block = block(i);
                block.mapColor.rgba8888(color);
                //partial alpha colors indicate a square sprite
                block.squareSprite = block.mapColor.a > 0.5f;
                block.mapColor.a = 1f;
                block.hasColor = true;
            }
        }
        pixmap.dispose();
        ColorMapper.load();
    }

    /** Get last piece of content created for error-handling purposes. */
    @Override
    public @Nullable
    Content getLastAdded(){
        return lastAdded;
    }

    /** Remove last content added in case of an exception. */
    @Override
    public void removeLast(){
        if(lastAdded != null && contentMap[lastAdded.getContentType().ordinal()].peek() == lastAdded){
            contentMap[lastAdded.getContentType().ordinal()].pop();
            if(lastAdded instanceof MappableContent c){
                contentNameMap[lastAdded.getContentType().ordinal()].remove(c.name);
            }
        }
    }

    @Override
    public void handleContent(Content content){
        this.lastAdded = content;
        contentMap[content.getContentType().ordinal()].add(content);
    }

    @Override
    public void setCurrentMod(@Nullable LoadedMod mod){
        this.currentMod = mod;
    }

    @Override
    public String transformName(String name){
        return currentMod == null ? name : currentMod.name + "-" + name;
    }

    @Override
    public void handleMappableContent(MappableContent content){
        if(contentNameMap[content.getContentType().ordinal()].containsKey(content.name)){
            throw new IllegalArgumentException("Two content objects cannot have the same name! (issue: '" + content.name + "')");
        }
        if(currentMod != null){
            content.minfo.mod = currentMod;
            if(content.minfo.sourceFile == null){
                content.minfo.sourceFile = new Fi(content.name);
            }
        }
        contentNameMap[content.getContentType().ordinal()].put(content.name, content);
    }

    @Override
    public void setTemporaryMapper(MappableContent[][] temporaryMapper){
        this.temporaryMapper = temporaryMapper;
    }

    @Override
    public Seq<Content>[] getContentMap(){
        return contentMap;
    }

    @Override
    public void each(Cons<Content> cons){
        for(Seq<Content> seq : contentMap){
            seq.each(cons);
        }
    }

    @Override
    public <T extends MappableContent> T getByName(ContentType type, String name){
        var map = contentNameMap[type.ordinal()];

        if(map == null) return null;

        //load fallbacks
        if(type == ContentType.block){
            name = SaveVersion.modContentNameMap.get(name, name);
        }

        return (T)map.get(name);
    }

    @Override
    public <T extends Content> T getByID(ContentType type, int id){

        if(temporaryMapper != null && temporaryMapper[type.ordinal()] != null && temporaryMapper[type.ordinal()].length != 0){
            //-1 = invalid content
            if(id < 0){
                return null;
            }
            if(temporaryMapper[type.ordinal()].length <= id || temporaryMapper[type.ordinal()][id] == null){
                return (T)contentMap[type.ordinal()].get(0); //default value is always ID 0
            }
            return (T)temporaryMapper[type.ordinal()][id];
        }

        if(id >= contentMap[type.ordinal()].size || id < 0){
            return null;
        }
        return (T)contentMap[type.ordinal()].get(id);
    }

    @Override
    public <T extends Content> Seq<T> getBy(ContentType type){
        return isInitialization ? contentMap[type.ordinal()].as() : contentMap[type.ordinal()].select(c -> c.minfo instanceof ModedModContentInfo).as();
    }

    //utility methods, just makes things a bit shorter

    @Override
    public Seq<Block> blocks(){
        return getBy(ContentType.block);
    }

    @Override
    public Block block(int id){
        return getByID(ContentType.block, id);
    }

    @Override
    public Block block(String name){
        return getByName(ContentType.block, name);
    }

    @Override
    public Seq<Item> items(){
        return getBy(ContentType.item);
    }

    @Override
    public Item item(int id){
        return getByID(ContentType.item, id);
    }

    @Override
    public Item item(String name){
        return getByName(ContentType.item, name);
    }

    @Override
    public Seq<Liquid> liquids(){
        return getBy(ContentType.liquid);
    }

    @Override
    public Liquid liquid(int id){
        return getByID(ContentType.liquid, id);
    }

    @Override
    public Liquid liquid(String name){
        return getByName(ContentType.liquid, name);
    }

    @Override
    public Seq<BulletType> bullets(){
        return getBy(ContentType.bullet);
    }

    @Override
    public BulletType bullet(int id){
        return getByID(ContentType.bullet, id);
    }

    @Override
    public Seq<StatusEffect> statusEffects(){
        return getBy(ContentType.status);
    }

    @Override
    public StatusEffect statusEffect(String name){
        return getByName(ContentType.status, name);
    }

    @Override
    public Seq<SectorPreset> sectors(){
        return getBy(ContentType.sector);
    }

    @Override
    public SectorPreset sector(String name){
        return getByName(ContentType.sector, name);
    }

    @Override
    public Seq<UnitType> units(){
        return getBy(ContentType.unit);
    }

    @Override
    public UnitType unit(int id){
        return getByID(ContentType.unit, id);
    }

    @Override
    public UnitType unit(String name){
        return getByName(ContentType.unit, name);
    }

    @Override
    public Seq<Planet> planets(){
        return getBy(ContentType.planet);
    }

    @Override
    public Planet planet(String name){
        return getByName(ContentType.planet, name);
    }

    public static class ModedModContentInfo extends Content.ModContentInfo{

    }
}