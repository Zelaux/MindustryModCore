package mmc.ui.dialogs.cheat;

import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.ui.Button;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;

public class UnlockContentDialog extends BaseDialog {
    private static int counter = 0;
    private static final float allScale = 1f;
    private Table items;
    Button.ButtonStyle colorButtonStyle;

    public UnlockContentDialog(Button.ButtonStyle colorButtonStyle) {
        super("Unlock content dialog");
        this.colorButtonStyle = colorButtonStyle;
        addCloseButton();
        addCloseListener();
    }

    @Override
    public void addCloseButton() {
        this.setFillParent(true);
        this.keyDown((key) -> {
            if (key == KeyCode.escape || key == KeyCode.back) {
                Core.app.post(this::hide);
            }

        });
        this.cont.pane((t) -> {
            this.items = t.margin(10.0F);
        }).left();
        this.shown(this::setup);
        this.hidden(() -> {
        });
        super.addCloseButton();
    }

    void setup() {
        this.items.clearChildren();
        this.items.left();
        float bsize = 40.0F;
        counter = 0;
        Vars.content.each(c -> {
            if (c instanceof UnlockableContent) {

                UnlockableContent content = (UnlockableContent) c;
                boolean invalidateBlock = content instanceof Block && (((Block) content).buildVisibility != BuildVisibility.shown && ((Block) content).buildVisibility != BuildVisibility.campaignOnly);
                boolean invalidateUnitType = content instanceof UnitType && content.isHidden();
                if (invalidateBlock || invalidateUnitType)
                    return;
                this.items.table(Tex.pane, (t) -> {
                    t.margin(4.0F).marginRight(8.0F).left();
                    t.image(content.uiIcon).size(24.0F).padRight(4.0F).padLeft(4.0F);
                    Button button = new Button(colorButtonStyle);
                    String replace = (!content.localizedName.equals(content.name) ? content.localizedName : Strings.capitalize(content.name))
                            .replace("   ", "_\t===\t_")
                            .replace("  ", "_\t==\t_")
                            .replace(" ", "\n")
                            .replace("_\t==\t_", "  ")
                            .replace("_\t===\t_", "   ");
                    StringBuilder contentName = new StringBuilder();
                    for (int i = 0, counter = 0; i < replace.length(); i++) {
                        char c1 = replace.charAt(i);
                        if (c1 == '\n') {
                            counter++;
                            contentName.append(counter < 4 ? c1 : " ");
                        } else contentName.append(c1);
                    }
                    t.label(contentName::toString).left().width(90.0F * 2f);
                    button.clicked(() -> {
                        if (content.unlocked()) {
                            content.clearUnlock();
                        } else {
                            content.unlock();
                        }
                    });
                    t.add(button).size(bsize).update((b) -> {
                        b.setColor(content.unlocked() ? Pal.accent : Color.grays(0.5f));
                    });
                }).pad(2.0F).height(64f / Scl.scl()).left().fillX();
                counter++;
                int coln = Vars.mobile ? 2 : 3;
                if (counter % coln == 0) {
                    this.items.row();
                }
            }
        });
    }
}
