package mma.world.blocks.production;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.util.Time;
import mindustry.gen.Sounds;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.DrawSmelter;

public class MultiGenericSmelter extends GenericCrafter {
    public Vec3[] topPoints= {};

    public MultiGenericSmelter(String name) {
        super(name);
        drawer=new DrawSmelter(Color.valueOf("ffc999"));
        ambientSound = Sounds.smelter;
        ambientSoundVolume = 0.07f;
    }
    public void topPoints(Vec3... topPoints){
        this.topPoints=topPoints;
    }
    public class MultiGenericSmelterBuild extends GenericCrafterBuild{
        private void drawTop(float x,float y,float sizeUp){
            DrawSmelter drawer=(DrawSmelter)MultiGenericSmelter.this.drawer;
            if (this.warmup > 0.0F && drawer.flameColor.a > 0.001F) {
                float g = 0.3F;
                float r = 0.06F;
                float cr = Mathf.random(0.1F);
                Draw.alpha((1.0F - g + Mathf.absin(Time.time, 8.0F, g) + Mathf.random(r) - r) * this.warmup);
                Draw.tint(drawer.flameColor);
                Fill.circle(x, y, (3.0F + Mathf.absin(Time.time, 5.0F, 2.0F) + cr)*sizeUp);
                Draw.color(1.0F, 1.0F, 1.0F, this.warmup);
                Draw.rect(drawer.top, x, y);
                Fill.circle(x, y, (1.9F + Mathf.absin(Time.time, 5.0F, 1.0F) + cr)*sizeUp);
                Draw.color();
            }
        }
        @Override
        public void draw() {
            super.draw();
//            this.onProximityUpdate();

            Draw.rect(this.block.region, this.x, this.y, this.block.rotate ? this.rotdeg() : 0.0F);
            if (topPoints.length>0) {
                float x=this.x- Mathf.floor(this.block.size/2f)*8,
                        y=this.y-Mathf.floor(this.block.size/2f)*8;
                for (Vec3 vec : topPoints) {
                    this.drawTop(x+vec.x*8f, y+vec.y*8f,vec.z);
                }
            }
        }
    }
}
