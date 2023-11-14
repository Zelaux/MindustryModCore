package mmc.world.blocks.distribution;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.ItemBuffer;

public class CrossBufferedItemBridge extends CrossItemBridge {
    public final int timerAccept;
    public float speed;
    public int bufferCapacity;
    public CrossBufferedItemBridge(String name) {
        super(name);
        this.hasItems = true;
        this.timerAccept = this.timers++;
        this.speed = 40.0F;
        this.bufferCapacity = 50;
        this.hasPower = false;
        this.canOverdrive = true;
    }
    public class CrossBufferedItemBridgeBuild extends CrossItemBridgeBuild{
        ItemBuffer buffer;
        public CrossBufferedItemBridgeBuild() {
            super();
            this.buffer = new ItemBuffer(bufferCapacity);
        }
        public void updateTransport(Building other) {
            if(buffer.accepts() && items.total() > 0){
                buffer.accept(items.take());
            }

            Item item = buffer.poll(speed / timeScale);
            if(timer(timerAccept, 4 / timeScale) && item != null && other.acceptItem(this, item)){
                moved = true;
                other.handleItem(this, item);
                buffer.remove();
            }

        }

        public void write(Writes write) {
            super.write(write);
            buffer.write(write);
        }

        public void read(Reads read, byte revision) {
            super.read(read, revision);
            buffer.read(read);
        }
    }
}
