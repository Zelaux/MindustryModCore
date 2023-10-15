package mmc.ui.dialogs.cheat;

import arc.graphics.*;
import arc.scene.ui.*;

class ModUIMirror{
    public static Dialog getInfoDialog(String title, String subTitle, String message, Color lineColor){
        return new Dialog(title){{
            setFillParent(true);
            cont.margin(15.0F);
            cont.add(subTitle);
            cont.row();
            cont.image().width(300.0F).pad(2.0F).height(4.0F).color(lineColor);
            cont.row();
            cont.add(message).pad(2.0F).growX().wrap().get().setAlignment(1);
            cont.row();
            cont.button("@ok", this::hide).size(120.0F, 50.0F).pad(4.0F);
            closeOnBack();
        }};
    }
}
