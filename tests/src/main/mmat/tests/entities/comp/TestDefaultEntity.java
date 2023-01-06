package mmat.tests.entities.comp;

import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mma.annotations.ModAnnotations.*;

@Component
@GenerateDefaultImplementation
abstract class TestDefaultEntityComp implements Buildingc{
    public void hahaMethod(){
        System.out.println("haha");
    }
    public abstract void ohnoMethod();
}
