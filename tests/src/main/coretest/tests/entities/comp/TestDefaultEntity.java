package coretest.tests.entities.comp;

import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mmc.annotations.ModAnnotations.*;

@Component
@GenerateDefaultImplementation
abstract class TestDefaultEntityComp implements Buildingc{
    public void hahaMethod(){
        System.out.println("haha");
    }
    public void hahaMethod(String hahaString){
        System.out.println("haha"+hahaString);
    }
    public boolean nonoMethod(String hahaString){
        return hahaString.equals("haha");
    }
    public abstract void ohnoMethod();
}
