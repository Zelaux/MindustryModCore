package mmc.world.blocks.distribution;

import arc.Core;
import arc.func.Boolf;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.IntSeq;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.Time;
import arclibrary.graphics.*;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.Placement;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mmc.gen.*;
import mmc.graphics.ALines;

import static arc.util.Tmp.v1;
import static arc.util.Tmp.v2;
import static mindustry.Vars.*;

public class CrossItemBridge extends ItemBridge {
    public Prov<Seq<Block>> connectBlocksGetter = () -> new Seq<>();
    Seq<Block> connectibleBlocks = new Seq<>();
    public Boolf<Building> connectFilter = (building) -> connectibleBlocks.contains(building.block());
    byte maxConnections = 10;

    public CrossItemBridge(String name) {
        super(name);
    }

    CrossItemBridge cast(Block b) {
        return (CrossItemBridge) b;
    }

    CrossItemBridgeBuild cast(Building b) {
        return (CrossItemBridgeBuild) b;
    }

    @Override
    public void init() {
        super.init();
        Seq<Block> connectibleBlocks = connectBlocksGetter.get();
        if (connectibleBlocks == null) connectibleBlocks = new Seq<>();
        connectibleBlocks.add(this);
        this.connectibleBlocks = connectibleBlocks;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, this.range, StatUnit.blocks);
        stats.add(Stat.powerConnections, this.maxConnections, StatUnit.none);
    }

    @Override
    public void load(){
        super.load();
        ModContentRegions.loadRegions(this);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("connections", (CrossItemBridgeBuild entity) -> {
            return new Bar(() -> {
                //in bundle: Connections: {0}/{1}
                return Core.bundle.format("bar.cross-item-bridge-lines", cast(entity).realConnections(), this.maxConnections);
            }, () -> Pal.items, () -> (float) cast(entity).realConnections() / (float) this.maxConnections);
        });
    }

    @Override
    public void drawBridge(BuildPlan req, float ox, float oy, float flip) {
        Lines.stroke(8.0F);
        v1.set(ox, oy).sub(req.drawx(), req.drawy()).setLength(4.0F);
//        Angles.angle(req.drawx(),req.drawy(),ox,oy)
        Lines.line(bridgeRegion, req.drawx() + v1.x, req.drawy() + v1.y, ox - v1.x, oy - v1.y, false);
        Draw.rect(arrowRegion, (req.drawx() + ox) / 2.0F, (req.drawy() + oy) / 2.0F, Angles.angle(req.drawx(), req.drawy(), ox, oy) + flip);
    }

    public Tile findLink(int x, int y) {
        return findLink(x, y, true);
    }

    public Tile findLink(int x, int y, boolean checkBlock) {
        Tile tile = Vars.world.tile(x, y);
        if (checkBlock) {
            if (tile != null && this.lastBuild != null && this.linkValid(tile, this.lastBuild.tile) && this.lastBuild.tile != tile)
                return this.lastBuild.tile;
        } else {
            if (tile != null && this.lastBuild != null && this.linkValid(tile, this.lastBuild.tile, false, true) && this.lastBuild.tile != tile)
                return this.lastBuild.tile;
        }
        return null;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        Tile link = this.findLink(x, y, false);
        Lines.stroke(2.0F, Pal.placing);
        Lines.dashCircle(x * 8f, y * 8f, range * 8f);

        Draw.reset();
        Draw.color(Pal.placing);
        Lines.stroke(1.0F);
        if (link != null && (link.x != x || link.y != y)) {
            final float angle = Angles.angle(link.x, link.y, x, y) + 90;
            float w = 8f;
            float h = Mathf.dst(link.x, link.y, x, y) * 8f - 8f;
            Lines.stroke(1.0F);
            float rectX = (float) (x + link.x) / 2.0F * 8.0F - w / 2.0F,
                    rectY = (float) (y + link.y) / 2.0F * 8.0F - h / 2.0F;
//            Lines.poly(ALines.rotRect(rectX, rectY, w, h, angle).toArray(Vec2.class), 0, 0, 1f);
            ELines.rect(rectX, rectY, w, h, angle);
            v1.set(x, y).sub(link.x, link.y).setLength(4.0F).scl(-1.0F);
            Vec2 arrowOffset = new Vec2(v1).scl(1f).setLength(1f);
            Draw.rect("bridge-arrow", link.x * 8f - arrowOffset.x * 8f, link.y * 8f - arrowOffset.y * 8f, angle - 90f);
        }

        Draw.reset();
    }

    @Override
    public TextureRegion[] getGeneratedIcons() {
        return super.getGeneratedIcons();
    }

    @Override
    public boolean linkValid(Tile tile, Tile other) {
        return linkValid(tile, other, true);
    }

    @Override
    public boolean linkValid(Tile tile, Tile other, boolean checkDouble) {
        return linkValid(tile, other, checkDouble, false);
    }

    public boolean linkValid(Tile tile, Tile other, boolean checkDouble, boolean old) {
        if (old) {
            if (other != null && tile != null && this.positionsValid(tile.x, tile.y, other.x, other.y)) {
                return (other.block() == tile.block() && tile.block() == this || !(tile.block() instanceof ItemBridge) && other.block() == this) && (other.team() == tile.team() || tile.block() != this) && (!checkDouble || ((ItemBridgeBuild) other.build).link != tile.pos());
            } else {
                return false;
            }
        } else {
            check:
            {
                if (!(other != null && tile != null) || other.build == null || tile.build == null) break check;
                other = other.build.tile;
                tile = tile.build.tile;
                int offset = other.block().isMultiblock() ? Mathf.floor(other.block().size / 2f) : 0;
                boolean b2 = tile.pos() != other.pos();
                if (tile.block() == this) {
                    Vec2 offVec = v1.trns(tile.angleTo(other) + 90f, offset, offset);
                    if (!positionsValid(tile.x, tile.y, Mathf.ceil(other.x + offVec.x), Mathf.ceil(other.y + offVec.y)))
                        break check;
                    CrossItemBridge block = (CrossItemBridge) tile.block();
                    boolean connected = false;
                    if (other.build instanceof ItemBridgeBuild) {
                        connected = other.build.<ItemBridgeBuild>as().link == tile.pos();
                    }
                    return ((block.connectFilter.get(other.build)) || !(tile.block() instanceof ItemBridge) && other.block() == this) &&
                           b2 &&
                           (other.team() == tile.team() || other.block() != this) &&

                           (!checkDouble || !connected);
                } else {
                    if (!positionsValid(tile.x, tile.y, other.x, other.y)) break check;
                    boolean b3 = other.team() == tile.team() || tile.block() != this;
                    if (other.block() == this) {
                        CrossItemBridge block = (CrossItemBridge) other.block();
                        boolean b1 = true;
                        boolean b4 = !checkDouble || !(other.build instanceof ItemBridgeBuild && ((ItemBridgeBuild) other.build).link == tile.pos());
                        return b1 &&
                               b2 &&
                               b3 &&
                               b4;
                    } else {
                        return (other.block() == tile.block() && tile.block() == this || !(tile.block() instanceof ItemBridge) && other.block() == this)
                               && b3 &&
                               (!checkDouble || ((ItemBridgeBuild) other.build).link != tile.pos());
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean positionsValid(int x1, int y1, int x2, int y2) {
        return Mathf.within(x1, y1, x2, y2, range + 0.5f);
    }

    public boolean positionsValid(Position pos, Position other) {
        return positionsValid((int) pos.getX(), (int) pos.getY(), (int) other.getX(), (int) other.getY());
    }

    public boolean positionsValid(Point2 pos, Point2 other) {
        return positionsValid(pos.x, pos.y, other.x, other.y);
    }

    public void changePlacementPath(Seq<Point2> points, int rotation) {
        Placement.calculateNodes(points, this, rotation, (point, other) -> {
            return positionsValid(point, other);
        });
    }

    public class CrossItemBridgeBuild extends ItemBridgeBuild  {
        public void drawBase() {
            Draw.rect(this.block.region, this.x, this.y, this.block.rotate ? this.rotdeg() : 0.0F);
            this.drawTeamTop();
        }

        public boolean acceptIncoming(int pos) {
            if ((incoming.size + (link == -1 ? 0 : 1) < maxConnections) && !incoming.contains(pos)) incoming.add(pos);
            return incoming.contains(pos);
        }

        @Override
        public void checkIncoming() {

            Tile other;
            for (int i : incoming.toArray()) {
                other = Vars.world.tile(i);
                boolean valid = linkValid(this.tile, other, false) && (other.build instanceof ItemBridgeBuild && ((ItemBridgeBuild) other.build).link == this.tile.pos());
                if (!valid) {
                    incoming.removeValue(i);
                }
            }
        }

        public int realConnections() {
            return incoming.size + (link == -1 ? 0 : 1);
        }

        public boolean canLinked() {
            return (realConnections() < maxConnections);
        }

        public boolean canReLink() {
            return (realConnections() <= maxConnections && link != -1);
        }

        public boolean onConfigureTileTapped(Building other) {
            if (other instanceof ItemBridgeBuild && ((ItemBridgeBuild) other).link == this.pos()) {
                incoming.removeValue(other.pos());
                other.<ItemBridgeBuild>as().incoming.add(this.pos());
                this.configure(other.pos());
                other.configure(-1);
            } else if (linkValid(this.tile, other.tile)) {
                if (this.link == other.pos()) {
                    if (other instanceof ItemBridgeBuild) other.<ItemBridgeBuild>as().incoming.removeValue(this.pos());
                    incoming.add(other.pos());
                    this.configure(-1);
                } else if (!(other instanceof CrossItemBridgeBuild && !cast(other).canLinked()) && (canLinked() || canReLink())) {
                    if (other instanceof ItemBridgeBuild) other.<ItemBridgeBuild>as().incoming.add(this.pos());
                    incoming.removeValue(other.pos());
                    this.configure(other.pos());
                }

                return false;
            }

            return true;
        }

        @Override
        public void updateTile() {
            incoming.size = Math.min(incoming.size, maxConnections - (link == -1 ? 0 : 1));
            incoming.shrink();
            Building linkBuilding = Vars.world.build(link);
            if (linkBuilding != null) {
                configureAny(linkBuilding.pos());
            }

            if (timer(timerCheckMoved, 30f)) {
                wasMoved = moved;
                moved = false;
            }
            time += wasMoved ? delta() : 0f;

            checkIncoming();

            Tile other = world.tile(link);
            if (!linkValid(tile, other)) {
                doDump();
                warmup = 0f;
            } else {
                if (other.build instanceof ItemBridgeBuild) {
                    if (other.build instanceof CrossItemBridgeBuild && !cast(other.build).acceptIncoming(this.tile.pos())) {
                        configureAny(-1);
                        return;
                    }
                }

                IntSeq inc = ((ItemBridgeBuild) other.build).incoming;
                int pos = tile.pos();
                if (!inc.contains(pos)) {
                    inc.add(pos);
                }

                warmup = Mathf.approachDelta(warmup, efficiency(), 1f / 30f);
                updateTransport(other.build);
            }

        }

        public void doDump() {
            //allow dumping multiple times per frame
            dumpAccumulate();
        }

        public void draw() {
            drawBase();

            Draw.z(Layer.power);

            Tile other = Vars.world.tile(link);
            Building build = Vars.world.build(link);
            if (build == this) build = null;
            if (build != null) other = build.tile;
            if (!linkValid(this.tile, other) || build == null || Mathf.zero(Renderer.bridgeOpacity)) return;
            final float angle = Angles.angle(x, y, build.x, build.y);
            v1.trns(angle, tilesize / 2f);
            float len1 = (size * tilesize) / 2.0F - 1.5F;
            float len2 = (build.block.size * tilesize) / 2.0F - 1.5F;
            final float x = this.x + Angles.trnsx(angle, len1), y = this.y + Angles.trnsy(angle, len1);
            final float x2 = build.x - Angles.trnsx(angle, len2), y2 = build.y - Angles.trnsy(angle, len2);
//            Draw.color(Color.white);
            if (pulse) {
                Draw.color(Color.white, Color.black, Mathf.absin(Time.time, 6f, 0.07f));
            }

            Draw.alpha(Math.max(warmup, 0.25F) * Renderer.bridgeOpacity);
            Draw.rect(endRegion, x - v1.x, y - v1.y, angle + 90);
            Draw.rect(endRegion, x2 + v1.x, y2 + v1.y, angle + 270f);
            Lines.stroke(8.0F);

            Lines.line(bridgeRegion,
                    x,
                    y,
                    x2,
                    y2,
                    false);
            int dist = ((int) Mathf.dst(x - v1.x, y - v1.y, x2, y2) / tilesize) - 1;
            Draw.color();
            int arrows = (int) ((dist * tilesize - arrowOffset - tilesize / 2f) / arrowSpacing);
            v2.trns(angle - 45f, 1f, 1f);
            for (float a = 0; a < arrows; ++a) {
                Draw.alpha(Mathf.absin(a - time / arrowTimeScl, arrowPeriod, 1f) * warmup * Renderer.bridgeOpacity);
                float arrowX, arrowY;
                arrowX = x - v1.x + v2.x * (tilesize / 2f + a * arrowSpacing + arrowOffset);
                arrowY = y - v1.y + v2.y * (tilesize / 2f + a * arrowSpacing + arrowOffset);
                Draw.rect(arrowRegion, arrowX, arrowY,
                        angle);
            }
            Draw.reset();


        }

        public void drawSelect() {
            if (linkValid(tile, Vars.world.tile(link))) {
                drawInput(Vars.world.tile(link));
            }

            for (int pos : incoming.items) {
                drawInput(Vars.world.tile(pos));
            }
            Draw.reset();
        }

        protected void drawInput(Tile other) {
            if (linkValid(this.tile, other, false)) {
                boolean linked = other.pos() == this.link;
                final float angle = tile.angleTo(other);
                v2.trns(angle, 2.0F);
                float tx = tile.drawx();
                float ty = tile.drawy();
                float ox = other.drawx();
                float oy = other.drawy();
                float alpha = Math.abs((float) (linked ? 100 : 0) - Time.time * 2.0F % 100.0F) / 100.0F;
                float x = Mathf.lerp(ox, tx, alpha);
                float y = Mathf.lerp(oy, ty, alpha);
                Tile otherLink = linked ? other : tile;
                float rel = (linked ? tile : other).angleTo(otherLink);
                Draw.color(Pal.gray);
                Lines.stroke(2.5F);
                Lines.square(ox, oy, 2.0F, 45.0F);
                Lines.stroke(2.5F);
                Lines.line(tx + v2.x, ty + v2.y, ox - v2.x, oy - v2.y);
                Draw.color(linked ? Pal.place : Pal.accent);
                Lines.stroke(1.0F);
                Lines.line(tx + v2.x, ty + v2.y, ox - v2.x, oy - v2.y);
                Lines.square(ox, oy, 2.0F, 45.0F);
                Draw.mixcol(Draw.getColor(), 1.0F);
                Draw.color();
                Draw.rect(arrowRegion, x, y, rel);
                Draw.mixcol();
            }
        }

        public void drawConfigure() {
            Drawf.select(this.x, this.y, (float) (this.tile.block().size * 8) / 2.0F + 2.0F, Pal.accent);
            Draw.color(Pal.accent);
            Lines.dashCircle(x, y, (range) * 8f);
            Draw.color();
            if (!canReLink() && !canLinked()) return;
            OrderedMap<Building, Boolean> orderedMap = new OrderedMap<>();
            for (int x = -range; x <= range; ++x) {
                for (int y = -range; y <= range; ++y) {
                    Tile other = this.tile.nearby(x, y);
                    if (linkValid(this.tile, other) && !(tile == other)) {
                        if (!orderedMap.containsKey(other.build)) orderedMap.put(other.build, false);
                    }
                }
            }
            Building linkBuilding = Vars.world.build(link);
            if (linkBuilding != null) {
                configure(linkBuilding.pos());
                orderedMap.remove(linkBuilding);
                orderedMap.put(linkBuilding, true);
            } else {
                configure(-1);
            }
            if (orderedMap.containsKey(this)) orderedMap.remove(this);
            orderedMap.each((other, linked) -> {
//                if (!linkValid(tile, other.tile)) return;
                Drawf.select(other.x, other.y, (float) (other.block().size * 8) / 2.0F + 2.0F + (linked ? 0.0F : Mathf.absin(Time.time, 4.0F, 1.0F)), linked ? Pal.place : Pal.breakInvalid);
            });

        }


    }
}
