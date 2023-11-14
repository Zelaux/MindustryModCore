package mmc.tools;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.serialization.*;
import arc.util.serialization.Jval.*;
import mindustry.ctype.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mmc.*;
import mmc.core.*;
import mmc.gen.*;
import mmc.tools.gen.*;
import mmc.type.pixmap.*;

public class ModImagePacker extends MindustryImagePacker{
    public static boolean unitOutlines = true;
    public static PixmapProcessor processor = new PixmapProcessor(){
        @Override
        public void save(Pixmap pixmap, String path){
            MindustryImagePacker.save(pixmap, path);
        }

        @Override
        public Pixmap get(String name){
            return MindustryImagePacker.get(name);
        }

        @Override
        public boolean has(String name){
            return MindustryImagePacker.has(name);
        }

        @Override
        public Pixmap get(TextureRegion region){
            return MindustryImagePacker.get(region);
        }

        @Override
        public void replace(String name, Pixmap image){
            MindustryImagePacker.replace(name, image);

        }

        @Override
        public void replace(TextureRegion name, Pixmap image){
            MindustryImagePacker.replace(name, image);

        }

        @Override
        public boolean replaceAbsolute(TextureRegion name, Pixmap image){
            return ModImagePacker.replaceAbsolute(name, image);
        }

        @Override
        public void delete(String name){
            MindustryImagePacker.delete(name);
        }
    };
    static Mods.ModMeta modMeta;

    public ModImagePacker(){
        try{
            start();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    static public boolean replaceAbsolute(String name, Pixmap image){
        PackIndex packIndex = cache.get(name);
        if(packIndex == null) return false;
        packIndex.pixmap = image;
        packIndex.file.writePng(image);
        return true;
    }

    static public boolean replaceAbsolute(TextureRegion region, Pixmap image){
        return replaceAbsolute(((GenRegion)region).name, image);
    }

    public static void main(String[] args) throws Exception{
        new ModImagePacker();
    }

    public static String full(String name){
        return modMeta.name + "-" + name;
    }

    protected void downloadMindustrySprites(){
        try{
            Fi zipFileLoc = Fi.tempFile("mindustrySprites.zip");
            zipFileLoc.delete();
//            FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/Zelaux/ZelauxModCore/master/core/mindustrySprites.zip"), zipFileLoc.file());
            zipFileLoc.write(getClass().getClassLoader().getResourceAsStream("core/mindustrySprites.zip"), false);
            Fi mindustrySprites = Fi.tempDirectory("mindustrySprites");
            for(Fi fi : new ZipFi(zipFileLoc).list()){
                fi.copyTo(mindustrySprites);
            }
            Fi sprites = Fi.get("../../../assets-raw/sprites_out/mindustrySprites");
            sprites.emptyDirectory();
            String filePrefix = mindustrySprites.absolutePath() + "/";
            mindustrySprites.walk(fi -> {
                fi.copyTo(sprites.child(fi.absolutePath().substring(filePrefix.length())));
//                Log.info("fi: @", fi);
            });
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void start() throws Exception{
        disableIconProcessing = true;

        ModImagePacker.modMeta = getModMeta();

        ModVars.packSprites = true;
        downloadMindustrySprites();
        super.start();
        deleteMindustrySprites();
        ModVars.packSprites = false;
    }

    protected Class<?> modSettingsClass(){
        return null;
    }

    protected ModMeta getModMeta(){
        Json json = new Json();
        //Fi.get("")="core/assets-raw/sprites_out/generated/"
        Fi metaf = Fi.get("../../../../").child("mod.hjson");
        if(!metaf.exists()){
            metaf = Fi.get("../../../../").child("mod.json");
        }
        return json.fromJson(ModMeta.class, Jval.read(metaf.readString()).toString(Jformat.plain));
    }

    private void deleteMindustrySprites(){
        Fi sprites = Fi.get("../../../assets-raw/sprites_out/mindustrySprites");
        sprites.deleteDirectory();
    }

    @Override
    protected void preCreatingContent(){
        super.preCreatingContent();
        ModEntityMapping.init();
    }

    @Override
    protected void runGenerators(){
        new ModGenerators();
    }

    @Override
    protected void load(){
        ModContentLoader.eachModContent(this::checkContent);
    }

    protected void checkContent(Content content){
        /*if(content instanceof MappableContent){
            ModContentRegions.loadRegions((MappableContent)content);
        }*/
    }
}
