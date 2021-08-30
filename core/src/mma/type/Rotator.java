package mma.type;

public class Rotator {
    public float x;
    public float y;
    public float size = -1;

    public Rotator(float x, float y, float size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public Rotator(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public interface DrillRotorCons {
        void get(float x, float y, float rotorSize);
    }
}
