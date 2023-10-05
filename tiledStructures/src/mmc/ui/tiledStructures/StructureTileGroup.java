package mmc.ui.tiledStructures;

import arc.func.*;
import arc.struct.*;
import mmc.ui.tiledStructures.TiledStructuresCanvas.StructureTilemap.*;

public class StructureTileGroup{
    private final Seq<StructureTile> tiles = new Seq<>();
    private float x, y;
    private int structureX, structureY;
    private int width = 0, height = 0;

    public void setPosition(int x, int y){
        for(StructureTile structure : tiles){
            structure.pos(structure.obj.editorX - structureX + x, structure.obj.editorY - structureY + y);
        }
        this.structureX = x;
        this.structureY = y;

        this.x = x * TiledStructuresCanvas.unitSize;
        this.y = y * TiledStructuresCanvas.unitSize;
    }

    public void clear(){
        tiles.clear();
        setPosition(0, 0);
        setSize(0, 0);
    }

    public void add(StructureTile tile){

        tiles.add(tile);
        calculateSize();
    }

    public void calculateSize(){
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        float minElementX = Integer.MAX_VALUE, minElementY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for(StructureTile structure : tiles){
            minX = Math.min(structure.obj.editorX, minX);
            minY = Math.min(structure.obj.editorY, minY);

            minElementX = Math.min(structure.x, minElementX);
            minElementY = Math.min(structure.y, minElementY);

            maxX = Math.max(structure.obj.editorX + structure.obj.objWidth(), maxX);
            maxY = Math.max(structure.obj.editorY + structure.obj.objHeight(), maxY);
        }
        width = maxX - minX;
        height = maxY - minY;
        structureX = minX;
        structureY = minY;
        x = minElementX;
        y = minElementY;

    }

    private void setSize(int width, int height){
        this.width = width;
        this.height = height;
    }

    public int height(){
        return height;
    }

    public int width(){
        return width;
    }

    public boolean any(){
        return tiles.any();
    }

    public boolean isEmpty(){
        return tiles.isEmpty();
    }


    public void each(Cons<StructureTile> cons){
        tiles.each(cons);
    }

    public boolean contains(Boolf<StructureTile> cons){
        return tiles.contains(cons);
    }

    public Seq<StructureTile> list(){
        return tiles;
    }

    public StructureTile get(int i){
        return tiles.get(i);
    }

    public void moveBy(float dx, float dy){
        for(StructureTile structure : tiles){
            structure.moveBy(dx, dy);
        }
        x += dx;
        y += dy;
    }

    public boolean contains(StructureTile structureTile){
        return tiles.contains(structureTile);
    }

    public void add(StructureTileGroup group){
        tiles.set(group.tiles);
    }

    public void set(StructureTileGroup group){
        clear();
        tiles.set(group.tiles);
        structureX = group.structureX;
        structureY = group.structureY;
        x = group.x;
        y = group.y;
        width = group.width;
        height = group.height;
    }

    public float x(){
        return x;
    }

    public float y(){
        return y;
    }

    public int structureX(){
        return structureX;
    }

    public int structureY(){
        return structureY;
    }

    public void remove(StructureTile tile){
        tiles.remove(tile);
        calculateSize();
    }
}
