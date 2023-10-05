package coretest.tests.ui.tiledStructures;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mmc.ui.tiledStructures.TiledStructures.*;
import mmc.ui.tiledStructures.*;
import coretest.tests.ui.tiledStructures.TestStructures.*;

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

    public Element selectionTable = new Element(){
        boolean selecting;

        float prevX, prevY;

        {
            fillParent = true;
            System.out.println("fillParent: " + this);
            addListener(new InputListener(){


                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    System.out.println("touchDown");
                    localToStageCoordinates(Tmp.v1.set(x, y));
                    prevX = Tmp.v1.x;
                    prevY = Tmp.v1.y;
                    selecting = true;
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                    System.out.println("touchUp");
                    localToStageCoordinates(Tmp.v1.set(x, y));
                    canvas.tilemap.stageToLocalCoordinates(Tmp.v1);

                    int x1 = (int)(Tmp.v1.x / TiledStructuresCanvas.unitSize);
                    int y1 = (int)(Tmp.v1.y / TiledStructuresCanvas.unitSize);

                    Tmp.v1.set(prevX, prevY);
                    canvas.tilemap.stageToLocalCoordinates(Tmp.v1);

                    int x0 = (int)(Tmp.v1.x / TiledStructuresCanvas.unitSize);
                    int y0 = (int)(Tmp.v1.y / TiledStructuresCanvas.unitSize);
                    remove();
                    setSelection(x0, y0, x1, y1);
                    selecting = false;
                }
            });
            hidden(() -> remove());
        }

        @Override
        public void act(float delta){
            setWidth(parent.getWidth());
            setHeight(parent.getHeight());
            super.act(delta);
        }

        @Override
        public void draw(){
            if(selecting){
                Vec2 mouse = stageToLocalCoordinates(Core.scene.screenToStageCoordinates(Core.input.mouse()));
                Vec2 startPosition = stageToLocalCoordinates(Tmp.v1.set(prevX, prevY));

                float x = Math.min(mouse.x, startPosition.x);
                float y = Math.min(mouse.y, startPosition.y);
                float width = Math.abs(mouse.x - startPosition.x);
                float height = Math.abs(mouse.y - startPosition.y);
                Draw.color(Pal.accent, 0.5f);
                Fill.crect(x, y, width, height);
                Draw.color();
            }
        }
    };

    public TestTiledStructuresDialog(String title, Class<? extends TiledStructure> initClass){
        super(title, initClass);
    }

    private void setSelection(int x0, int y0, int x1, int y1){
        this.canvas.setSelection(Math.min(x0, x1), Math.min(y0, y1), Math.abs(x0 - x1), Math.abs(y0 - y1));

    }

    @Override
    protected void setupUI(Class<? extends TiledStructure> initClass){
        clear();
        margin(0f);

        stack(
            new Image(Styles.black5),
            canvas = new TiledStructuresCanvas(this),
            new Table(){{
                canvas.update(() -> {
                    canvas.originX = canvas.getWidth() / 2f;
                    canvas.originY = canvas.getHeight() / 2f;
                });
                canvas.setTransform(true);
                buttons.defaults().size(160f, 64f).pad(2f);
                buttons.button("@back", Icon.left, () -> hide());
              /*  buttons.button("@add", Icon.add, () -> getProvider(initClass).get(new TypeInfo(initClass), it -> {
                    if(canvas.isQuerying()){
                        canvas.addQuery(it);
                    }else{
                        canvas.beginQuery(it);
                    }
                }));*/
                buttons.button("@add", Icon.add, () -> {
                    if(canvas.isQuerying()){
                        canvas.addQuery(new TestStructure());
                    }else{
                        canvas.beginQuery(new TestStructure());
                    }
                });
                buttons.button("add 2", Icon.add, () -> {
                    TestStructure a = new TestStructure();
                    TestStructure b = new TestStructure();
                    a.addParent(b, 1, 0);
                    canvas.addQuery(a);
                    canvas.addQuery(b);
                });
                buttons.button("+", () -> {
                    float scale = (1f / 4) + 1;
                    canvas.scaleX *= scale;
                    canvas.scaleY *= scale;
                }).size(48f);
                buttons.button("-", () -> {
                    float scale = (-1f / 4) + 1;
                    canvas.scaleX *= scale;
                    canvas.scaleY *= scale;
                }).size(48f);
                buttons.button("select", Styles.flatTogglet, () -> {
                    if(!selectionTable.remove()){
                        Core.scene.add(selectionTable);
                        System.out.println("enabled");
                    }else{
                        System.out.println("disabled");
                    }
                }).checked(it -> selectionTable.parent != null);
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
