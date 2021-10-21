package mma.entities.compByAnuke;

import arc.util.*;
import mindustry.annotations.Annotations.*;
import static mindustry.logic.LAccess.*;

@Component
abstract class TimerComp {

    transient Interval timer = new Interval(6);

    public boolean timer(int index, float time) {
        if (Float.isInfinite(time))
            return false;
        return timer.get(index, time);
    }
}
