package mma.type.formloaders;

import arc.graphics.*;
import arc.util.*;
import mma.struct.*;
import mma.struct.BitWordList.*;
import mma.type.CustomShape.*;
import mma.type.*;

public class SpriteLoader extends CustomShapeLoader<Pixmap>{
    public final int chunkSize;
    public final ChunkProcessor chunkProcessor;

    public SpriteLoader(int chunkSize, ChunkProcessor chunkProcessor){
        this.chunkSize = chunkSize;
        this.chunkProcessor = chunkProcessor;
    }

    @Override
    public void load(Pixmap type){
        width = type.width / chunkSize;
        height = type.height / chunkSize;
        blocks = new BitWordList(width * height, WordLength.two);
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                int chunkX = x * chunkSize;
                int chunkY = y * chunkSize;
                int index = x + y * width;
                blocks.set(index, (byte)chunkProcessor.process(type, chunkX, chunkY, chunkSize).ordinal());
            }
        }
    }

    public interface ChunkProcessor{
        public BlockType process(Pixmap pixmap, int chunkX, int chunkY, int size);

        class PercentProcessor implements ChunkProcessor{
            /** [0-1] */
            public float percent;
            public int anchorChunkX;
            public int anchorChunkY;

            public PercentProcessor(float percent, int anchorChunkX, int anchorChunkY){
                this.percent = percent;
                this.anchorChunkX = anchorChunkX;
                this.anchorChunkY = anchorChunkY;
            }

            @Override
            public BlockType process(Pixmap pixmap, int chunkX, int chunkY, int size){
                if(chunkX == anchorChunkX && chunkY == anchorChunkY) return BlockType.anchorBlock;
                int total = size * size;
                float counter = 0;
                for(int dx = 0; dx < size; dx++){
                    for(int dy = 0; dy < size; dy++){
                        Color color = Tmp.c1.set(pixmap.get(chunkX + dx, chunkY + dy));
                        float max = Math.max(Math.max(color.r, color.g), color.b);
                        counter += max;
                    }
                }
                return counter / total > percent ? BlockType.block : BlockType.voidBlock;
            }
        }
    }
}
