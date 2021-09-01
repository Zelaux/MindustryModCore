package mma.entities;

import arc.math.geom.Vec2;
import mindustry.gen.Entityc;
import mindustry.gen.Unit;
import mma.annotations.ModAnnotations;
import mma.gen.TestAnnotationsc;

@ModAnnotations.EntityDef({TestAnnotationsc.class})
@ModAnnotations.Component
abstract class TestAnnotationsComp implements Entityc {
    public Vec2 vec2Test;
    Unit testUnit;

    @Override
    @ModAnnotations.MethodPriority(1000)
    public void update() {
        throw new RuntimeException();
    }

    @Override
    @ModAnnotations.MethodPriority(1000)
    public void add() {
        throw new RuntimeException();
    }

    @Override
    @ModAnnotations.MethodPriority(1000)
    public void remove() {
        throw new RuntimeException();
    }

    public void test() {

    }
}
