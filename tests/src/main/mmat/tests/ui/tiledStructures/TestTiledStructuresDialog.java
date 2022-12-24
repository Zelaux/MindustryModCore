package mmat.tests.ui.tiledStructures;

import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mma.ui.tiledStructures.TiledStructures.*;
import mma.ui.tiledStructures.*;
import mmat.tests.ui.tiledStructures.TestStructures.*;

import static mindustry.Vars.mobile;

public class TestTiledStructuresDialog extends TiledStructuresDialog{
    static{
        setGlobalProvider(TestStructure.class, (type, cons) -> new BaseDialog("@add"){{
            cont.pane(p -> {
                p.background(Tex.button);
                p.marginRight(14f);
                p.defaults().size(195f, 56f);

                int i = 0;
                for(Prov<TestStructure> gen : TestStructures.providers){
                    TestStructure obj = gen.get();
                    p.button(obj.typeName(), Styles.flatt, () -> {
                        cons.get(obj);
                        hide();
                    }).with(Table::left).get().getLabelCell().growX().left().padLeft(5f).labelAlign(Align.left);

                    if(++i % 3 == 0) p.row();
                }
            }).scrollX(false);

            addCloseButton();
            show();
        }});
    }

    public TestTiledStructuresDialog(String title, Class<? extends TiledStructure> initClass){
        super(title, initClass);
    }

    @Override
    protected void setupUI(Class<? extends TiledStructure> initClass){
        clear();
        margin(0f);

        stack(
            new Image(Styles.black5),
            canvas = new TiledStructuresCanvas(this),
            new Table(){{
                buttons.defaults().size(160f, 64f).pad(2f);
                buttons.button("@back", Icon.left, () -> hide());
                buttons.button("@add", Icon.add, () -> getProvider(initClass).get(new TypeInfo(initClass), it -> {
                    if(canvas.isQuerying()){
                        canvas.addQuery(it);
                    }else{
                        canvas.beginQuery(it);
                    }
                }));

                if(mobile){
                    buttons.button("@cancel", Icon.cancel, canvas::stopQuery).disabled(b -> !canvas.isQuerying());
                    buttons.button("@ok", Icon.ok, canvas::placeQuery).disabled(b -> !canvas.isQuerying());
                }

                setFillParent(true);
                margin(3f);

                add(titleTable).growX().fillY();
                row().add().grow();
                row().add(buttons).fill();
                addCloseListener();
            }}
        ).grow().pad(0f).margin(0f);
    }
}
