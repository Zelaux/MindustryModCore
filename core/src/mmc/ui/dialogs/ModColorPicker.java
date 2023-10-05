package mmc.ui.dialogs;

import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.Image;
import arc.util.Structs;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.BaseDialog;
import mmc.core.ModUI;

import java.util.Objects;

public class ModColorPicker extends BaseDialog {
    public static final String[] allowedCharacters = "abcdef1234567890#".split("");
    Color current = new Color();
    private Cons<Color> cons = (c) -> {
    };

    public ModColorPicker() {
        super("@pickcolor");
    }

    public void show(Color color, Cons<Color> consumer) {
        this.show(color, true, consumer);
    }

    public void show(Color color, boolean alpha, Cons<Color> consumer) {
        this.current.set(color);
        this.cons = consumer;
        this.show();
        this.cont.clear();
        this.cont.pane((t) -> {
            t.table(Tex.pane, (i) -> {
                i.stack(new Element[]{new Image(Tex.alphaBg), new Image() {
                    {
                        this.setColor(ModColorPicker.this.current);
                        this.update(() -> {
                            this.setColor(ModColorPicker.this.current);
                        });
                    }
                }}).size(200.0F);
            }).colspan(2).padBottom(5.0F);
            float w = 150.0F;
            t.row();
            t.defaults().padBottom(4.0F);
            t.add("R").color(Pal.remove);
            float var10004 = this.current.r;
            Color var10005 = this.current;
            Objects.requireNonNull(var10005);
            t.slider(0.0F, 1.0F, 0.01F, var10004, var10005::r).width(w);
            t.row();
            t.add("G").color(Color.lime);
            var10004 = this.current.g;
            var10005 = this.current;
            Objects.requireNonNull(var10005);
            t.slider(0.0F, 1.0F, 0.01F, var10004, var10005::g).width(w);
            t.row();
            t.add("B").color(Color.royal);
            var10004 = this.current.b;
            var10005 = this.current;
            Objects.requireNonNull(var10005);
            t.slider(0.0F, 1.0F, 0.01F, var10004, var10005::b).width(w);
            t.row();
            if (alpha) {
                t.add("A");
                var10004 = this.current.a;
                var10005 = this.current;
                Objects.requireNonNull(var10005);
                t.slider(0.0F, 1.0F, 0.01F, var10004, var10005::a).width(w);
                t.row();
            }
            t.button("Write hex", () -> {
                ModUI.showTextInput("white hex color", "hex", this.current.toString().length() - (alpha ? 0 : 2), this.current.toString(), (f, s) -> {

                    return Structs.contains(allowedCharacters,(String.valueOf(s)).toLowerCase());
                }, (s -> {
                    this.current.set(Color.valueOf(s));
                }));
            }).width(w).colspan(2);

        });
        this.buttons.clear();
        this.addCloseButton();
        this.buttons.button("@ok", Icon.ok, () -> {
            this.cons.get(this.current);
            this.hide();
        });
    }
}
