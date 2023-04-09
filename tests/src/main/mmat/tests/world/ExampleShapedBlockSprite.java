package mmat.tests.world;

import arc.*;
import arc.graphics.*;
import mma.type.formloaders.*;
import mma.type.formloaders.SpriteShapeLoader.ChunkProcessor.*;
import mma.world.blocks.*;

public class ExampleShapedBlockSprite extends CustomShapeBlock{
    public ExampleShapedBlockSprite(String name){
        super(name);
    }

    @Override
    public void load(){
        super.load();
        Pixmap pixmap = Core.atlas.getPixmap(region).crop();
        SpriteShapeLoader loader = new SpriteShapeLoader(32, new PercentProcessor(0.25f,
        pixmap.width / 64,
        pixmap.height / 64));
        loader.load(pixmap);
        customShape = loader.toShape();
    }
}
