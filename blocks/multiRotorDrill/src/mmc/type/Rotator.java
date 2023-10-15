package mmc.type;

import arc.graphics.g2d.*;
import mindustry.*;
import mmc.*;

import static mindustry.Vars.*;

public class Rotator{
    /** tile relative x */
    public float x;
    /** tile relative y */
    public float y;
    public float size = -1;
    public String textureName;
    public boolean scaleTextureBySize;
    public TextureRegion region;
    public TextureRegion regionTop;

    /**
     * @param x tile relative x
     * @param y tile relative y
     * @param size world size
     */
    public Rotator(float x, float y, float size, String textureName){
        this.x = x;
        this.y = y;
        this.size = size;
        this.textureName = textureName == null ? null : ModVars.fullName(textureName);
    }

    /**
     * @param x tile relative x
     * @param y tile relative y
     * @param size world size
     */
    public Rotator(float x, float y, float size){
        this(x, y, size, null);
    }

    /**
     * @param x tile relative x
     * @param y tile relative y
     */
    public Rotator(float x, float y, String textureName){
        this(x, y, -1, textureName);
    }

    /**
     * @param x tile relative x
     * @param y tile relative y
     */
    public Rotator(float x, float y){
        this(x, y, null);
    }

    /**
     * @param x world relative x
     * @param y world relative y
     * @param size world size
     */
    public static Rotator withWorld(float x, float y, float size, String textureName){
        return new Rotator(x / Vars.tilesize, y / Vars.tilesize, size, textureName);
    }

    public Rotator scaleTextureBySize(boolean scaleTextureBySize){
        this.scaleTextureBySize = scaleTextureBySize;
        return this;
    }

    public void drawAt(float drawx, float drawy, float rotation){
        float targetx = drawx + x * tilesize-tilesize/2f;
        float targety = drawy + y * tilesize-tilesize/2f;
        if(scaleTextureBySize){
            float w = size * Draw.xscl;
            float h = size * Draw.yscl;
            Draw.rect(region, targetx , targety , w, h, rotation);
            Draw.rect(regionTop, targetx , targety , w, h, 0);
        }else{

            float regionWidth = region.width * Draw.scl * Draw.xscl;
            float regionHeight = region.height * Draw.scl * Draw.yscl;

            float regionTopWidth = regionTop.width * Draw.scl * Draw.xscl;
            float regionTopHeight = regionTop.height * Draw.scl * Draw.yscl;

            Draw.rect(region, targetx , targety , regionWidth, regionHeight, rotation);
            Draw.rect(regionTop, targetx, targety , regionTopWidth, regionTopHeight, 0f);
        }
//        Draw.color(Color.black);
//        AFill.circle(targetx, targety, size / 2f);
//        Draw.color();
    }

    public interface DrillRotorCons{
        void get(float x, float y, float rotorSize);
    }
}
