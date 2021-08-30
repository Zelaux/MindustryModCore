package mma.entities.compByAnuke;

import arc.graphics.g2d.*;
import mindustry.game.*;
import mindustry.gen.*;
import mma.annotations.ModAnnotations;

import static mindustry.Vars.*;


@ModAnnotations.Component
abstract class BlockUnitComp implements Unitc {

    @ModAnnotations.Import
    Team team;

    @ModAnnotations.ReadOnly
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

    @ModAnnotations.Replace
    @Override
    public TextureRegion icon() {
        return tile.block.fullIcon;
    }

    @Override
    public void killed() {
        tile.kill();
    }

    @ModAnnotations.Replace
    public void damage(float v, boolean b) {
        tile.damage(v, b);
    }

    @ModAnnotations.Replace
    public boolean dead() {
        return tile == null || tile.dead();
    }

    @ModAnnotations.Replace
    public boolean isValid() {
        return tile != null && tile.isValid();
    }

    @ModAnnotations.Replace
    public void team(Team team) {
        if (tile != null && this.team != team) {
            this.team = team;
            if (tile.team != team) {
                tile.team(team);
            }
        }
    }
}