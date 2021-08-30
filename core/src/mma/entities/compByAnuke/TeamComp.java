package mma.entities.compByAnuke;

import arc.util.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import mma.annotations.ModAnnotations;

import static mindustry.Vars.*;


@ModAnnotations.Component
abstract class TeamComp implements Posc {

    @ModAnnotations.Import
    float x, y;

    Team team = Team.derelict;

    public boolean cheating() {
        return team.rules().cheat;
    }

    @Nullable
    public CoreBuild core() {
        return team.core();
    }

    @Nullable
    public CoreBuild closestCore() {
        return state.teams.closestCore(x, y, team);
    }

    @Nullable
    public CoreBuild closestEnemyCore() {
        return state.teams.closestEnemyCore(x, y, team);
    }
}