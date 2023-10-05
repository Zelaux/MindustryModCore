package coretest.tests.world;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arclibrary.graphics.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import mmc.annotations.ModAnnotations.*;
import mmc.graphics.*;
import mmc.world.*;

import static mindustry.Vars.*;

public class TestBlock extends ModBlock{
    @Load("@basePrefix-base")
    public TextureRegion testRegion;
    @Load("@basePrefix-@-@size-@group.ordinal()-base")
    public TextureRegion testRegion2;
    @Load(value = "@basePrefix-#-#0",lengths = {10})
    public TextureRegion[] testRegion3;
    @Load(value = "@basePrefix-#-#0-#1",lengths = {10,10})
    public TextureRegion[][] testRegion4;
    @Load(value = "1",fallback = "2")
    public TextureRegion testRegion5;
    @ALoad(value = "1",fallback = {"2","3","4","5","6"})
    public TextureRegion testRegion6;
    public String basePrefix;

    public TestBlock(String name){
        super(name);

        solid = false;
        underBullets = true;
        update = true;
//        hasItems = true;
        itemCapacity = 20;
        group = BlockGroup.drills;
//        unloadable = false;
//        noUpdateDisabled = true;
    }

    public class TestBuild extends Building{
        float drawValue = 0f;

        @Override
        public void draw(){
            super.draw();
            setupColor();
            Vec2 vec = Core.input.mouseWorld();
            Building selected = world.buildWorld(vec.x, vec.y);
//            Building selected = Vars.control.input.config.getSelected();
            if(selected == this){
                drawValue = Mathf.lerpDelta(drawValue, 1f, 0.1f);
//                drawValue = Math.min(1f, drawValue + Time.time / 10f);
            }else{
                drawValue = Mathf.lerpDelta(drawValue, 0f, 0.1f);
//                drawValue = Math.max(0f, drawValue - Time.time / 10f);
                if(drawValue < 0.001f){
                    Fill.circle(x, y, size * tilesize);
                }else{
                    drawSelect();
                }
            }
            Draw.color();
        }

        private void setupColor(){
            Draw.color(Pal.accent, 0.25f);
        }

        @Override
        public void drawSelect(){
            super.drawSelect();

            setupColor();
            EFill.donut(x, y, (size - 0.5f) * tilesize * drawValue, size * tilesize);
            Draw.color();
        }

        @Override
        public void drawConfigure(){
            super.drawConfigure();
        }

        @Override
        public void drawCracks(){
            super.drawCracks();
        }

        @Override
        public void drawDisabled(){
            super.drawDisabled();
        }

        @Override
        public void drawLight(){
            super.drawLight();
        }

        @Override
        public void drawLiquidLight(Liquid liquid, float amount){
            super.drawLiquidLight(liquid, amount);
        }


        @Override
        public void drawStatus(){
            super.drawStatus();
        }

        @Override
        public void drawTeam(){
            super.drawTeam();
        }

        @Override
        public void drawTeamTop(){
            super.drawTeamTop();
        }
    }

}
