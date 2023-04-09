package mmat.tests.world;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mma.type.formloaders.*;
import mma.world.blocks.*;

public class ExampleShapedBlockPixmap extends CustomShapeBlock{
    public ExampleShapedBlockPixmap(String name){
        super(name);
    }

    @Override
    public void load(){
        super.load();
        PixmapRegion atlasPixmap = Core.atlas.getPixmap(name + "-position");
        Pixmap pixmap = atlasPixmap.crop();
        PixmapShapeLoader loader = new PixmapShapeLoader(Color.black,Color.white,Color.red);
        loader.load(pixmap);
        customShape = loader.toShape();
    }
}
