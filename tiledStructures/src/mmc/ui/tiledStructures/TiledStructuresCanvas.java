package mmc.ui.tiledStructures;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.graphics.gl.*;
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
import mmc.ui.tiledStructures.TiledStructures.*;
import mmc.ui.tiledStructures.TiledStructures.TiledStructure.*;
import mmc.ui.tiledStructures.TiledStructuresCanvas.StructureTilemap.*;
import mmc.ui.tiledStructures.TiledStructuresCanvas.StructureTilemap.StructureTile.*;
import mmc.ui.tiledStructures.TiledStructuresDialog.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static mindustry.Vars.mobile;

public class TiledStructuresCanvas extends WidgetGroup{
    public static final int
        /*objWidth = 4,*/ /*objHeight = 2,*/
        bounds = 100;
    public static final float unitSize = Scl.scl(48f);
    public final TiledStructuresDialog tiledStructuresDialog;
    public final StructureTileGroup selection = new StructureTileGroup();
    public final StructureTilemap queryTilemap = new StructureTilemap();
    protected final FrameBuffer frameBuffer = new FrameBuffer();
    @NotNull
    protected final TiledStructureGroup query = new TiledStructureGroup(){
        @Override
        public void add(TiledStructure<?> group, boolean hasPosition){
            super.add(group, hasPosition);

            queryTilemap.createTile(group);
        }

        @Override
        public void setPosition(int x, int y){
            super.setPosition(x, y);
            for(Element child : queryTilemap.getChildren()){
                if(!(child instanceof StructureTile tile)) continue;
                tile.pos(tile.obj.editorX, tile.obj.editorY);
            }
        }

        @Override
        public void clear(){
            super.clear();
            queryTilemap.clearTiles();
        }
    };
    public Seq<TiledStructure> structures = new Seq<>();
    public StructureTilemap tilemap;
    private boolean pressed;
    private long visualPressed;
//    private int queryX = -4, queryY = -2;

