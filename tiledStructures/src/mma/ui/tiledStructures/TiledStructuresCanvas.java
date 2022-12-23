package mma.ui.tiledStructures;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.Nullable;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mma.ui.tiledStructures.TiledStructures.*;
import mma.ui.tiledStructures.TiledStructuresCanvas.StructureTilemap.StructureTile.*;
import mma.ui.tiledStructures.TiledStructuresDialog.*;
import org.jetbrains.annotations.*;

import static mindustry.Vars.mobile;

public class TiledStructuresCanvas extends WidgetGroup{
    public static final int
        /*objWidth = 4,*/ /*objHeight = 2,*/
        bounds = 100;
    public final TiledStructuresDialog tiledStructuresDialog;
    public final float unitSize = Scl.scl(48f);

    public Seq<TiledStructure> structures = new Seq<>();
    public StructureTilemap tilemap;
    protected TiledStructure query;
    private boolean pressed;
    private long visualPressed;
    private int queryX = -4, queryY = -2;

    public TiledStructuresCanvas(TiledStructuresDialog tiledStructuresDialog){
        this.tiledStructuresDialog = tiledStructuresDialog;
        setFillParent(true);
        addChild(tilemap = new StructureTilemap());

        addCaptureListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(query != null && button == KeyCode.mouseRight){
                    stopQuery();

                    event.stop();
                    return true;
                }else{
                    return false;
                }
            }
        });

        addCaptureListener(new ElementGestureListener(){
            int pressPointer = -1;

            @Override
            public void pan(InputEvent event, float x, float y, float deltaX, float deltaY){
                if(tilemap.moving != null || tilemap.connecting != null) return;
                tilemap.x = Mathf.clamp(tilemap.x + deltaX, -bounds * unitSize + width, bounds * unitSize);
                tilemap.y = Mathf.clamp(tilemap.y + deltaY, -bounds * unitSize + height, bounds * unitSize);
            }

            @Override
            public void tap(InputEvent event, float x, float y, int count, KeyCode button){
                if(query == null) return;

                Vec2 pos = localToDescendantCoordinates(tilemap, Tmp.v1.set(x, y));
                queryX = Mathf.round((pos.x - query.objWidth() * unitSize / 2f) / unitSize);
                queryY = Mathf.floor((pos.y - unitSize) / unitSize);

                // In mobile, placing the query is done in a separate button.
                if(!mobile) placeQuery();
            }

            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(pressPointer != -1) return;
                pressPointer = pointer;
                pressed = true;
                visualPressed = Time.millis() + 100;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(pointer == pressPointer){
                    pressPointer = -1;
                    pressed = false;
                }
            }
        });
    }

    public void clearObjectives(){
        stopQuery();
        tilemap.clearTiles();
    }

    public void stopQuery(){
        if(query == null) return;
        query = null;

        Core.graphics.restoreCursor();
    }

    public void query(TiledStructure obj){
        stopQuery();
        query = obj;
    }

    public void placeQuery(){
        if(isQuerying() && tilemap.createTile(queryX, queryY, query)){
            structures.add(query);
            stopQuery();
            updateStructures();
        }
    }

    @org.jetbrains.annotations.Nullable
    public TiledStructure getQuery(){
        return query;
    }

    public boolean isQuerying(){
        return query != null;
    }

    public boolean isVisualPressed(){
        return pressed || visualPressed > Time.millis();
    }

    public void updateStructures(){
        if(!tiledStructuresDialog.settings.updateStructuresOnChange || tiledStructuresDialog.originalStructures == null) return;


        tiledStructuresDialog.out.get(structures); //<-NPE where?!

        clearObjectives();
        structures.set(tiledStructuresDialog.originalStructures.get());
        for(TiledStructure<?> structure : structures){
            tilemap.createTile(structure.editorX, structure.editorY, structure);
        }
    }

    private void showEditDialog(TiledStructure<?> obj){
        BaseDialog dialog = new BaseDialog(tiledStructuresDialog.title.getText().toString());
        dialog.cont.pane(Styles.noBarPane, list -> list.top().table(e -> {
            e.margin(0f);
            tiledStructuresDialog.getInterpreter((Class<TiledStructure>)obj.getClass()).build(tiledStructuresDialog,
                e, obj.typeName(),
                new TypeInfo(obj.getClass()), null, null,
                null,
                () -> obj,
                res -> {
                    if(tiledStructuresDialog.settings.updateStructuresAfterConfig){
                        updateStructures();
                    }
                });
        }).width(400f).fillY()).grow();

        dialog.addCloseButton();
        dialog.show();
        dialog.hidden(() -> {
            if(!tiledStructuresDialog.settings.updateStructuresAfterConfig){
                updateStructures();
            }
        });
    }

    public class StructureTilemap extends WidgetGroup{

        /** The connector button that is being pressed. */
        protected @Nullable Connector connecting;
        /** The current tile that is being moved. */
        protected @Nullable StructureTile moving;

        public StructureTilemap(){
            setTransform(false);
            setSize(getPrefWidth(), getPrefHeight());
            touchable(() -> isQuerying() ? Touchable.disabled : Touchable.childrenOnly);
        }

        @Override
        public void draw(){
            validate();
            int minX = Math.max(Mathf.floor((x - width - 1f) / unitSize), -bounds), minY = Math.max(Mathf.floor((y - height - 1f) / unitSize), -bounds),
                maxX = Math.min(Mathf.ceil((x + width + 1f) / unitSize), bounds), maxY = Math.min(Mathf.ceil((y + height + 1f) / unitSize), bounds);
            float progX = x % unitSize, progY = y % unitSize;

            Lines.stroke(3f);
            Draw.color(Pal.darkestGray, parentAlpha);

            for(int x = minX; x <= maxX; x++) Lines.line(progX + x * unitSize, minY * unitSize, progX + x * unitSize, maxY * unitSize);
            for(int y = minY; y <= maxY; y++) Lines.line(minX * unitSize, progY + y * unitSize, maxX * unitSize, progY + y * unitSize);

            if(isQuerying()){
                int tx, ty;
                if(mobile){
                    tx = queryX;
                    ty = queryY;
                }else{
                    Vec2 pos = screenToLocalCoordinates(Core.input.mouse());
                    tx = Mathf.round((pos.x - query.objWidth() * unitSize / 2f) / unitSize);
                    ty = Mathf.floor((pos.y - unitSize) / unitSize);
                }

                Lines.stroke(4f);
                Draw.color(
                    isVisualPressed() ? Pal.metalGrayDark : validPlace(tx, ty, null, query) ? Pal.accent : Pal.remove,
                    parentAlpha
                );

                Lines.rect(x + tx * unitSize, y + ty * unitSize, query.objWidth() * unitSize, query.objHeight() * unitSize);
            }

            if(moving != null){
                int tx, ty;
                float x = this.x + (tx = Mathf.round(moving.x / unitSize)) * unitSize;
                float y = this.y + (ty = Mathf.round(moving.y / unitSize)) * unitSize;

                Draw.color(
                    validPlace(tx, ty, moving, moving.obj) ? Pal.accent : Pal.remove,
                    0.5f * parentAlpha
                );

                Fill.crect(x, y, moving.obj.objWidth() * unitSize, moving.obj.objHeight() * unitSize);
            }

            Draw.reset();
            super.draw();

            Draw.reset();
            Seq<StructureTile> tiles = getChildren().as();

            Connector conTarget = null;
            if(connecting != null){
                Vec2 pos = connecting.localToAscendantCoordinates(this, Tmp.v1.set(connecting.pointX, connecting.pointY));
                Element hit = hit(pos.x, pos.y, true);
                if(hit instanceof Connector con && connecting.canConnectTo(con)){
                    conTarget = con;
                }
            }

            boolean removing = false;
            for(var tile : tiles){
                for(var parent : tile.obj.inputWires){
                    var parentTile = tiles.find(t -> t.obj == parent.obj);

                    Connector
                        conFrom = parentTile.conChildren[parent.parentOutput],
                        conTo = tile.conParent[parent.input];

                    if(conTarget != null && (
                        (connecting.findParent && connecting == conTo && conTarget == conFrom) ||
                            (!connecting.findParent && connecting == conFrom && conTarget == conTo)
//                        (connecting.findParent && connecting.id < conTo.length && conTarget.id < conFrom.length && connecting == conTo[connecting.id] && conTarget == conFrom[conTarget.id]) ||
//                            (!connecting.findParent && connecting.id < conFrom.length && conTarget.id < conTo.length && connecting == conFrom[connecting.id] && conTarget == conTo[conTarget.id])
                    )){
                        removing = true;
                        continue;
                    }


                    Connector parentConnector = conFrom;//[parent.parentOutput];
                    Connector childConnector = conTo;//[parent.input];
                    if(conFrom.isDisabled() || conTo.isDisabled()) continue;
                    Vec2
                        from = parentConnector.localToAscendantCoordinates(this, Tmp.v1.set(parentConnector.getWidth() / 2f, parentConnector.getHeight() / 2f)).add(x, y),
                        to = childConnector.localToAscendantCoordinates(this, Tmp.v2.set(childConnector.getWidth() / 2f, childConnector.getHeight() / 2f)).add(x, y);

                    drawCurve(parent.obj.colorForInput(parent.parentOutput), from.x, from.y, to.x, to.y, parent.obj == tile.obj);
                }
            }

            if(connecting != null){
                Vec2
                    mouse = (conTarget == null
                                 ? connecting.localToAscendantCoordinates(this, Tmp.v1.set(connecting.pointX, connecting.pointY))
                                 : conTarget.localToAscendantCoordinates(this, Tmp.v1.set(conTarget.getWidth() / 2f, conTarget.getHeight() / 2f))
                ).add(x, y),
                    anchor = connecting.localToAscendantCoordinates(this, Tmp.v2.set(connecting.getWidth() / 2f, connecting.getHeight() / 2f)).add(x, y);

                Vec2
                    from = connecting.findParent ? mouse : anchor,
                    to = connecting.findParent ? anchor : mouse;

                drawCurve(removing, from.x, from.y, to.x, to.y, conTarget != null && connecting.tile().obj == conTarget.tile().obj);
            }

            Draw.reset();
        }

        protected void drawCurve(boolean remove, float x1, float y1, float x2, float y2, boolean selfConnecting){
            drawCurve(remove ? Pal.remove : Pal.accent, x1, y1, x2, y2, selfConnecting);
        }

        protected void drawCurve(Color color, float x1, float y1, float x2, float y2, boolean selfConnecting){


            Lines.stroke(4f);
            Draw.color(color, parentAlpha);

            Fill.square(x1, y1, 8f, 45f);
            Fill.square(x2, y2, 8f, 45f);

            float dist = Math.abs(x1 - x2) / 2f;
            float lerpProgress = Interp.pow5Out.apply(Mathf.clamp(Math.abs(y1 - y2) * 2 / unitSize));
//            float disty = lerpProgress;
            float disty;
            if(!selfConnecting){
                disty = 0;
            }else{
                float value = (y2 - y1) * 2;
                if(value == 0) value = unitSize / 2f;
                disty = Math.min(dist, Math.abs(value)) * Mathf.sign(value);
            }
            float cx1 = x1 + dist * (lerpProgress);
            float cx2 = x2 - dist * (lerpProgress);

            float cy1 = y1 + disty;
            float cy2 = y2 + disty;
            Lines.curve(x1, y1, cx1, cy1, cx2, cy2, x2, y2, Math.max(4, (int)(Mathf.dst(x1, y1, x2, y2) / 4f)));

            float progress = (Time.time % (60 * 4)) / (60 * 4);

            float t2 = progress * progress;
            float t3 = progress * t2;
            float t1 = 1 - progress;
            float t13 = t1 * t1 * t1;
            float kx1 = t13 * x1 + 3 * progress * t1 * t1 * cx1 + 3 * t2 * t1 * cx2 + t3 * x2;
            float ky1 = t13 * y1 + 3 * progress * t1 * t1 * cy1 + 3 * t2 * t1 * cy2 + t3 * y2;

            Fill.circle(kx1, ky1, 6f);

            Draw.reset();
        }

        public boolean validPlace(int x, int y, @Nullable StructureTile ignore, TiledStructure<?> structure){
            float offset = 0.f;
            Tmp.r1.set(x + offset, y + offset, structure.objWidth() - offset, structure.objHeight() - offset).grow(-0.001f);

            if(!Tmp.r2.setCentered(0, 0, bounds * 2, bounds * 2).contains(Tmp.r1)){
                return false;
            }

            for(var other : children){
                if(other instanceof StructureTile tile){
                    Rect collider = Tmp.r2.set(tile.tx + offset, tile.ty + offset, tile.obj.objWidth() - offset, tile.obj.objHeight() - offset);

                    if(tile != ignore && collider.overlaps(Tmp.r1)){
                        return false;
                    }
                }
            }

            return true;
        }

        public boolean createTile(TiledStructure obj){
            return createTile(obj.editorX, obj.editorY, obj);
        }

        public boolean createTile(int x, int y, TiledStructure obj){
            if(!validPlace(x, y, null, obj)) return false;

            StructureTile tile = new StructureTile(obj, x, y);
            tile.pack();

            addChild(tile);

            return true;
        }

        public boolean moveTile(StructureTile tile, int newX, int newY){
            if(!validPlace(newX, newY, tile, tile.obj)) return false;
            int editorX = tile.obj.editorX;
            int editorY = tile.obj.editorY;
            tile.pos(newX, newY);
            if(newX != editorX || newY != editorY) updateStructures();

            return true;
        }

        public void removeTile(StructureTile tile){
            if(!tile.isDescendantOf(this)) return;
            tile.remove();
            updateStructures();
        }

        public void clearTiles(){
            clearChildren();
        }

        @Override
        public float getPrefWidth(){
            return bounds * unitSize;
        }

        @Override
        public float getPrefHeight(){
            return bounds * unitSize;
        }

        public static class ConnectorStyle{
            public ButtonStyle inputStyle;
            public ButtonStyle outputStyle;

            public ConnectorStyle(ButtonStyle inputStyle, ButtonStyle outputStyle){
                this.inputStyle = inputStyle;
                this.outputStyle = outputStyle;
            }

            public ConnectorStyle(){
            }

            public static ConnectorStyle defaultStyle(){
                if(!Core.scene.hasStyle(ConnectorStyle.class)){
                    Core.scene.addStyle(ConnectorStyle.class, innerDefault());
                }
                return Core.scene.getStyle(ConnectorStyle.class);
            }

            @NotNull
            private static ConnectorStyle innerDefault(){
                float drawableScale = Reflect.get(TextureAtlas.class, Core.atlas, "drawableScale");
                Color fillColor = Tmp.c1.set(0x252525FF);
                ButtonStyle input = new ButtonStyle(){{
                    down = Tex.buttonSideLeftDown;
                    up = Tex.buttonSideLeft;
                    over = Tex.buttonSideLeftOver;
                    AtlasRegion region = Core.atlas.find("button-side-left-over");
                    Pixmap pixmap = Core.atlas.getPixmap(region).crop();

                    pixmap.each((x, y) -> {
                        if(pixmap.getA(x, y) > 0){
                            pixmap.set(x, y, fillColor);
                        }
                    });

                    int[] splits = region.splits;
                    NinePatch patch = new NinePatch(new Texture(pixmap), splits[0], splits[1], splits[2], splits[3]);
                    pixmap.dispose();
                    int[] pads = region.pads;
                    if(pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
                    disabled = new ScaledNinePatchDrawable(patch, drawableScale);
                }};
                ButtonStyle output = new ButtonStyle(){{
                    down = Tex.buttonSideRightDown;
                    up = Tex.buttonSideRight;
                    over = Tex.buttonSideRightOver;

                    AtlasRegion region = Core.atlas.find("button-side-right-over");
                    Pixmap pixmap = Core.atlas.getPixmap(region).crop();

                    pixmap.each((x, y) -> {
                        if(pixmap.getA(x, y) > 0){
                            pixmap.set(x, y, fillColor);
                        }
                    });

                    int[] splits = region.splits;
                    NinePatch patch = new NinePatch(new Texture(pixmap), splits[0], splits[1], splits[2], splits[3]);
                    pixmap.dispose();
                    int[] pads = region.pads;
                    if(pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
                    disabled = new ScaledNinePatchDrawable(patch, drawableScale);
                }};
                return new ConnectorStyle(input, output);
            }
        }

        public class StructureTile extends Table{
            public final TiledStructure<?> obj;
            public final Mover mover;
            public final Connector[] conParent, conChildren;
            public int tx, ty;

            public StructureTile(TiledStructure<?> obj, int x, int y){
                this.obj = obj;
                setTransform(false);
                setClip(false);
                int childrenSizes = Mathf.num(obj.inputConnections() > 0) + Mathf.num(obj.outputConnections() > 0);
                float scl = Scl.scl(1f);
                float oneUnitWidth = unitSize / scl;
                inputs:
                {
                    conParent = new Connector[obj.inputConnections()];
                    if(conParent.length > 0){
                        float height1 = unitSize * obj.objHeight() / scl;
                        table(inputButtons -> {
                            for(int i = 0; i < conParent.length; i++){
                                inputButtons.add(conParent[i] = new Connector(true, i))

                                    .growX().height(height1 / conParent.length);
                                inputButtons.row();
                                Tooltip tooltip = obj.inputConnectorTooltip(i);
                                if(tooltip != null) conParent[i].addListener(tooltip);
                            }
                        }).size(oneUnitWidth, height1);
                    }
                }
                table(Tex.whiteui, t -> {
                    float pad = (oneUnitWidth - 32f) / 2f - 4f;
                    t.margin(pad);
                    t.touchable(() -> Touchable.enabled);
                    t.setColor(Pal.gray);

                    boolean oneHeight = obj.objHeight() == 1;
                    Cons2<TiledStructuresDialog, Table> editor = oneHeight ? null : obj.editor();
                    t.table(label -> {
                        label.labelWrap(obj.typeName())
                            .style(Styles.outlineLabel)
                            .left().grow().get()
                            .setAlignment(Align.left);
                        if(editor != null || oneHeight){
                            label.button(Icon.trashSmall, () -> removeTile(this)).right().size(40f);
                            if(oneHeight && obj.hasFields()){
                                label.button(Icon.pencilSmall, () -> showEditDialog(obj)).disabled(!obj.hasFields()).size(40f);
                            }
                        }
                    }).grow();

                    t.row();
                    if(!oneHeight){

                        t.table(b -> {
                            if(editor != null){
                                editor.get(tiledStructuresDialog, b);
                            }else{
                                b.left().defaults().size(40f);
                                b.button(Icon.pencilSmall, () -> {
                                    showEditDialog(obj);
                                }).disabled(!obj.hasFields());
                                b.button(Icon.trashSmall, () -> removeTile(this)).right().size(40f);
                            }
                        }).left().grow();
                    }
                }).growX().height(unitSize * obj.objHeight() / scl).get().addCaptureListener(mover = new Mover());
                outputs:
                {
                    conChildren = new Connector[obj.outputConnections()];
                    if(conChildren.length > 0){
                        float height1 = unitSize * obj.objHeight() / scl;
                        table(outputButtons -> {
                            for(int i = 0; i < conChildren.length; i++){
                                outputButtons.add(conChildren[i] = new Connector(false, i)).growX().height(height1 / conChildren.length);
                                outputButtons.row();
                                Tooltip tooltip = obj.outputConnectorTooltip(i);
                                if(tooltip != null) conChildren[i].addListener(tooltip);
                            }
                        }).size(oneUnitWidth, height1);
                    }
                }

                setSize(getPrefWidth(), getPrefHeight());
                pos(x, y);
            }

            public void pos(int x, int y){

                obj.editorX = (tx = x);
                obj.editorY = (ty = y);
                this.x = x * unitSize;
                this.y = y * unitSize;
            }

            @Override
            public float getPrefWidth(){
                return obj.objWidth() * unitSize;
            }

            @Override
            public float getPrefHeight(){
                return obj.objHeight() * unitSize;
            }

            @Override
            public boolean remove(){
                if(super.remove()){
                    obj.inputWires.clear();

                    for(int i = 0; i < structures.size; i++){
                        TiledStructure<?> next = structures.get(i);
                        if(next == obj){
                            structures.remove(i);
                            i--;
                        }else{
                            next.inputWires.remove(wire -> wire.obj == obj);
                        }
                    }

                    return true;
                }else{
                    return false;
                }
            }

            public class Mover extends InputListener{
                public int prevX, prevY;
                public float lastX, lastY;

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(moving != null) return false;
                    moving = StructureTile.this;
                    moving.toFront();

                    prevX = moving.tx;
                    prevY = moving.ty;

                    // Convert to world pos first because the button gets dragged too.
                    Vec2 pos = event.listenerActor.localToStageCoordinates(Tmp.v1.set(x, y));
                    lastX = pos.x;
                    lastY = pos.y;
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer){
                    Vec2 pos = event.listenerActor.localToStageCoordinates(Tmp.v1.set(x, y));

                    moving.moveBy(pos.x - lastX, pos.y - lastY);
                    lastX = pos.x;
                    lastY = pos.y;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(!moveTile(moving,
                        Mathf.round(moving.x / unitSize),
                        Mathf.round(moving.y / unitSize)
                    )){
                        moving.pos(prevX, prevY);
                    }
                    moving = null;
                }
            }

            public class Connector extends Button{
                public final boolean findParent;
                public final int id;
                public float pointX, pointY;

                public Connector(boolean findParent, int id){
                    super(new ButtonStyle(findParent ? obj.connectorStyle().inputStyle : obj.connectorStyle().outputStyle));
                    if(findParent){
                        setDisabled(() -> !obj.enabledInput(id));
                    }else{
                        setDisabled(() -> !obj.enabledOutput(id));
                    }
                    this.findParent = findParent;
                    this.id = id;

                    clearChildren();

                    addCaptureListener(new InputListener(){
                        int conPointer = -1;

                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                            if(conPointer != -1 || isDisabled()) return false;
                            conPointer = pointer;

                            if(connecting != null) return false;
                            connecting = Connector.this;

                            pointX = x;
                            pointY = y;
                            return true;
                        }

                        @Override
                        public void touchDragged(InputEvent event, float x, float y, int pointer){
                            if(conPointer != pointer) return;
                            pointX = x;
                            pointY = y;
                        }

                        @Override
                        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                            if(conPointer != pointer || connecting != Connector.this) return;
                            conPointer = -1;

                            Vec2 pos = Connector.this.localToAscendantCoordinates(StructureTilemap.this, Tmp.v1.set(x, y));
                            if(StructureTilemap.this.hit(pos.x, pos.y, true) instanceof Connector con && con.canConnectTo(Connector.this)){
                                TiledStructure<?> otherObj = con.tile().obj;
                                if(findParent){
                                    if(!obj.inputWires.remove(wire -> wire.parentOutput == con.id && wire.input == Connector.this.id && wire.obj == otherObj)){
                                        obj.addParent(otherObj, Connector.this.id, con.id);
                                    }
                                }else{
                                    if(!otherObj.inputWires.remove(wire -> wire.input == con.id && wire.parentOutput == Connector.this.id && wire.obj == obj)){
                                        otherObj.addParent(obj, con.id, Connector.this.id);
                                    }
                                }
                                updateStructures();
                            }

                            connecting = null;
                        }
                    });
                }

                public boolean canConnectTo(Connector other){
                    return
                        findParent != other.findParent && !other.isDisabled()/* &&
                            tile() != other.tile()*/;
                }

                @Override
                public void draw(){
                    super.draw();
                    float cx = x + width / 2f;
                    float cy = y + height / 2f;

                    // these are all magic numbers tweaked until they looked good in-game, don't mind them.
                    Lines.stroke(3f, Pal.accent);
                    if(findParent){
                        Lines.line(cx, cy + 9f, cx + 9f, cy);
                        Lines.line(cx + 9f, cy, cx, cy - 9f);
                    }else{
                        Lines.square(cx, cy, 9f, 45f);
                    }
                }

                public StructureTile tile(){
                    return StructureTile.this;
                }

                @Override
                public boolean isPressed(){
                    return super.isPressed() || connecting == this;
                }

                @Override
                public boolean isOver(){
                    return super.isOver() && (connecting == null || connecting.canConnectTo(this));
                }
            }
        }
    }
}
