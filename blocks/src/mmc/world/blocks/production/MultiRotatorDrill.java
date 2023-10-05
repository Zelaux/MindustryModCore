package mmc.world.blocks.production;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.graphics.*;
import mindustry.world.blocks.production.*;
import mmc.type.*;

public class MultiRotatorDrill extends Drill implements ImageGenerator{
    public Rotator[] rotators = {};
    public boolean drawRotator = true;

    public MultiRotatorDrill(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();
    }

    public void rotators(Rotator... rotators){
        this.rotators = rotators;
        for(Rotator rotator : rotators){
            if(rotator.size == -1) rotator.size = size;
        }
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);

        if(!synthetic()){
            PixmapRegion image = Core.atlas.getPixmap(fullIcon);
            mapColor.set(image.get(image.width / 2, image.height / 2));
        }

        if(variants > 0){
            for(int i = 0; i < variants; i++){
                String rname = name + (i + 1);
                packer.add(MultiPacker.PageType.editor, "editor-" + rname, Core.atlas.getPixmap(rname));
            }
        }

        Pixmap last = null;

        TextureRegion[] gen = icons();

        if(outlineIcon){
            PixmapRegion region = Core.atlas.getPixmap(gen[outlinedIcon >= 0 ? outlinedIcon : gen.length - 1]);
            Pixmap out = last = Pixmaps.outline(region, outlineColor, outlineRadius);
            if(Core.settings.getBool("linear")){
                Pixmaps.bleed(out);
            }
            packer.add(MultiPacker.PageType.main, name, out);
        }

        PixmapRegion editorBase = Core.atlas.getPixmap(fullIcon);

        if(gen.length > 1){
            Pixmap base = Core.atlas.getPixmap(gen[0]).crop();
            for(int i = 1; i < gen.length; i++){
                if(i == gen.length - 1 && last != null){
                    base.draw(last, 0, 0, true);
                }else{
                    base.draw(Core.atlas.getPixmap(gen[i]), true);
                }
            }

            packer.add(MultiPacker.PageType.main, "block-" + name + "-full", base);

            editorBase = new PixmapRegion(base);
        }

        packer.add(MultiPacker.PageType.editor, name + "-icon-editor", editorBase);
    }

    public Rotator rotator(float x, float y, float size){
        return new Rotator(x, y, size);
    }

    public Rotator rotator(float x, float y){
        return new Rotator(x, y);
    }

    public void rotators(float size, Rotator... rotators){
        this.rotators = rotators;
        for(Rotator rotator : rotators){
            rotator.size = size;
        }
    }

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list){
        Draw.rect(this.region, req.drawx(), req.drawy(), !this.rotate ? 0.0F : (float)(req.rotation * 90));
        if(drawRotator){
            float x = req.drawx() - Mathf.floor(this.size / 2f) * 8,
            y = req.drawy() - Mathf.floor(this.size / 2f) * 8;
            for(Rotator vec : rotators){
                Draw.rect(rotatorRegion, x + vec.x * 8, y + vec.y * 8);
                Draw.rect(topRegion, x + vec.x * 8, y + vec.y * 8);
            }
        }
        if(req.config != null){
            this.drawPlanConfig(req, list);
        }
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{this.region};
    }

    @Override
    public Pixmap generate(Pixmap icon, Func<TextureRegion, Pixmap> pix){
        Pixmap rotator = pix.get(rotatorRegion);
        Pixmap top = pix.get(topRegion);
        Cons3<Pixmap, Integer, Integer> draw = (pixmap, drawx, drawy) -> {
            icon.draw(pixmap.copy(),
            0, 0,
            pixmap.width, pixmap.height,
            drawx - pixmap.width / 2, drawy - pixmap.height / 2,
            pixmap.width, pixmap.height,
            false, true);
        };
        for(Rotator rotor : rotators){
            int x = (int)(rotor.x * 32 + 16);
            int y = (int)((size - rotor.y) * 32 - 16);
            draw.get(rotator, x, y);
            draw.get(top, x, y);
        }
        return icon;
    }

    @Override
    public void load(){
        super.load();
        for(Rotator rotator : rotators){
            if(rotator.textureName == null){
                rotator.region=rotatorRegion;
                rotator.regionTop=topRegion;
            }else{
                rotator.region=Core.atlas.find(rotator.textureName);
                rotator.regionTop=Core.atlas.find(rotator.textureName+"-top",topRegion);
            }
        }
    }

    public class MultiRotorDrillBuild extends DrillBuild{
        @Override
        public void updateTile(){
            if(dominantItem == null){
                return;
            }

            if(timer(timerDump, dumpTime)){
                dump(items.has(dominantItem) ? dominantItem : null);
            }

            timeDrilled += warmup * delta();

            if(items.total() < itemCapacity && dominantItems > 0 && efficiency > 0){
                float speed = Mathf.lerp(1f, liquidBoostIntensity, optionalEfficiency) * efficiency;

                lastDrillSpeed = (speed * dominantItems * warmup) / (drillTime + hardnessDrillMultiplier * dominantItem.hardness);
                warmup = Mathf.approachDelta(warmup, speed, warmupSpeed);
                progress += delta() * dominantItems * speed * warmup;

                if(Mathf.chanceDelta(updateEffectChance * warmup))
                    updateEffect.at(x + Mathf.range(size * 2f), y + Mathf.range(size * 2f));
            }else{
                lastDrillSpeed = 0f;
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                return;
            }

            float delay = drillTime + hardnessDrillMultiplier * dominantItem.hardness;

            if(dominantItems > 0 && progress >= delay && items.total() < itemCapacity){
                offload(dominantItem);

                progress %= delay;

                if(wasVisible) effectAboveRotor((x, y, size) -> drillEffect.at(x + Mathf.range(size), y + Mathf.range(size), dominantItem.color));
            }
        }

        protected void effectAboveRotor(Rotator.DrillRotorCons cons2){
            Rotator rotator = Structs.random(rotators);
            float xo = this.x - Mathf.floor(this.block.size / 2f) * 8;
            float yo = this.y - Mathf.floor(this.block.size / 2f) * 8;
            cons2.get(xo + rotator.x * 8, yo + rotator.y * 8, rotator.size);
        }

        public void draw(){
            float s = 0.3F;
            float ts = 0.6F;
            Draw.rect(region, this.x, this.y);
            super.drawCracks();
            if(drawRim){
                Draw.color(heatColor);
                Draw.alpha(this.warmup * ts * (1.0F - s + Mathf.absin(Time.time, 3.0F, s)));
                Draw.blend(Blending.additive);
                Draw.rect(rimRegion, this.x, this.y);
                Draw.blend();
                Draw.color();
            }
            if(rotators.length > 0){
                float x = this.x - Mathf.floor(this.block.size / 2f) * 8,
                y = this.y - Mathf.floor(this.block.size / 2f) * 8;
                for(Rotator rotator : rotators){
                    rotator.drawAt(x,y,this.timeDrilled * rotateSpeed);
                }
            }
            if(this.dominantItem != null && drawMineItem){
                Draw.color(this.dominantItem.color);
                Draw.rect(itemRegion, this.x, this.y);
                Draw.color();
            }

        }
    }
}
