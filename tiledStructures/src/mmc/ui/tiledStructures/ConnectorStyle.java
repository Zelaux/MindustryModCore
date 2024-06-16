package mmc.ui.tiledStructures;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.NinePatch;
import arc.graphics.g2d.TextureAtlas;
import arc.scene.style.ScaledNinePatchDrawable;
import arc.scene.ui.Button;
import arc.util.Reflect;
import arc.util.Tmp;
import mindustry.gen.Tex;
import org.jetbrains.annotations.NotNull;

public class ConnectorStyle {
    public Button.ButtonStyle inputStyle;
    public Button.ButtonStyle outputStyle;

    public ConnectorStyle(Button.ButtonStyle inputStyle, Button.ButtonStyle outputStyle) {
        this.inputStyle = inputStyle;
        this.outputStyle = outputStyle;
    }

    public ConnectorStyle() {
    }

    public static ConnectorStyle defaultStyle() {
        if (!Core.scene.hasStyle(ConnectorStyle.class)) {
            Core.scene.addStyle(ConnectorStyle.class, innerDefault());
        }
        return Core.scene.getStyle(ConnectorStyle.class);
    }

    @NotNull
    private static ConnectorStyle innerDefault() {
        float drawableScale = Reflect.get(TextureAtlas.class, Core.atlas, "drawableScale");
        Color fillColor = Tmp.c1.set(0x252525FF);
        Button.ButtonStyle input = new Button.ButtonStyle() {{
            down = Tex.buttonSideLeftDown;
            up = Tex.buttonSideLeft;
            over = Tex.buttonSideLeftOver;
            TextureAtlas.AtlasRegion region = Core.atlas.find("button-side-left-over");
            Pixmap pixmap = Core.atlas.getPixmap(region).crop();

            pixmap.each((x, y) -> {
                if (pixmap.getA(x, y) > 0) {
                    pixmap.set(x, y, fillColor);
                }
            });

            int[] splits = region.splits;
            NinePatch patch = new NinePatch(new Texture(pixmap), splits[0], splits[1], splits[2], splits[3]);
            pixmap.dispose();
            int[] pads = region.pads;
            if (pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
            disabled = new ScaledNinePatchDrawable(patch, drawableScale);
        }};
        Button.ButtonStyle output = new Button.ButtonStyle() {{
            down = Tex.buttonSideRightDown;
            up = Tex.buttonSideRight;
            over = Tex.buttonSideRightOver;

            TextureAtlas.AtlasRegion region = Core.atlas.find("button-side-right-over");
            Pixmap pixmap = Core.atlas.getPixmap(region).crop();

            pixmap.each((x, y) -> {
                if (pixmap.getA(x, y) > 0) {
                    pixmap.set(x, y, fillColor);
                }
            });

            int[] splits = region.splits;
            NinePatch patch = new NinePatch(new Texture(pixmap), splits[0], splits[1], splits[2], splits[3]);
            pixmap.dispose();
            int[] pads = region.pads;
            if (pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
            disabled = new ScaledNinePatchDrawable(patch, drawableScale);
        }};
        return new ConnectorStyle(input, output);
    }
}
