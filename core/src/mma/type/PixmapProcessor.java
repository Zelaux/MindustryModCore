package mma.type;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;

public interface PixmapProcessor{
    void save(Pixmap pixmap, String path);

    Pixmap get(String name);

    boolean has(String name);

    Pixmap get(TextureRegion region);

    void replace(String name, Pixmap image);

    void replace(TextureRegion name, Pixmap image);

    void delete(String name);

    default void drawScaledFit(Pixmap base, Pixmap image) {
        Vec2 size = Scaling.fit.apply(image.width, image.height, base.width, base.height);
        int wx = (int) size.x, wy = (int) size.y;
        // TODO bad linear scaling
        base.draw(image, 0, 0, image.width, image.height, base.width / 2 - wx / 2, base.height / 2 - wy / 2, wx, wy, true, true);
    }

    default void drawCenter(Pixmap pix, Pixmap other) {
        pix.draw(other, pix.width / 2 - other.width / 2, pix.height / 2 - other.height / 2, true);
    }

    default void saveScaled(Pixmap pix, String name, int size) {
        Pixmap scaled = new Pixmap(size, size);
        // TODO bad linear scaling
        scaled.draw(pix, 0, 0, pix.width, pix.height, 0, 0, size, size, true, true);
        save(scaled, name);
    }
}
