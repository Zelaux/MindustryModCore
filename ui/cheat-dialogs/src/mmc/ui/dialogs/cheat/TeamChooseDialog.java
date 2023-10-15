package mmc.ui.dialogs.cheat;

import arc.func.Cons;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import mindustry.game.Team;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.mobile;

public class TeamChooseDialog extends BaseDialog {
    private final Cons<Team> confirm;

    public TeamChooseDialog(Cons<Team> confirm) {
        super("Choose team:");
        this.confirm = confirm;
        setup();
        if (mobile) onResize(this::setup);
        this.addCloseButton();
    }

    private void setup() {
        cont.clear();
        this.cont.table(i -> {
            i.table(t -> {
                final int buttonSize = 20;
                int pad = 6,counter=0;
                float coln = mobile ? 10 : 20;
                if (mobile){
                    ScrollPane scrollPane = t.pane(p -> {
                    }).get();
//                    scrollPane.setScrollingDisabled(false,false);
                    t= (Table) scrollPane.getWidget();
                }
//                coln = !mobile ? 20 : (Core.graphics.getWidth() - Scl.scl((2) * pad)) / Scl.scl(buttonSize + pad);
                coln = Mathf.clamp(coln,1,100);
                for (Team team : Team.all) {
                    if (counter++ % coln == 0) t.row();
                    ImageButton button = new ImageButton(Tex.whitePane, Styles.clearNoneTogglei);
                    button.clearChildren();
                    Image image = new Image();
                    button.background(image.getDrawable()).setColor(team.color);
                    button.add(image).color(team.color).size(buttonSize);
                    button.clicked(() -> {
                        confirm.get(team);
                        this.hide();
                    });
                    t.add(button).color(team.color).width(buttonSize).height(buttonSize).pad(pad);
                }
            });
        }).growX().bottom().center();
    }
}
