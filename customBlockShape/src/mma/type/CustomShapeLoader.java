package mma.type;

import mma.struct.*;

public abstract class CustomShapeLoader<T>{
    public int width;
    public int height;
    public BitWordList blocks;

    public abstract void load(T type);

    public CustomShape toForm(){
        return new CustomShape(width, height, blocks);
    }
}
