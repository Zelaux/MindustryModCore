package mmc.io;

import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.units.UnitController;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.io.TypeIO;
import mindustry.logic.LAccess;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.annotations.Annotations;
import mmc.annotations.ModAnnotations.*;

import static mindustry.Vars.*;

@Annotations.TypeIOHandler
@DefaultTypeIOHandler
public class ModTypeIO extends TypeIO {
    public static Entityc readEntity(Reads read){
        return TypeIO.readEntity(read);
    }
    public static void writeInteger(Writes write, Integer integer) {
        write.i(integer);
    }

    public static Integer readInteger(Reads read) {
        return read.i();
    }

    public static void writeSeqBuilding(Writes write, Seq<Building> buildings) {
        write.i(buildings.size);
        for (Building building : buildings) {
            writeBuilding(write, building);
        }
    }

    public static Seq<Building> readSeqBuilding(Reads read) {
        Seq<Building> buildings = new Seq<>();
        int size = read.i();
        for (int i = 0; i < size; i++) {
            buildings.add(readBuilding(read));
        }
        return buildings;
    }

    public static void writeEnum(Writes writes, Enum<?> enumValue) {
        writes.s(enumValue.ordinal());
    }

    public static <T> T readEnum(Reads read, T[] values) {
        return values[Mathf.mod(read.s(), values.length)];
    }

    public static void writeStaticWall(Writes writes, StaticWall staticWall) {
        writes.s(staticWall == null ? -1 : staticWall.id);
    }

    public static StaticWall readStaticWall(Reads read) {
        short s = read.s();
        Block staticWall = s == -1 ? null : Vars.content.block(s);
        return staticWall instanceof StaticWall ? (StaticWall) staticWall : null;
    }

    public static void writeOreBlock(Writes writes, OreBlock block) {
        writes.s(block == null ? -1 : block.id);
    }

    public static OreBlock readOreBlock(Reads read) {
        short s = read.s();
        Block block = s == -1 ? null : Vars.content.block(s);
        return block instanceof OreBlock ? (OreBlock) block : null;
    }

    public static void writeFloor(Writes writes, Floor floor) {
        writes.s(floor == null ? -1 : floor.id);
    }

    public static Floor readFloor(Reads read) {
        short s = read.s();
        Block floor = s == -1 ? null : Vars.content.block(s);
        return floor instanceof Floor ? (Floor) floor : null;
    }


    public static void writeTeam(Writes write, Team team) {
        if (team == null) {
            write.b(-1);
        } else {
            TypeIO.writeTeam(write, team);
        }
    }

    public static Team readTeam(Reads reads) {
        int id = reads.b();
        if (id == -1) return null;
        return Team.get(id);
    }

    public static void writeUnitType(Writes write, UnitType unitType) {
        if (unitType == null) {
            write.s(-1);
        } else {
            TypeIO.writeUnitType(write, unitType);
        }
    }

    public static UnitController readController(Reads read) {
        return readController(read, null);
    }


}

