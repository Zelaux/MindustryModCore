package mmc.core;

import arc.func.*;
import arc.struct.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.mod.Mods.*;
import mindustry.type.*;
import mindustry.world.*;

public class ContentLoaderWrapper extends ContentLoader{
public final ContentLoader wrapped;

    public ContentLoaderWrapper(ContentLoader wrapped){
        super();
        this.wrapped = wrapped;
    }


    @Override
    public void createBaseContent(){
        wrapped.createBaseContent();
    }

    @Override
    public void createModContent(){
        wrapped.createModContent();
    }

    @Override
    public void logContent(){
        wrapped.logContent();
    }

    @Override
    public void init(){
        wrapped.init();
    }

    @Override
    public void load(){
        wrapped.load();
    }

    @Override
    public void loadColors(){
        wrapped.loadColors();
    }

    @Override
    public Content getLastAdded(){
        return wrapped.getLastAdded();
    }

    @Override
    public void removeLast(){
        wrapped.removeLast();
    }

    @Override
    public void handleContent(Content content){
        wrapped.handleContent(content);
    }

    @Override
    public void setCurrentMod(LoadedMod mod){
        wrapped.setCurrentMod(mod);
    }

    @Override
    public String transformName(String name){
        return wrapped.transformName(name);
    }

    @Override
    public void handleMappableContent(MappableContent content){
        wrapped.handleMappableContent(content);
    }

    @Override
    public void setTemporaryMapper(MappableContent[][] temporaryMapper){
        wrapped.setTemporaryMapper(temporaryMapper);
    }

    @Override
    public Seq<Content>[] getContentMap(){
        return wrapped.getContentMap();
    }

    @Override
    public void each(Cons<Content> cons){
        wrapped.each(cons);
    }

    @Override
    public <T extends MappableContent> T getByName(ContentType type, String name){
        return wrapped.getByName(type, name);
    }

    @Override
    public <T extends Content> T getByID(ContentType type, int id){
        return wrapped.getByID(type, id);
    }

    @Override
    public <T extends Content> Seq<T> getBy(ContentType type){
        return wrapped.getBy(type);
    }

    @Override
    public Seq<Block> blocks(){
        return wrapped.blocks();
    }

    @Override
    public Block block(int id){
        return wrapped.block(id);
    }

    @Override
    public Block block(String name){
        return wrapped.block(name);
    }

    @Override
    public Seq<Item> items(){
        return wrapped.items();
    }

    @Override
    public Item item(int id){
        return wrapped.item(id);
    }

    @Override
    public Item item(String name){
        return wrapped.item(name);
    }

    @Override
    public Seq<Liquid> liquids(){
        return wrapped.liquids();
    }

    @Override
    public Liquid liquid(int id){
        return wrapped.liquid(id);
    }

    @Override
    public Liquid liquid(String name){
        return wrapped.liquid(name);
    }

    @Override
    public Seq<BulletType> bullets(){
        return wrapped.bullets();
    }

    @Override
    public BulletType bullet(int id){
        return wrapped.bullet(id);
    }

    @Override
    public Seq<StatusEffect> statusEffects(){
        return wrapped.statusEffects();
    }

    @Override
    public StatusEffect statusEffect(String name){
        return wrapped.statusEffect(name);
    }

    @Override
    public Seq<SectorPreset> sectors(){
        return wrapped.sectors();
    }

    @Override
    public SectorPreset sector(String name){
        return wrapped.sector(name);
    }

    @Override
    public Seq<UnitType> units(){
        return wrapped.units();
    }

    @Override
    public UnitType unit(int id){
        return wrapped.unit(id);
    }

    @Override
    public UnitType unit(String name){
        return wrapped.unit(name);
    }

    @Override
    public Seq<Planet> planets(){
        return wrapped.planets();
    }

    @Override
    public Planet planet(String name){
        return wrapped.planet(name);
    }
}
