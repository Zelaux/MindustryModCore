package mma.type.formloaders;

import mma.struct.*;
import mma.struct.BitWordList.*;
import mma.type.CustomShape.*;
import mma.type.*;

public class StringShapeLoader extends CustomShapeLoader<String[]>{
    public final char voidChar, blockChar, anchorChar;

    public StringShapeLoader(char voidChar, char blockChar, char anchorChar){
        this.voidChar = voidChar;
        this.blockChar = blockChar;
        this.anchorChar = anchorChar;
    }

    @Override
    public void load(String... lines){
        blocks = new BitWordList(lines.length * lines[0].length(), WordLength.two);
        int i = 0;
        width = lines[0].length();
        height = lines.length;
        for(String line : lines){
            for(char c : line.toCharArray()){
                BlockType blockType;
                if(c == voidChar){
                    blockType = BlockType.voidBlock;
                }else if(c == blockChar){
                    blockType = BlockType.block;
                }else if(c == anchorChar){
                    blockType = BlockType.anchorBlock;
                }else{
                    throw new IllegalArgumentException("Illegal character \"" + c + "\"");
                }
                blocks.set(i, (byte)blockType.ordinal());
                i++;
            }
        }

    }
}
