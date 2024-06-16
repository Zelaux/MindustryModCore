package mmc.ui.tiledStructures;

import arc.func.*;
import arc.struct.*;
import mmc.ui.tiledStructures.TiledStructures.*;

public class TiledStructureGroup{
    private final Seq<TiledStructure<?>> structures = new Seq<>();
    private int x, y;
    private int width = 0, height = 0;

    public void setPosition(int x, int y){
        for(TiledStructure<?> structure : structures){
            structure.editorX = structure.editorX - this.x + x;
            structure.editorY = structure.editorY - this.y + y;
        }
        this.x = x;
        this.y = y;
    }

    public void clear(){
        structures.clear();
        x = y = width = height = 0;
    }

    public void add(TiledStructure<?> group, boolean hasPosition){
        if(!hasPosition){
            group.editorX = x;
            group.editorY = height;
            width = Math.max(group.objWidth(), width);
            height += group.objHeight();
        }
        structures.add(group);
        if(hasPosition) calculateSize();
    }

    public void calculateSize(){
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for(TiledStructure<?> structure : structures){
            minX = Math.min(structure.editorX, minX);
            minY = Math.min(structure.editorY, minY);

            maxX = Math.max(structure.editorX + structure.objWidth(), maxX);
            maxY = Math.max(structure.editorY + structure.objHeight(), maxY);
        }
        width = maxX - minX;
        height = maxY - minY;
        x = minX;
        y = minY;
        setPosition(0, 0);
    }

    public void validateConnections(){
        for(TiledStructure<?> structure : structures){
            structure.inputWires.removeAll(it -> !structures.contains(it.obj));
        }
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
        return structures.any();
    }

    public boolean isEmpty(){
        return structures.isEmpty();
    }


    public void each(Cons<TiledStructure<?>> cons){
        structures.each(cons);
    }

    public boolean contains(Boolf<TiledStructure<?>> cons){
        return structures.contains(cons);
    }

    public Seq<TiledStructure<?>> list(){
        return structures;
    }

    public int x(){
        return x;
    }

    public int y(){
        return y;
    }
}
