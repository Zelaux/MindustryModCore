package mma.entities.compByAnuke;

import arc.graphics.g2d.*;
import mindustry.annotations.Annotations.*;
import mindustry.game.*;
import mindustry.gen.*;
import static mindustry.Vars.*;
import static mindustry.logic.LAccess.*;

@mma.annotations.ModAnnotations.Component
abstract class BlockUnitComp implements Unitc {

    @mma.annotations.ModAnnotations.Import
    Team team;

    @mma.annotations.ModAnnotations.ReadOnly
    transient Building tile;

    public void tile(Building tile) {
        this.tile = tile;
        // sets up block stats
        maxHealth(tile.block.health);
        health(tile.health());
        hitSize(tile.block.size * tilesize * 0.7f);
        set(tile);
    }

    @Override
    public void update() {
        if (tile != null) {
            team = tile.team;
        }
    }

    @mma.annotations.ModAnnotations.Replace
    @Override
    public TextureRegion icon() {
        return tile.block.fullIcon;
    }

    @Override
    public void killed() {
        tile.kill();
    }

    @mma.annotations.ModAnnotations.Replace
    public void damage(float v, boolean b) {
        tile.damage(v, b);
    }

    @mma.annotations.ModAnnotations.Replace
    public boolean dead() {
        return tile == null || tile.dead();
    }

    @mma.annotations.ModAnnotations.Replace
    public boolean isValid() {
        return tile != null && tile.isValid();
    }

    @mma.annotations.ModAnnotations.Replace
    public void team(Team team) {
        if (tile != null && this.team != team) {
            this.team = team;
            if (tile.team != team) {
                tile.team(team);
            }
        }
    }
}
