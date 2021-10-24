package mma.type;

import arc.func.Func;
import arc.graphics.Pixmap;
import arc.graphics.g2d.TextureRegion;
import mindustry.ctype.*;

public interface ImageGenerator{
    Pixmap generate(Pixmap icon, Func<TextureRegion,Pixmap> pixmapProvider);
    default Pixmap generate(Pixmap icon, PixmapProcessor processor){
        return generate(icon,processor::get);
    }
}
