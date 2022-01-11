package mma.entities;

import arc.math.geom.*;
import mindustry.annotations.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mma.annotations.ModAnnotations.*;
import mma.gen.*;

@Annotations.EntityDef({TestAnnotationsc.class})
@Annotations.Component
abstract class TestAnnotationsComp implements Entityc{
    public Vec2 vec2Test;
    Unit testUnit;

    @Override
    @Annotations.MethodPriority(1000)
//    @UseOnlyImplementation({Velc.class})
//    @IgnoreImplementation({Minerc.class,Unitc.class,Commanderc.class})
    public void update(){
        throw new RuntimeException();
    }

    @Override
    @Annotations.MethodPriority(1000)
    public void add(){
        throw new RuntimeException();
    }

    @Override
    @Annotations.MethodPriority(1000)
    public void remove(){
        throw new RuntimeException();
    }

    public void test(){

    }
}

@Struct
class TestStructStruct{
    short block, floor, overlay;
}