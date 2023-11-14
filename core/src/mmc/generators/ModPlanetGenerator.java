package mmc.generators;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.ObjectMap;
import arc.util.Tmp;
import arc.util.noise.Ridged;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.maps.generators.BaseGenerator;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.world.Block;
import mindustry.world.TileGen;

public class ModPlanetGenerator extends PlanetGenerator {

    private static int lastSeed = 0;
    protected final int seed=lastSeed++;
    public  Block[][] arr;
    public  float scl = 0.f;
    public  float waterOffset = 0.f;
    public   BaseGenerator basegen = new BaseGenerator();
    public float water = 0.f;
   protected ObjectMap<Block,Block> dec;

protected     ObjectMap<Block,Block> tars;

    public ModPlanetGenerator() {
        dec=new ObjectMap<>();
        tars=new ObjectMap<>();
        arr=new Block[0][0];
    }

    public Color getColor(Vec3 position) {
        Block block = this.getBlock(position);

        Tmp.c1.set(block.mapColor).a = 1 - block.albedo;
        return Tmp.c1;
    }

    public Block getBlock(Vec3 pos) {
        float height = this.rawHeight(pos);

        Tmp.v31.set(pos);
        pos = Tmp.v33.set(pos).scl(scl);
        float rad = scl;
        float temp = Mathf.clamp(Math.abs(pos.y * 2) / rad);
        float tnoise =Simplex.noise3d(seed, 7d, 0.56d, 1d / 3d, pos.x, pos.y + 999d, pos.z);
        temp = Mathf.lerp(temp, tnoise, 0.5f);
        height *= 0.9f;
        height = Mathf.clamp(height);

        float tar =Simplex.noise3d(seed, 4, 0.55, 0.5, pos.x, pos.y + 999, pos.z) * 0.3f + Tmp.v31.dst(0, 0, 1) * 0.2f;
        Block res = arr[
                Mathf.clamp(Mathf.floor(temp * arr.length), 0, arr[0].length - 1)][Mathf.clamp(Mathf.floor(height * arr[0].length), 0, arr[0].length - 1)
                ];

        if (tar > 0.5) {
            return tars.get(res, res);
        } else {
            return res;
        }

    }

    public float rawHeight(Vec3 pos) {
        pos = Tmp.v33.set(pos);
        pos.scl(scl);

        return (Mathf.pow(Simplex.noise3d(seed, 7, 0.5, 1d / 3d, pos.x, pos.y, pos.z), 2.3f) + waterOffset) / (1 + waterOffset);
    }

    public float getHeight(Vec3 position) {
        float height = this.rawHeight(position);
        return Math.max(height, water);
    }

    public void genTile(Vec3 position, TileGen tile) {
        tile.floor = this.getBlock(position);
        tile.block = tile.floor.asFloor().wall;

        if (Ridged.noise3d(seed,position.x, position.y, position.z, 22) > 0.32) {
            tile.block = Blocks.air;
        }
    }
}
