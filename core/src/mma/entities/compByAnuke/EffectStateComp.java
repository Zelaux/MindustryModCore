package mma.entities.compByAnuke;

import arc.graphics.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.*;
import mindustry.gen.*;
import static mindustry.logic.LAccess.*;

// @mma.annotations.ModAnnotations.EntityDef(value = { EffectStatec.class, Childc.class }, pooled = true, serialize = false)
@mma.annotations.ModAnnotations.Component(base = true)
abstract class EffectStateComp implements Posc, Drawc, Timedc, Rotc, Childc {

    @mma.annotations.ModAnnotations.Import
    float time, lifetime, rotation, x, y;

    @mma.annotations.ModAnnotations.Import
    int id;

    Color color = new Color(Color.white);

    Effect effect;

    Object data;

    @Override
    public void draw() {
        lifetime = effect.render(id, color, time, lifetime, rotation, x, y, data);
    }

    @mma.annotations.ModAnnotations.Replace
    public float clipSize() {
        return effect.clip;
    }
}
