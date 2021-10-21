package mma.world.blocks.distribution;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Blocks;
import mindustry.ctype.Content;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Teamc;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Router;
import mindustry.world.meta.BlockGroup;
import mindustry.annotations.Annotations;

public class SmartRouter extends Router {

    public @Annotations.Load("@-cross")
    TextureRegion cross;
    public @Annotations.Load("@-arrow")
    TextureRegion arrow;

    public SmartRouter(String name) {
        super(name);
        this.solid = true;
        this.update = true;
        this.configurable = true;
        this.hasItems = true;
        this.itemCapacity = 1;
        this.group = BlockGroup.transportation;
        this.unloadable = false;
        this.noUpdateDisabled = true;
    }

    public class CustomRouterBuild extends Building {
        public Item lastItem;
        public Tile lastInput;
        public float time;
        boolean up = false, left = false, right = false, down = false;

        @Override
        public void updateTableAlign(Table table) {
            float addPos = Mathf.ceil(size / 2f) - 1;
            Vec2 pos = Core.input.mouseScreen((x) + addPos - 0.5f, y + addPos);
            table.setSize(size * 12f);
            table.setPosition(pos.x, pos.y, 0);
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4) {
            if (type == LAccess.config) {
                String lastConfig = config();
                try {
                    String config = (String) p1;
                    right = config.charAt(0) == '1';
                    up = config.charAt(1) == '1';
                    left = config.charAt(2) == '1';
                    down = config.charAt(3) == '1';
                    return;
                } catch (Exception ignored) {
                    handleString(lastConfig);
                }
                return;
            }
            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public Object senseObject(LAccess sensor) {
            if (sensor == LAccess.config) {
                return Strings.format("@@@@", Mathf.num(right), Mathf.num(up), Mathf.num(left), Mathf.num(down));
            }
            return super.senseObject(sensor);
        }

        @Override
        public void buildConfiguration(Table t) {
            t.add();
            t.button(Icon.up, () -> {
                up = !up;
                updateConfig();
            }).update((b) -> {
                b.setColor(up ? Color.lime : Color.valueOf("f25555"));
            });
            t.add().row();
            t.button(Icon.left, () -> {
                left = !left;
                updateConfig();
            }).update((b) -> {
                b.setColor(left ? Color.lime : Color.valueOf("f25555"));
            });
            t.add();
            t.button(Icon.right, () -> {
                right = !right;
                updateConfig();
            }).update((b) -> {
                b.setColor(right ? Color.lime : Color.valueOf("f25555"));
            });
            t.row();
            t.add();
            t.button(Icon.down, () -> {
                down = !down;
                updateConfig();
            }).update((b) -> {
                b.setColor(down ? Color.lime : Color.valueOf("f25555"));
            });
            t.add();
            /*
            table.button("@edit",()->{
                BaseDialog dialog=new BaseDialog("@edit");
                dialog.addCloseButton();
                dialog.show();
            });*/
//            super.buildConfiguration(table);
        }

        public void updateTile() {
            if (lastItem == null && items.any()) {
                lastItem = items.first();
            }

            if (lastItem != null) {
                time += 1.0F / speed * this.delta();
                Building target = this.getTileTarget(this.lastItem, this.lastInput, false);
                if (target != null && (this.time >= 1.0F || !(target.block instanceof Router) && !target.block.instantTransfer)) {
                    this.getTileTarget(this.lastItem, this.lastInput, true);
                    target.handleItem(this, this.lastItem);
                    this.items.remove(this.lastItem, 1);
                    this.lastItem = null;
                }
            }

        }

        private void drawSide(boolean bool, int side) {
            if (bool) {
                Draw.rect(arrow, this.x, this.y, side * 90);
            } else {
                Draw.rect(cross, this.x, this.y, side * 90);
            }
        }

        @Override
        public void draw() {
            super.draw();
            drawSide(up, 1);
            drawSide(left, 2);
            drawSide(right, 0);
            drawSide(down, 3);
        }

        public int acceptStack(Item item, int amount, Teamc source) {
            return 0;
        }

        public boolean acceptItem(Building source, Item item) {
            return this.team == source.team && this.lastItem == null && this.items.total() == 0 && acceptSide(source);
        }

        public void handleItem(Building source, Item item) {
            this.items.add(item, 1);
            this.lastItem = item;
            this.time = 0.0F;
            this.lastInput = source.tile();
        }

        public int removeStack(Item item, int amount) {
            int result = super.removeStack(item, amount);
            if (result != 0 && item == this.lastItem) {
                this.lastItem = null;
            }

            return result;
        }

        public Building getTileTarget(Item item, Tile from, boolean set) {
            int counter;
            counter = this.rotation;

            for (int i = 0; i < this.proximity.size; ++i) {
                Building other = this.proximity.get((i + counter) % this.proximity.size);
                if (set) {
                    this.rotation = (byte) ((this.rotation + 1) % this.proximity.size);
                }

                if ((other.tile != from || from.block() != Blocks.overflowGate) && other.acceptItem(this, item)) {
                    if (this.acceptSide(other)) return other;
                }
            }

            return null;

        }

        private boolean acceptSide(Building other) {

            if (left && other.x < x) return true;
            if (right && other.x > x) return true;
            if (down && other.y < y) return true;
            if (up && other.y > y) return true;
            return false;
        }

        @Override
        public String config() {
            return Strings.format("1r@@@@", Mathf.num(up), Mathf.num(down), Mathf.num(left), Mathf.num(right));
        }

        private void updateConfig() {
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.str(config() + "");
        }

        @Override
        public double sense(Content content) {
            return super.sense(content);
        }

        @Override
        public void playerPlaced(Object config) {
            if (lastConfig == null) {
                lastConfig = "1r0000";
            }
            handleString(config);
        }

        @Override
        public void handleString(Object obj) {
            try {
                String value = obj + "";
                if (value.contains(" ")) {
                    String[] bools = value.intern().split(" ");
                    if (bools.length == 4) {
                        up = Boolean.parseBoolean(bools[0]);
                        down = Boolean.parseBoolean(bools[1]);
                        left = Boolean.parseBoolean(bools[2]);
                        right = Boolean.parseBoolean(bools[3]);
                    }
                } else if (value.startsWith("1r")) {
                    String[] bools = value.substring(2).intern().split("");
//                    Log.info("@", Arrays.toString(bools));
                    if (bools.length == 4) {
                        up = bools[0].equals("1");
                        down = bools[1].equals("1");
                        left = bools[2].equals("1");
                        right = bools[3].equals("1");
                    }
                }
            } catch (Exception exception) {

            }
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            switch (revision) {
                case 0:
                    handleString(read.str());
                    break;
                case 1:
                    handleString(read.str());

                    break;
            }
        }
    }
}
