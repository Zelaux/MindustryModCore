package mmc.customArc.cfunc;


import java.util.Objects;

public class Couple<A,B> {
    public final A o1;
    public final B o2;

    public Couple(A o1, B o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public String toString() {
        return "Couple[" + o1 + "," + o2 + "]";
    }

    public boolean equals(Object other) {
        return
                other instanceof Couple<?,?> &&
                        Objects.equals(o1, ((Couple<?,?>)other).o1) &&
                        Objects.equals(o2, ((Couple<?,?>)other).o2);
    }

    public int hashCode() {
        if (o1 == null) return (o2 == null) ? 0 : o2.hashCode() + 1;
        else if (o2 == null) return o1.hashCode() + 2;
        else return o1.hashCode() * 17 + o2.hashCode();
    }

    public static <A,B> Couple<A,B> of(A a, B b) {
        return new Couple<>(a,b);
    }
}
