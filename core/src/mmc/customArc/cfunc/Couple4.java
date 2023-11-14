package mmc.customArc.cfunc;

import java.util.Objects;

public class Couple4<A, B, C, D> {
    public final A o1;
    public final B o2;
    public final C o3;
    public final D o4;

    public Couple4(A o1, B o2, C o3, D o4) {
        this.o1 = o1;
        this.o2 = o2;
        this.o3 = o3;
        this.o4 = o4;
    }

    @Override
    public String toString() {
        return "Couple4{" +
                "o1=" + o1 +
                ", o2=" + o2 +
                ", o3=" + o3 +
                ", o4=" + o4 +
                '}';
    }

    public static <A, B, C, D> Couple4<A, B, C, D> of(A a, B b, C c, D d) {
        return new Couple4<>(a, b, c, d);
    }

    public boolean equals(Object other) {
        return
                other instanceof Couple4<?, ?, ?, ?> &&
                        Objects.equals(o1, ((Couple4<?, ?, ?, ?>) other).o1) &&
                        Objects.equals(o2, ((Couple4<?, ?, ?, ?>) other).o2) &&
                        Objects.equals(o3, ((Couple4<?, ?, ?, ?>) other).o3) &&
                        Objects.equals(o3, ((Couple4<?, ?, ?, ?>) other).o4);
    }

    public int hashCode() {
        boolean an = o1 == null, bn = o2 == null, cn = o3 == null, dn = o4 == null;
        if (an && bn && cn && dn) {
            return 0;
        } else if (bn && cn && dn) {
            return o1.hashCode();
        } else if (an && cn && dn) {
            return o2.hashCode() + 1;
        } else if (an && bn && dn) {
            return o3.hashCode() + 2;
        } else if (an && bn && cn) {
            return o4.hashCode() + 3;
        }
        return 0;
    }
}
