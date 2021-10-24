package mma.world.blocks.production;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.util.Time;
import mindustry.gen.Sounds;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.DrawSmelter;
import mma.world.draw.*;
import mma.world.draw.MultiDrawSmelter.*;
/**
 * @deprecated use GenericCrafter with {@link MultiDrawSmelter}*/
@Deprecated
public class MultiGenericSmelter extends GenericCrafter {
//    public Vec3[] topPoints= {};

    public MultiGenericSmelter(String name) {
        super(name);
        drawer=new MultiDrawSmelter();
        ambientSound = Sounds.smelter;
        ambientSoundVolume = 0.07f;
    }

    public void topPoints(Vec3... topPoints){
        if (drawer instanceof MultiDrawSmelter drawSmelter){
            FlamePoint[] flamePoints = new FlamePoint[topPoints.length];
            for(int i = 0; i < topPoints.length; i++){
                Vec3 point = topPoints[i];
                flamePoints[i]=new FlamePoint(point.x,point.y,point.z);
            }
            drawSmelter.flamePoints =flamePoints;
        }
    }
    public class MultiGenericSmelterBuild extends GenericCrafterBuild{
    }
}
