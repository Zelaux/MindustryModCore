package mma.type;

import arc.func.Func;
import arc.graphics.Pixmap;
import arc.graphics.g2d.TextureRegion;

public interface SelfIconGenerator {
    Pixmap generate(Pixmap icon, Func<TextureRegion,Pixmap> pixmapProvider);
}
