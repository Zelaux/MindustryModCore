package mmat.tests.entities;

import arc.struct.*;
import mindustry.gen.*;
import mma.annotations.ModAnnotations.*;

@Serialize
public class SerializationTests{
    public SerializationTests testField;
    public Unit unit;
    public boolean bool;
    public int integer=0;
    public ObjectMap<String,Building> map;
}
