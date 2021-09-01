package mma.tools.gen;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Log.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.logic.*;
import mindustry.world.blocks.*;
import java.io.*;

public class MindustryImagePacker {

    static public ObjectMap<String, PackIndex> cache = new ObjectMap<>();

    static public String texname(UnlockableContent c) {
        return c.getContentType() + "-" + c.name + "-ui";
    }

    static public void generate(String name, Runnable run) {
        Time.mark();
        run.run();
        Log.info("&ly[Generator]&lc Time to generate &lm@&lc: &lg@&lcms", name, Time.elapsed());
    }

    static public Pixmap get(String name) {
        return get(Core.atlas.find(name));
    }

    static public boolean has(String name) {
        return Core.atlas.has(name);
    }

    static public Pixmap get(TextureRegion region) {
        validate(region);
        return cache.get(((AtlasRegion) region).name).pixmap.copy();
    }

    static public void save(Pixmap pix, String path) {
        Fi.get(path + ".png").writePng(pix);
    }

    static public void drawCenter(Pixmap pix, Pixmap other) {
        pix.draw(other, pix.width / 2 - other.width / 2, pix.height / 2 - other.height / 2, true);
    }

    static public void saveScaled(Pixmap pix, String name, int size) {
        Pixmap scaled = new Pixmap(size, size);
        // TODO bad linear scaling
        scaled.draw(pix, 0, 0, pix.width, pix.height, 0, 0, size, size, true, true);
        save(scaled, name);
    }

    static public void drawScaledFit(Pixmap base, Pixmap image) {
        Vec2 size = Scaling.fit.apply(image.width, image.height, base.width, base.height);
        int wx = (int) size.x, wy = (int) size.y;
        // TODO bad linear scaling
        base.draw(image, 0, 0, image.width, image.height, base.width / 2 - wx / 2, base.height / 2 - wy / 2, wx, wy, true, true);
    }

    static public void delete(String name) {
        ((GenRegion) Core.atlas.find(name)).path.delete();
    }

    static public void replace(String name, Pixmap image) {
        Fi.get(name + ".png").writePng(image);
        ((GenRegion) Core.atlas.find(name)).path.delete();
    }

    static public void replace(TextureRegion region, Pixmap image) {
        replace(((GenRegion) region).name, image);
    }

    static public void err(String message, Object... args) {
        throw new IllegalArgumentException(Strings.format(message, args));
    }

    static public void validate(TextureRegion region) {
        if (((GenRegion) region).invalid) {
            MindustryImagePacker.err("Region does not exist: @", ((GenRegion) region).name);
        }
    }

    static public class GenRegion extends AtlasRegion {

        public boolean invalid;

        public Fi path;

        public GenRegion(String name, Fi path) {
            if (name == null)
                throw new IllegalArgumentException("name is null");
            this.name = name;
            this.path = path;
        }

        @Override
        public boolean found() {
            return !invalid;
        }
    }

    static public class PackIndex {

        @Nullable
        public AtlasRegion region;

        @Nullable
        public Pixmap pixmap;

        public Fi file;

        public PackIndex(Fi file) {
            this.file = file;
        }
    }
}
