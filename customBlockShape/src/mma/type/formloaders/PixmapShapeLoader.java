package mma.type.formloaders;

import arc.graphics.*;
import mma.struct.*;
import mma.struct.BitWordList.*;
import mma.type.CustomShape.*;
import mma.type.*;

public class PixmapShapeLoader extends CustomShapeLoader<Pixmap>{
    public final int voidColor, blockColor, anchorColor;

    public PixmapShapeLoader(int voidColor, int blockColor, int anchorColor){
        this.voidColor = voidColor;
        this.blockColor = blockColor;
        this.anchorColor = anchorColor;
    }

    public PixmapShapeLoader(Color voidColor, Color blockColor, Color anchorColor){
        this(voidColor.rgba(), blockColor.rgba(), anchorColor.rgba());
    }

    /**
     * @throws IllegalArgumentException is pixmap contains unknown colors
     */
    @Override
    public void load(Pixmap pixmap){
        width = pixmap.width;
        height = pixmap.height;
        blocks = new BitWordList(width * height, WordLength.two);
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                int index = (width-1-x) + (y) * width;
                int c = pixmap.get(x, y);
                BlockType blockType;
                if(c == voidColor){
                    blockType = BlockType.voidBlock;
                }else if(c == blockColor){
                    blockType = BlockType.block;
                }else if(c == anchorColor){
                    blockType = BlockType.anchorBlock;
                }else{
                    throw new IllegalArgumentException("Illegal character \"" + c + "\"");
                }
                blocks.set(index, (byte)blockType.ordinal());
            }
        }
    }
}
