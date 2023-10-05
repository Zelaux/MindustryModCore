package mmc.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.annotations.*;
import mindustry.ctype.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.blocks.*;
import mindustry.world.meta.*;
import mmc.world.*;

import java.util.*;

import static mindustry.Vars.*;

public class SmartSorter extends ModBlock{
    public boolean invert;
    public @Annotations.Load("@-cross")
    TextureRegion crossRegion;
    public @Annotations.Load("@-item")
    TextureRegion itemRegion;

    public SmartSorter(String name){
        super(name);
        update = true;
        solid = true;
        instantTransfer = true;
        group = BlockGroup.transportation;
        configurable = true;
        unloadable = false;
        saveConfig = true;

        config(String.class, (SmartSorterBuild tile, String str) -> {
            tile.handleString(str);
        });
    }

    @Override
    public void drawPlanConfig(BuildPlan req, Eachable<BuildPlan> list){
        float x = req.drawx();
        float y = req.drawy();
        if(req.config == null) return;
        String[] strings = req.config.toString().split(" ");
        int[] sides = new int[4];
        if(strings.length == 4){
            for(int i = 0; i < strings.length; i++){
                sides[i] = Strings.parseInt(strings[i], -1);
            }
        }
        for(int i = 0; i < sides.length; i++){
            int side = sides[i];
            Item item = content.items().find(it -> {
                return it.id == side;
            });
            if(side != -1 && item != null){
                Draw.color(item.color);
                Draw.rect(itemRegion, x, y, i * 90);
            }else{
                Draw.rect(crossRegion, x, y, i * 90);
            }
        }
    }

    @Override
    public boolean outputsItems(){
        return true;
    }

    public class SmartSorterBuild extends Building{
        final int[] sides = new int[4];

        public SmartSorterBuild(){
            Arrays.fill(sides, -1);
        }

        @Override
        public Object senseObject(LAccess sensor){
            if(sensor == LAccess.config){
                return config();
            }
            return super.senseObject(sensor);
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4){
            if(type == LAccess.config){

                String lastConfig = config();
                try{
                    String config = (String)p1;
                    String[] split = config.split(" ");
                    if(split.length == 4){
                        for(int i = 0; i < split.length; i++){
                            String val = split[i];
                            if(Strings.canParseFloat(val)){
                                sides[i] = (int)Double.parseDouble(val);
                            }else{
                                MappableContent byName = content.getByName(ContentType.item, val);
                                if(byName == null){
                                    for(Item item : content.items()){
                                        if(item.localizedName.equals(val)){
                                            byName = item;
                                            break;
                                        }
                                    }
                                }
                                sides[i] = byName.id;
                            }
                        }
                    }
                    return;
                }catch(Exception ignored){
                    handleString(lastConfig);
                }
                return;
            }
            super.control(type, p1, p2, p3, p4);
        }

        public void configSide(int side, int item){
            sides[side] = item;
        }

        public void configSide(int side, Item item){
            configSide(side, item == null ? -1 : item.id);
        }

        @Override
        public void draw(){
            super.draw();
            for(int i = 0; i < sides.length; i++){
                int side = sides[i];
                if(side == -1 || content.item(side) == null){
                    Draw.rect(crossRegion, x, y, i * 90f);
                }else{
                    Draw.color(content.item(side).color);
                    Draw.rect(itemRegion, x, y, i * 90f);
                    Draw.color();
                }

            }
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            Building to = getTileTarget(item, source, false);

            return to != null && to.acceptItem(this, item) && to.team == team;
        }

        @Override
        public void handleItem(Building source, Item item){
            getTileTarget(item, source, true).handleItem(this, item);
        }

        public boolean isSame(Building other){
            return other != null && other.block.instantTransfer;
        }

        private boolean canAccept(Building building, Item item){
            int dir = building.relativeTo(this);
            int side = sides[Mathf.mod(dir + 2, 4)];
            return !invert == (side == item.id || side == -1);
        }

        public Building getTileTarget(Item item, Building source, boolean flip){
            int dir = source.relativeTo(tile.x, tile.y);
            if(dir == -1) return null;
            Building to;
            Building a = nearby(Mathf.mod(dir - 1, 4));
            Building b = nearby(Mathf.mod(dir, 4));
            Building c = nearby(Mathf.mod(dir + 1, 4));
            boolean ac = a != null && canAccept(a, item) && !(a.block.instantTransfer && source.block.instantTransfer) &&
                         a.acceptItem(this, item);
            boolean bc = b != null && canAccept(b, item) && !(b.block.instantTransfer && source.block.instantTransfer) &&
                         b.acceptItem(this, item);
            boolean cc = c != null && canAccept(c, item) && !(c.block.instantTransfer && source.block.instantTransfer) &&
                         c.acceptItem(this, item);

            if(ac && !bc && !cc){
                to = a;
            }else if(bc && !ac && !cc){
                to = b;
            }else if(cc && !ac && !bc){
                to = c;
            }else if(!ac && !bc && !cc){
                return null;
            }else{
                Seq<Building> candidates = new Seq<>();
                if(ac) candidates.add(a);
                if(bc) candidates.add(b);
                if(cc) candidates.add(c);
                to = candidates.get(rotation = Mathf.mod(rotation, candidates.size));
                /*if (ac && bc) {
                    to = rotation % 2 == 0 ? a : b;
                } else if(bc && cc){
                    to = rotation % 2 == 0 ? b : c;
                } else {
                    to = rotation % 2 == 0 ? c : a;
                }*/
                if(flip) rotation = Mathf.mod(rotation + 1, candidates.size);
            }
            return to;
        }

        @Override
        public void updateTableAlign(Table t){
            float addPos = Mathf.ceil(this.block.size / 2f) - 1;
            Vec2 pos = Core.input.mouseScreen((this.x) + addPos - 0.5f, this.y + addPos);
//            t.setSize(this.block.size * 12f);
            t.setPosition(pos.x, pos.y, 0);
        }

        private void addButton(Table t, int dir){
            TextureAtlas.AtlasRegion cross = Core.atlas.find("cross");
            ImageButton button = new ImageButton(new TextureRegionDrawable(cross));
            button.update(() -> {
                button.getStyle().imageUp = new TextureRegionDrawable(content.item(sides[dir]) == null ? cross : content.item(sides[dir]).fullIcon);
            });
            button.clicked(() -> {
                t.clearChildren();
                ItemSelection.buildTable(t, content.items(), () -> content.item(sides[dir]), newItem -> {
                    configSide(dir, newItem);
                    configure(config());
                    t.clearChildren();
                    buildConfiguration(t);

                    t.pack();
                });

                t.pack();
            });
            t.add(button).minSize(48f);
        }

        @Override
        public String config(){
            return sides[0] + " " + sides[1] + " " + sides[2] + " " + sides[3];
        }

        @Override
        public void handleString(Object value){
            String str = (String)value;
            String[] split = str.split(" ");
            for(int i = 0; i < split.length; i++){
                configSide(i, Strings.parseInt(split[i]));
            }
        }

        @Override
        public void buildConfiguration(Table t){
            t.add();
            addButton(t, 1);
            t.add().row();
            addButton(t, 2);
            t.add();
            addButton(t, 0);
            t.row();
            t.add();
            addButton(t, 3);
            t.add();
        }

        private void configure(){
            configure(config());
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            deselect();
            //                configure(null);
            return this != other;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(sides[0]);
            write.i(sides[1]);
            write.i(sides[2]);
            write.i(sides[3]);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            sides[0] = read.i();
            sides[1] = read.i();
            sides[2] = read.i();
            sides[3] = read.i();
        }
    }
}
