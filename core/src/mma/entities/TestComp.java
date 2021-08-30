package mma.entities;

import mindustry.gen.Entityc;
import mindustry.gen.Unit;
import mma.annotations.ModAnnotations;

@ModAnnotations.Component
abstract class TestComp implements Entityc {
    Unit testUnit;

    public void test() {

    }
}
