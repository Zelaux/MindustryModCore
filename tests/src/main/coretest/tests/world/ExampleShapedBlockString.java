package coretest.tests.world;

import mmc.type.formloaders.*;
import mmc.world.blocks.*;

public class ExampleShapedBlockString extends CustomShapeBlock{
    public ExampleShapedBlockString(String name){
        super(name);
        StringShapeLoader loader = new StringShapeLoader('0', '1', '#');
        loader.load(
        "111100000",
        "111100000",
        "111100000",
        "111110000",
        "1111#1000",
        "011111111",
        "011111111",
        "001111111",
        "000011111"
        );
        customShape = loader.toShape();
    }
}