    public TiledStructuresCanvas(TiledStructuresDialog tiledStructuresDialog){
        this.tiledStructuresDialog = tiledStructuresDialog;
        setFillParent(true);
        addChild(tilemap = new StructureTilemap());

        addCaptureListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(query.any() && button == KeyCode.mouseRight){
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
                if(tilemap.moving.any() || tilemap.connecting != null) return;
                tilemap.x = Mathf.clamp(tilemap.x + deltaX, -bounds * unitSize + width, bounds * unitSize);
                tilemap.y = Mathf.clamp(tilemap.y + deltaY, -bounds * unitSize + height, bounds * unitSize);
            }

            @Override
            public void tap(InputEvent event, float x, float y, int count, KeyCode button){
                if(query.isEmpty()) return;

                Vec2 pos = localToDescendantCoordinates(tilemap, Tmp.v1.set(x, y));
                query.setPosition(
                    queryX(pos), queryY(pos)
                );
//                queryX = queryX(pos);
                //noinspection IntegerDivisionInFloatingPointContext
//                queryY = queryY(pos);

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

    private int queryX(Vec2 pos){
        return Mathf.round(floatQueryX(pos) / unitSize);
    }

    private float floatQueryX(Vec2 pos){
        return pos.x - query.width() * unitSize / 2f;
    }

    private int queryY(Vec2 pos){
        return Mathf.round(floatQueryY(pos) / unitSize);
    }

    private float floatQueryY(Vec2 pos){
        return pos.y - query.height() * unitSize / 2f;
    }

    public void clearObjectives(){
        stopQuery();
        tilemap.clearTiles();
    }

    public void stopQuery(){
        if(query.isEmpty()) return;
        query.clear();

        Core.graphics.restoreCursor();
    }

    public void beginQuery(TiledStructure obj){
        stopQuery();
        addQuery(obj);
    }

    public void addQuery(TiledStructure obj){
        query.add(obj, false);
    }

    public void placeQuery(){
        if(!isQuerying()) return;
        if(query.contains(it -> !tilemap.validPlace(it.editorX, it.editorY, null, it))){
            return;
        }
        for(TiledStructure<?> tiledStructure : query.list()){
            tilemap.createTile(tiledStructure.editorX, tiledStructure.editorY, tiledStructure);
            structures.add(tiledStructure);
        }
        stopQuery();
        updateStructures();
    }

    public TiledStructureGroup getQuery(){
        return query;
    }

    public boolean isQuerying(){
        return query.any();
    }

    public boolean isVisualPressed(){
        return pressed || visualPressed > Time.millis();
    }

    public void updateStructures(){
        if(!tiledStructuresDialog.settings.updateStructuresOnChange || tiledStructuresDialog.originalStructures == null) return;


        tiledStructuresDialog.out.get(structures);

        clearObjectives();
        structures.set(tiledStructuresDialog.originalStructures.get());
        LongMap<StructureTile> newTiles = new LongMap<>();
        for(TiledStructure<?> structure : structures){
            tilemap.createTile(structure.editorX, structure.editorY, structure);
            StructureTile tile = (StructureTile)tilemap.getChildren().peek();
            newTiles.put(Pack.longInt(tile.tx, tile.ty), tile);
        }

        Func<StructureTile, StructureTile> updater = it -> {
            return newTiles.get(Pack.longInt(it.tx, it.ty));
        };
        selection.list().replace(updater);
        tilemap.moving.list().replace(updater);
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

    public void moveSelection(StructureTile head){
        tilemap.moving.set(selection);
        for(int i = 0; i < tilemap.moving.list().size; i++){
            tilemap.moving.list().get(i).toFront();
        }
//        Seq<StructureTile> list = tilemap.moving.list();
//        list.swap(list.indexOf(head), 0);
    }

    public void setSelection(int x, int y, int width, int height){
        selection.clear();
        for(Element it : tilemap.getChildren()){

            if(!(it instanceof StructureTile tile)){
                continue;
            }
            Tmp.r1.set(x + 0.0001f, y + 0.0001f, width - 0.0001f, height - 0.0001f);
            Tmp.r2.set(tile.tx + 0.0001f, tile.ty + 0.0001f, tile.obj.objWidth() - 0.0001f, tile.obj.objHeight() - 0.0001f);


            if(Tmp.r1.overlaps(Tmp.r2)){
                selection.add(tile);
            }
        }

    }

    public int roundCords(float cords){
        return Mathf.round(cords);
    }

    public class StructureTilemap extends WidgetGroup{

        /** The current tile that is being moved. */
        protected final StructureTileGroup moving = new StructureTileGroup();
        /** The connector button that is being pressed. */
        protected @Nullable Connector connecting;

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
                for(TiledStructure<?> queryStructures : query.list()){
                    int tx, ty;
                    if(mobile){
                        tx = queryStructures.editorX;
                        ty = queryStructures.editorY;
                    }else{
                        Vec2 pos = screenToLocalCoordinates(Core.input.mouse());
                        int qx = TiledStructuresCanvas.this.query.x();
                        int qy = TiledStructuresCanvas.this.query.y();
                        int queryX = queryX(pos);
                        int queryY = queryY(pos);
                        tx = queryX + queryStructures.editorX - qx;
                        ty = queryY + queryStructures.editorY - qy;
                    }

                    Lines.stroke(4f);
                    Draw.color(
                        isVisualPressed() ? Pal.metalGrayDark : validPlace(tx, ty, null, queryStructures) ? Pal.accent : Pal.remove,
                        parentAlpha
                    );

                    Lines.rect(x + tx * unitSize, y + ty * unitSize, queryStructures.objWidth() * unitSize, queryStructures.objHeight() * unitSize);
                }
                frameBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
                frameBuffer.begin(Color.clear);
                Seq<StructureTile> queryTiles = queryTilemap.getChildren().as();
                for(StructureTile tile : queryTiles){
                    TiledStructure<?> queryStructure = tile.obj;
                    float drawX;
                    float drawY;
                    if(mobile){
                        drawX = queryStructure.editorX * unitSize;
                        drawY = queryStructure.editorY * unitSize;
                    }else{
                        Vec2 pos = screenToLocalCoordinates(Core.input.mouse());
                        int qx = TiledStructuresCanvas.this.query.x();
                        int qy = TiledStructuresCanvas.this.query.y();

                        float queryX = floatQueryX(pos);
                        float queryY = floatQueryY(pos);
                        drawX = queryX + unitSize * (queryStructure.editorX - qx);
                        drawY = queryY + unitSize * (queryStructure.editorY - qy);
                    }
                    tile.x = x + drawX;
                    tile.y = y + drawY;
                    tile.pack();
                    tile.draw();
                }
                for(StructureTile tile : queryTiles){
                    TiledStructure<?> queryStructure = tile.obj;
                    float queryX;
                    float queryY;
                    if(mobile){
                        queryX = query.x() * unitSize;
                        queryY = query.y() * unitSize;
                    }else{
                        Vec2 pos = screenToLocalCoordinates(Core.input.mouse());
                        queryX = floatQueryX(pos);
                        queryY = floatQueryY(pos);
                    }
                    for(ConnectionWire<?> inputWire : queryStructure.inputWires){
                        StructureTile parentTile = queryTiles.find(it -> inputWire.obj == it.obj);
                        drawWire(tile, parentTile, inputWire, false);
                    }
                }

                frameBuffer.end();
                Tmp.m1.set(Draw.proj());
                Tmp.m2.set(Draw.trans());
                Draw.proj(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
                Draw.trans().idt();
                Draw.color(Color.white, Mathf.absin(5, 0.5f) + 0.25f);
                Draw.rect(Draw.wrap(frameBuffer.getTexture()), Core.graphics.getWidth() / 2f, Core.graphics.getHeight() / 2f, Core.graphics.getWidth(), -Core.graphics.getHeight());
                Draw.proj(Tmp.m1);
                Draw.trans(Tmp.m2);
            }

            if(moving.any()){
                moving.each(moving -> {
                    int tx, ty;
                    float x = this.x + (tx = roundCords(moving.x / unitSize)) * unitSize;
                    float y = this.y + (ty = roundCords(moving.y / unitSize)) * unitSize;

                    Draw.color(
                        validPlace(tx, ty, StructureTilemap.this.moving, moving.obj) ? Pal.accent : Pal.remove,
                        0.5f * parentAlpha
                    );

                    Fill.crect(x, y, moving.obj.objWidth() * unitSize, moving.obj.objHeight() * unitSize);
                });
            }

            if(moving.isEmpty()){
                for(StructureTile tile : selection.list()){
                    int tx, ty;
                    float x = this.x + (tx = tile.obj.editorX) * unitSize;
                    float y = this.y + (ty = tile.obj.editorY) * unitSize;

                    Draw.color(
                        Pal.accent,
                        0.5f * parentAlpha
                    );

                    Fill.crect(x, y, tile.obj.objWidth() * unitSize, tile.obj.objHeight() * unitSize);
                }
                Draw.reset();
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
                    if(tiledStructuresDialog.settings.ignoreEmptyWires && parentTile == null) continue;
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


                    drawWire(tile, parentTile, parent, true);
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

        private void drawWire(StructureTile tile, StructureTile parentTile, ConnectionWire<?> parent, boolean shouldOffset){
            Connector
                conFrom = parentTile.conChildren[parent.parentOutput],
                conTo = tile.conParent[parent.input];
            if(conFrom.isDisabled() || conTo.isDisabled()) return;
            Vec2
                from = conFrom.localToAscendantCoordinates(this, Tmp.v1.set(conFrom.getWidth() / 2f, conFrom.getHeight() / 2f)),
                to = conTo.localToAscendantCoordinates(this, Tmp.v2.set(conTo.getWidth() / 2f, conTo.getHeight() / 2f));
            if(shouldOffset){
                from.add(x, y);
                to.add(x, y);
            }

            drawCurve(parent.obj.colorForInput(parent.parentOutput), from.x, from.y, to.x, to.y, parent.obj == tile.obj);
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

        public boolean validPlace(int x, int y, @Nullable StructureTileGroup ignore, TiledStructure<?> structure){
            float offset = 0.f;
            Tmp.r1.set(x + offset, y + offset, structure.objWidth() - offset, structure.objHeight() - offset).grow(-0.001f);

            if(!Tmp.r2.setCentered(0, 0, bounds * 2, bounds * 2).contains(Tmp.r1)){
                return false;
            }

            for(var other : children){
                if(other instanceof StructureTile tile && tile.obj != structure){
                    Rect collider = Tmp.r2.set(tile.tx + offset, tile.ty + offset, tile.obj.objWidth() - offset, tile.obj.objHeight() - offset);

                    if(!(ignore != null && ignore.contains(tile)) && collider.overlaps(Tmp.r1)){
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

        public boolean moveTile(StructureTile tile, StructureTileGroup ignore, int newX, int newY){
            if(!validPlace(newX, newY, ignore, tile.obj)) return false;
            int editorX = tile.obj.editorX;
            int editorY = tile.obj.editorY;
            tile.pos(newX, newY);
            if(newX != editorX || newY != editorY) updateStructures();

            return true;
        }

        public void removeTile(StructureTile tile){
            if(!tile.isDescendantOf(this)) return;
            selection.remove(tile);
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
                this.obj = Objects.requireNonNull(obj, "obj cannot be null");
                setTransform(false);
                setClip(false);
                int childrenSizes = Mathf.num(obj.inputConnections() > 0) + Mathf.num(obj.outputConnections() > 0);
                float scl = Scl.scl(1f);
                float maxConnectorSize = unitSize / scl;
                float minConnectorSize = unitSize / scl / 2f;
                {
                    conParent = new Connector[obj.inputConnections()];
                    if(conParent.length > 0){
                        float height1 = unitSize * obj.objHeight() / scl;
                        float connectorHeight = height1 / conParent.length;
                        float connectorWidth = Mathf.clamp(connectorHeight, minConnectorSize, maxConnectorSize);
                        table(inputButtons -> {
                            for(int i = 0; i < conParent.length; i++){
                                inputButtons.add(conParent[i] = new Connector(true, i))
                                    .growX().size(connectorWidth, connectorHeight)
                                    .name("input-connector-" + i)
                                ;
                                inputButtons.row();
                                Tooltip tooltip = obj.inputConnectorTooltip(i);
                                if(tooltip != null) conParent[i].addListener(tooltip);
                            }
                        }).size(connectorWidth, height1)
                            .name("input-connectors")
                        ;
                    }
                }
                table(Tex.whiteui, t -> {
                    float pad = (unitSize / scl - 32f) / 2f - 4f;
                    t.margin(pad);
                    t.touchable(() -> Touchable.enabled);
                    t.setColor(Pal.gray);

                    boolean oneHeight = obj.objHeight() == 1;
                    Cons2<TiledStructuresDialog, Table> editor = oneHeight ? null : obj.editor();
                    t.table(label -> {
                        label.labelWrap(obj.typeName())
                            .style(Styles.outlineLabel)
                            .name("name-label")
                            .left().grow().get()
                            .setAlignment(Align.left);
                        if(editor != null || oneHeight){
                            label.button(Icon.trashSmall, () -> removeTile(this))
                                .right()
                                .size(40f)
                                .name("remove-button")
                            ;
                            if(oneHeight && obj.hasFields()){
                                label.button(Icon.pencilSmall, () -> showEditDialog(obj)).disabled(!obj.hasFields()).size(40f).name("edit-button");
                            }
                        }
                    }).name("label-table").grow();

                    t.row();
                    if(!oneHeight){

                        t.table(b -> {
                            if(editor != null){
                                editor.get(tiledStructuresDialog, b);
                            }else{
                                b.left().defaults().size(40f);
                                b.button(Icon.pencilSmall, () -> {
                                    showEditDialog(obj);
                                }).disabled(!obj.hasFields()).name("edit-button");
                                b.button(Icon.trashSmall, () -> removeTile(this)).right().size(40f)
                                    .name("remove-button");
                            }
                        }).left().grow();
                    }
                }).growX().height(unitSize * obj.objHeight() / scl)
                    .name("center-table")
                    .get().addCaptureListener(mover = new Mover());
                {
                    conChildren = new Connector[obj.outputConnections()];
                    if(conChildren.length > 0){
                        float height1 = unitSize * obj.objHeight() / scl;
                        float connectorHeight = height1 / conChildren.length;
                        float connectorWidth = Mathf.clamp(connectorHeight, minConnectorSize, maxConnectorSize);
                        table(outputButtons -> {
                            for(int i = 0; i < conChildren.length; i++){
                                outputButtons.add(conChildren[i] = new Connector(false, i)).growX().height(height1 / conChildren.length)
                                    .growX().size(connectorWidth, connectorHeight).name("output-connector-" + i);
                                outputButtons.row();
                                Tooltip tooltip = obj.outputConnectorTooltip(i);
                                if(tooltip != null) conChildren[i].addListener(tooltip);
                            }
                        }).size(connectorWidth, height1).name("output-connectors");
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
                    if(selection.contains(StructureTile.this)){
                        TiledStructuresCanvas.this.moveSelection(StructureTile.this);
//                        return true;
                    }else{
                        selection.clear();
                        if(moving.any()) return false;
                        moving.add(StructureTile.this);
                        moving.get(0).toFront();
                    }

                    prevX = moving.structureX();
                    prevY = moving.structureY();

                    // Convert to world pos first because the button gets dragged too.
                    Vec2 pos = event.listenerActor.localToStageCoordinates(Tmp.v1.set(x, y));
                    tilemap.stageToLocalCoordinates(pos);
                    lastX = pos.x;
                    lastY = pos.y;
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer){
                    Vec2 pos = event.listenerActor.localToStageCoordinates(Tmp.v1.set(x, y));
                    tilemap.stageToLocalCoordinates(pos);


                    moving.moveBy(pos.x - lastX, pos.y - lastY);
                    lastX = pos.x;
                    lastY = pos.y;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(moving.contains(moving -> !validPlace(roundCords(moving.x / unitSize),
                        roundCords(moving.y / unitSize), StructureTilemap.this.moving, moving.obj
                    ))){
                        moving.setPosition(prevX, prevY);
                    }else if(moving.any()){
                        int prevX = moving.get(0).tx;
                        int prevY = moving.get(0).ty;
                        moving.setPosition(roundCords(moving.x() / unitSize), roundCords(moving.y() / unitSize));
                        if(prevX != moving.get(0).tx || prevY != moving.get(0).ty){
                            updateStructures();
                        }
                    }
                    if(moving.list().size > 1){
                        selection.set(moving);
                    }else{
                        selection.clear();
                    }
                    moving.clear();
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
                    Draw.alpha(parentAlpha);
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
