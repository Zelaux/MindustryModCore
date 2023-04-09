package mma.world.blocks;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.ConstructBlock.*;
import mma.type.*;

import static mindustry.Vars.tilesize;

public class CustomShapeBlock extends Block{
    public CustomShape customShape;
    boolean checking = false;
    protected final int[] emptySubBuildings = new int[0];

    public CustomShapeBlock(String name){
        super(name);
        this.destructible = true;
        this.update = true;
    }

    @Override
    public void init(){
        super.init();
        clipSize = Math.max(Math.max(customShape.width, customShape.height) * tilesize, clipSize);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        boolean validPlan = true;
        if(!checking){
            validPlan = checkNearByValid(tile, null, team, rotation);
        }
        return validPlan && super.canPlaceOn(tile, team, rotation);
    }

    private boolean checkNearByValid(Tile tile, Building building, Team team, int rotation){
        boolean[] validPlan = {true};
        eachNearByValid(tile, building, team, rotation, (dx, dy, bool) -> validPlan[0] &= bool);
        return validPlan[0];
    }

    private void eachNearByValid(Tile tile, Building building, Team team, int rotation, Cons3<Integer, Integer, Boolean> cons3){
        checking = true;
        customShape.eachRelativeCenter(false, true, false, (dx, dy) -> {
            Tmp.p1.set(dx, dy).rotate(rotation);
            int nearbyX = tile.x + Tmp.p1.x;
            int nearbyY = tile.y + Tmp.p1.y;
            if(Build.validPlace(this, team, nearbyX, nearbyY, rotation) || building != null && Vars.world.build(nearbyX, nearbyY) == building){
                cons3.get(nearbyX, nearbyY, true);
            }else{
                cons3.get(nearbyX, nearbyY, false);
            }
        });
        checking = false;
    }

    @Override
    public void drawPlan(BuildPlan plan, Eachable<BuildPlan> list, boolean valid, float alpha){

        super.drawPlan(plan, list, valid, alpha);
        eachNearByValid(plan.tile(), null, Vars.player.team(), plan.rotation, (dx, dy, bool) -> {
            Drawf.selected(dx, dy, this, bool ? Pal.accent : Pal.remove);
        });
    }

    boolean removingSubs = false;
    private static final TileChangeEvent tileChangeEvent = new TileChangeEvent();

    @Override
    public void placeBegan(Tile tile, Block previous){
        customShape.eachRelativeCenter(false, true, false, (dx, dy) -> {
            ConstructBuild build = (ConstructBuild)tile.build;
            Tile nearby = tile.nearby(Tmp.p1.set(dx, dy).rotate(build.rotation));
            setTile(nearby, tile.block(), tile.build);
        });
        super.placeBegan(tile, previous);
    }

    protected void setTile(Tile nearby, Block block, Building build){
        Reflect.set(nearby, "block", block);
        nearby.build = build;
        Events.fire(tileChangeEvent.set(nearby));
        tileChangeEvent.set(null);
    }

    public class CustomFormBuild extends Building{
        int[] subBuilding = emptySubBuildings;
        private int prevRotation;

        @Override
        public void update(){
            updateRotation(super::update);
        }

        @Override
        public void draw(){
            if(updateRotation(super::draw)){
                return;
            }
            Draw.draw(Layer.overlayUI, () -> {

                Draw.mixcol(Pal.breakInvalid, 0.4f + Mathf.absin(Time.globalTime, 6f, 0.28f));
                Draw.rect(region, x, y);
                Draw.mixcol();
                eachNearByValid(tile, this, Vars.player.team(), rotation, (dx, dy, bool) -> {
                    if(!bool) Drawf.selected(dx, dy, block, Color.white);
                });
                Draw.mixcol();
            });

        }


        @Override
        public void remove(){
            boolean wasAdded = this.added;
            if(removingSubs) return;
            super.remove();
            if(wasAdded && !added){
                removeSub();
            }
        }

        private void removeSub(){
            if(removingSubs) return;
            removingSubs = true;
            for(int i : subBuilding){
                Tile tile = Vars.world.tile(i);
                tile.remove();
                Events.fire(tileChangeEvent.set(tile));
                tileChangeEvent.set(null);
            }
            removingSubs = false;
            subBuilding = emptySubBuildings;
        }

        private boolean updateRotation(Runnable callback){
            if(prevRotation == rotation || checkNearByValid(tile, this, team, rotation)){
                if(prevRotation != rotation){
                    removeSub();
                    initSubBuilds();
                }
                prevRotation = rotation;
                callback.run();
                return true;
            }


            int tmpRotation = rotation;
            rotation = prevRotation;
            callback.run();
            rotation = tmpRotation;
            return false;
        }

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation){
            Building init = super.init(tile, team, shouldAdd, rotation);
            prevRotation = rotation;
            if(!Vars.world.isGenerating()){
                initSubBuilds();
            }
            return init;
        }

        private void initSubBuilds(){
            Log.info("initSubBuilds @", tile);
            if(subBuilding != emptySubBuildings){
                removeSub();
            }
            subBuilding = new int[customShape.otherBlocksAmount];
            int[] counter = {0};
            customShape.eachRelativeCenter(false, true, false, (dx, dy) -> {
                Tile nearby = tile.nearby(Tmp.p1.set(dx, dy).rotate(rotation));
                setTile(nearby, CustomShapeBlock.this, this);
                subBuilding[counter[0]] = nearby.pos();
                counter[0]++;
            });
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(prevRotation);
            write.i(subBuilding.length);
            for(int j : subBuilding){
                write.i(j);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            prevRotation = read.i();
            int size = read.i();
            if(subBuilding != emptySubBuildings){
                removeSub();
            }
            subBuilding = new int[size];
            for(int i = 0; i < size; i++){
                subBuilding[i] = read.i();
            }
            initSubBuilds();
        }
    }
}
