package mmc.type;

import arc.func.Func;
import arc.graphics.Pixmap;
import arc.graphics.g2d.TextureRegion;
import mmc.type.pixmap.*;

public interface ImageGenerator{
    default Pixmap generate(Pixmap icon, Func<TextureRegion,Pixmap> pixmapProvider){
        return icon;
    }
    default Pixmap generate(Pixmap icon, PixmapProcessor processor){
        return generate(icon,processor::get);
    }
}
