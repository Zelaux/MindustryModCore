Annotations

+ [Entities annotations](#Entities-an)
    - Mindustry
        - [@Component](#En-Component)
        - [@EntityDef](#En-EntityDef)
        - [@Import](#En-Import)
        - [@MethodPriority](#En-MethodPriority)
        - [@Replace](#En-Replace)
        - [@SyncField](#En-SyncField)
        - [@SyncLocal](#En-SyncLocal)
        - [@ReadOnly](#En-ReadOnly)
        - `@InternalImpl` internal annotation
        - `@BaseComponent` internal annotation
        - `@EntityInterface` internal annotation
        - `@Final` _unused_
        - [@GroupDef](#En-GroupDef)
    - ZelauxModCore
        - [@GlobalReturn](#En-GlobalReturn)
        - [@IgnoreImplementation](#En-IgnoreImplementation)
        - [@UseOnlyImplementation](#En-UseOnlyImplementation)
        - [@SuperMethod](#En-SuperMethod)
        - [@ReplaceInternalImpl](#En-ReplaceInternalImpl)


+ [@Load](#Load);
+ [@RegisterStatement](#RegisterStatement);
+ [@Struct & @StructField](#Struct);
+ [@Remote](#Remote);
+ [@ModAssetsAnnotation](#ModAssetsAnnotation)
+ [@DependenciesAnnotation](#DependenciesAnnotation)
+ [@MainClass](#MainClass)
+ [@TypeIOHandler](#MainClass)

----

# <a name="Entities-an"></a>    Entities annotations

## <a name="Entities-Mindustry"></a> Mindustry

### <a name="En-Component"></a>    `@Component`

Indicates multiple inheritance on a component type.

Example with generating base class for this component:

```java
/*<...>*/
@Component(base = true)
abstract class BuildingComp /*implements <...>*/{
    /*<...>*/
}
```

Example without generating base class for this compoent:

```java
/*<...>*/
@Component()
abstract class PosComp implements Position{
    /*<...>*/
}
```

### <a name="En-EntityDef"></a>    `@EntityDef`

Indicates an entity definition.

Available params:

- `Class[] value` - list of component interfaces
- `boolean isFinal` - Whether the class is final
- `boolean pooled` - If true, entities are recycled.
- `boolean serialize` - Whether to serialize (makes the serialize method return this value).

  If `true`, this entity is automatically put into save files.
  If `false`, no serialization code is generated at all.
- `boolean genio` - Whether to generate IO code. This is for advanced usage only.
- `boolean legacy` - Whether I made a massive mistake by merging two different class branches

Example:

```java
/*<...>*/
@EntityDef(value = {Buildingc.class}, isFinal = false, genio = false, serialize = false)
@Component(base = true)
abstract class BuildingComp /*implements <...>*/{
    /*<...>*/
}
```

### <a name="En-Import"></a>    `@Import`

Indicates that a component field is imported from other components. This means it doesn't actually exist.
Example(importing `x` and `y` from PosComp):

```java
/*<...>*/
abstract class VelComp implements Posc{
    @Import
    float x, y;
    /*<...>*/
}
```

### <a name="En-MethodPriority"></a>    `@MethodPriority`

Indicates priority of a method in an entity. Methods with **higher** priority are done **last**.

Example:

```java
/*<...>*/
abstract class BuildingComp /*implements <...>*/{
    /*<...>*/
    @MethodPriority(100)
    @Override
    public void heal(){
        healthChanged();
    }
    /*<...>*/
}
```

### <a name="En-Replace"></a>    `@Replace`

Indicates that a method overrides other methods.
Example:

```java
/*<...>*/
public abstract class WorldLabelComp implements Posc, Drawc, Syncc{
    /*<...>*/
    @Replace
    public float clipSize(){
        return text.length() * 10f * fontSize;
    }
    /*<...>*/
}
```

### <a name="En-SyncField"></a>    `@SyncField`

Indicates that a field will be interpolated when synced.

Example where `x` will be linearly interpolated:

```java 
@SyncField(true) float x
```

Example where `angle` will be interpolated as angle:

```java 
@SyncField(false) float angle
```

Example where `x` will be clammed after interpolation:

```java 
@SyncField(value=___,clamped=true) float x
```

### <a name="En-SyncLocal"></a>    `@SyncLocal`

Indicates that a field will not be read from the server when syncing the local player state.
Example:

```java
/*<...>*/
abstract class PosComp implements Position{
    @SyncField(true)
    @SyncLocal
    float x, y;
    /*<...>*/
}
```

### <a name="En-ReadOnly"></a>    `@ReadOnly`

Indicates that a component field is read-only.
Example:

```java
/*<...>*/
abstract class PlayerComp implements UnitController, Entityc, Syncc, Timerc, Drawc{
    /*<...>*/
    @ReadOnly
    Unit unit = Nulls.unit;
    /*<...>*/
}
```

### <a name="En-GroupDef"></a>    `@GroupDef`

Creates a group that only examines entities that have all the components listed.

Example:

```java
package mindustry.entities;

import mindustry.annotations.Annotations.*;
import mindustry.gen.*;

class GroupDefs<G>{
    @GroupDef(value = Entityc.class)
    G all;
    @GroupDef(value = Playerc.class, mapping = true)
    G player;
    @GroupDef(value = Bulletc.class, spatial = true, collide = true)
    G bullet;
    @GroupDef(value = Unitc.class, spatial = true, mapping = true)
    G unit;
    @GroupDef(value = Buildingc.class)
    G build;
    @GroupDef(value = Syncc.class, mapping = true)
    G sync;
    @GroupDef(value = Drawc.class)
    G draw;
    @GroupDef(value = Firec.class)
    G fire;
    @GroupDef(value = Puddlec.class)
    G puddle;
    @GroupDef(value = WeatherStatec.class)
    G weather;
    @GroupDef(value = WorldLabelc.class, mapping = true)
    G label;
    @GroupDef(value = PowerGraphUpdaterc.class)
    G powerGraph;
}
```

## <a name="Entities-Core"></a> ZelauxModCore

### <a name="En-GlobalReturn"></a>    `@GlobalReturn`

The return statement of a method with this annotation will not be replaced.

Example component:

```java
/*<...>*/
@Component()
abstract class ExampleComp implements Entitic{
    @Import
    int id;

    /*<...>*/
    @Override
    @GlobalReturn
    void update(){
        if(id % 2 == 0){
            return;
        }
    }
    /*<...>*/
}
```

Generated class

```java
public class Example implements Examplec, Entityc{
    /*<...>*/
    @Override
    public void update(){
        /*code before*/
        example:
        {
            if(id % 2 == 0){
                return;
            }
        }
        /*code after*/
    }
    /*<...>*/
}
```

Generated class without `@GlobalReturn`

```java
public class Example implements Examplec, Entityc{
    /*<...>*/
    @Override
    public void update(){
        /*code before*/
        example:
        {
            if(id % 2 == 0){
                break examplec;
            }
        }
        /*code after*/
    }
    /*<...>*/
}
```

### <a name="En-IgnoreImplementation"></a>    `@IgnoreImplementation`

Ignores implementations from selected components for current method

Example component:

```java
/*<...>*/
@Component()
abstract class ExampleComp implements Velc, Entityc{

    /*<...>*/
    @Override
    @IgnoreImplementation({Velc.class})
    void update(){
        /*come code*/
    }
    /*<...>*/
}
```

Generated class

```java
public class Example implements Examplec, Velc, Entityc{/*<...>*/

    @Override
    public void update(){
        entity:
        {

        }
        example:
        {
            /*come code*/
        }
    }/*<...>*/
}
```

Generated class without @IgnoreImplementation

```java
public class Example implements Examplec, Velc, Entityc{
    /*<...>*/
    @Override
    public void update(){
        vel:
        {
            if(!net.client() || isLocal()){
                float px = x;
                float py = y;
                move(vel.x * Time.delta, vel.y * Time.delta);
                if(Mathf.equal(px, x)) vel.x = 0;
                if(Mathf.equal(py, y)) vel.y = 0;
                vel.scl(Math.max(1.0F - drag * Time.delta, 0));
            }
        }
        entity:
        {

        }
    }/*<...>*/
}
```

### <a name="En-UseOnlyImplementation"></a>    `@UseOnlyImplementation`

Selects implementations for current method

Example component:

```java
/*<...>*/
@Component()
abstract class ExampleComp implements Velc, Healthc, Entityc{

    /*<...>*/
    @Override
    @UseOnlyImplementation({Velc.class})
    void update(){
        /*come code*/
    }
    /*<...>*/
}
```

Generated class

```java
public class Example implements Examplec, Velc, Healthc, Entityc{/*<...>*/

    @Override
    public void update(){
        vel:
        {
            if(!net.client() || isLocal()){
                float px = x;
                float py = y;
                move(vel.x * Time.delta, vel.y * Time.delta);
                if(Mathf.equal(px, x)) vel.x = 0;
                if(Mathf.equal(py, y)) vel.y = 0;
                vel.scl(Math.max(1.0F - drag * Time.delta, 0));
            }
        }
        example:
        {
            /*come code*/
        }
    }/*<...>*/
}
```

Generated class without @UseOnlyImplementation

```java
public class Example implements Examplec, Velc, Healthc, Entityc{
    /*<...>*/
    @Override
    public void update(){
        vel:
        {
            if(!net.client() || isLocal()){
                float px = x;
                float py = y;
                move(vel.x * Time.delta, vel.y * Time.delta);
                if(Mathf.equal(px, x)) vel.x = 0;
                if(Mathf.equal(py, y)) vel.y = 0;
                vel.scl(Math.max(1.0F - drag * Time.delta, 0));
            }
        }
        entity:
        {

        }
        health:
        {
            hitTime -= Time.delta / hitDuration;
        }
        example:
        {
            /*come code*/
        }
    }/*<...>*/
}
```

### <a name="En-SuperMethod"></a>    `@SuperMethod`

Marks the component method that should have an implementation of another method

Example component:

```java
/*<...>*/
@Component()
abstract class ExampleComp implements Velc, Entityc{
    @Import
    int id;

    /*<...>*/
    @Override
    @SuperMethod(parentName = "update")
    abstract void globalUpdate();

    @Override
    @Replace
    void update(){
        if(id % 3 == 0){
            globalUpdate();
        }else{
            id++;
            globalUpdate();
            id += 4;
        }
    }
    /*<...>*/
}
```

Generated class

```java
public class Example implements Examplec, Velc, Entityc{
    /*<...>*/
    public void globalUpdate(){
        vel:
        {
            if(!net.client() || isLocal()){
                float px = x;
                float py = y;
                move(vel.x * Time.delta, vel.y * Time.delta);
                if(Mathf.equal(px, x)) vel.x = 0;
                if(Mathf.equal(py, y)) vel.y = 0;
                vel.scl(Math.max(1.0F - drag * Time.delta, 0));
            }
        }
        entity:
        {

        }
    }

    @Override
    public void update(){
        example:
        {
            if(id % 3 == 0){
                globalUpdate();
            }else{
                id++;
                globalUpdate();
                id += 4;
            }
        }
    }/*<...>*/
}
```

### <a name="En-ReplaceInternalImpl"></a>    `@ReplaceInternalImpl`

Replaces internal implementation for InternalImpl methods such as write, read, add, etc.

Example usage:

```java
/*<...>*/
@Component()
abstract class ExampleComp implements Velc, Entityc{
    /*<...>*/
    @Override
    @ReplaceInternalImpl
    void writeSync(Writes write){
        write(write);
    }

    @Override
    @ReplaceInternalImpl
    void readSync(Reads read){
        read(read);
    }
    /*<...>*/
}
```

# Other annotations

## Mindustry

### <a name="Load"></a>    `@Load`

Automatically loads block regions annotated with this.

Available params:

- `String value` - The region name to load. Variables can be used:
    * `"@"` -> block name
    * `"@expression"` -> `blockInstance.expression`
    * `"#"`,`"#1"`,`"#2"`,`"#positive_integer_number"` -> index number, for arrays
- `int length` - 1D Array length, if applicable.
- `int[] lengths` - 2D array lengths.
- `String fallback` -The fallback region name to load. Variables can be used:
    * `"@"` -> block name
    * `"@expression"` -> `blockInstance.expression`
      Example:

```java
/*<...>*/
class Block{
    /*<...>*/
    public @Load("@-team") TextureRegion teamRegion;
    /*<...>*/
}
```

### <a name="RegisterStatement"></a>    `@RegisterStatement`

Registers a logic statement for auto serialization.

Available params:

- `String value` - String name

[Example (mindustry.logic.LStatements)](https://github.com/Anuken/Mindustry/blob/master/core/src/mindustry/logic/LStatements.java)

### <a name="Struct"></a>    `@Struct & @StructField`

`@Struct` - Marks a class as a special value type struct. Class name must end in 'Struct'.
`@StructField` - Marks a field of a struct. _Optional_

Available params for `@StructField`:
- `int value` - Size of a struct field in bits. Not valid on booleans or floating point numbers.
  ExampleStruct
```java
@Struct
static class DisplayCmdStruct{
    @StructField(4)
    public byte type;
    @StructField(10)
    public int x, y, color;
}
```
Generated class
```java
public class DisplayCmdStruct{
    public static final long bitMaskType = (long)(0b0000000000000000000000000000001111L);

    public static final long bitMaskX = (long)(0b0000000000000000000011111111110000L);

    public static final long bitMaskY = (long)(0b0000000000111111111100000000000000L);

    public static final long bitMaskColor = (long)(0b1111111111000000000000000000000000L);
    public static byte type(long displaycmd) {
        return (byte)((displaycmd >>> 0) & (long)0b0000000000000000000000000000001111L);
    }

    public static long type(long displaycmd, byte value) {
        return (long)((displaycmd & (~(long)0b0000000000000000000000000000001111L)) | ((long)value << 0L));
    }

    public static int x(long displaycmd) {
        return (int)((displaycmd >>> 4) & (long)0b0000000000000000000000001111111111L);
    }

    public static long x(long displaycmd, int value) {
        return (long)((displaycmd & (~(long)0b0000000000000000000011111111110000L)) | ((long)value << 4L));
    }

    public static int y(long displaycmd) {
        return (int)((displaycmd >>> 14) & (long)0b0000000000000000000000001111111111L);
    }

    public static long y(long displaycmd, int value) {
        return (long)((displaycmd & (~(long)0b0000000000111111111100000000000000L)) | ((long)value << 14L));
    }

    public static int color(long displaycmd) {
        return (int)((displaycmd >>> 24) & (long)0b0000000000000000000000001111111111L);
    }

    public static long color(long displaycmd, int value) {
        return (long)((displaycmd & (~(long)0b1111111111000000000000000000000000L)) | ((long)value << 24L));
    }

    public static long get(byte type, int x, int y, int color) {
        return (long)((((long)type << 0L) & (long)0b0000000000000000000000000000001111L) | (((long)x << 4L) & (long)0b0000000000000000000011111111110000L) | (((long)y << 14L) & (long)0b0000000000111111111100000000000000L) | (((long)color << 24L) & (long)0b1111111111000000000000000000000000L));
    }
}
```

### <a name="Remote"></a>    `@Remote`
Marks a method as invokable remotely across a server/client connection.

Available params:

- `Loc value`(where) - Specifies the locations from which this method can be invoked.

  **IMPORTANT** if you selected `Loc.client` or `Loc.both` yor first param must be `mindustry.gen.Player player`
- `Variant variants`(target) -Specifies which methods are generated. Only affects server-to-client methods.
- `Loc called`(local) - The local locations where this method is called locally, when invoked.
- `boolean forward`- Whether to forward this packet to all other clients upon receival. Client only.
- `boolean unreliable` - Whether the packet for this method is sent with UDP instead of TCP. UDP is faster, but is prone to packet loss and duplication.
- `PacketPriority priority` - Priority of this event.

Example:
```java
public class ExampleClass{
    @Remote(Loc.server)
    public static void remoteExample(int id,long data){
        //some code
    }
    @Remote(Loc.client)
    public static void remoteExample(Player player,int id,long data){
        //some code
    }
}
```
## ZelauxModCore
### <a name="ModAssetsAnnotation"></a>    `@ModAssetsAnnotation`
Starts the generation for assets' classes such as Tex, Music, Sounds etc.
### <a name="DependenciesAnnotation"></a>    `@DependenciesAnnotation`
Generates YourPrefixDependencies class to verify the validity of dependencies
Use YourPrefixDependencies.valid() for checking
### <a name="MainClass"></a>    `@MainClass`
Indicates the main class and writes it to mod.(h)json file
### <a name="RootDirectoryPath"></a>    `@RootDirectoryPath`
Sets rootDirectory path
### <a name="AnnotationSettings"></a>    `@AnnotationSettings`
Sets AnnotationSettings