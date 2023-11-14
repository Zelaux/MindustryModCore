package mmc.customArc.cfunc;

import java.util.Objects;

public class Couple3<A,B,C> {
    public final A o1;
    public final B o2;
    public final C o3;

    public Couple3(A o1, B o2, C o3) {
        this.o1 = o1;
        this.o2 = o2;
        this.o3 = o3;
    }

    @Override
    public String toString() {
        return "Couple3{" +
                "o1=" + o1 +
                ", o2=" + o2 +
                ", o3=" + o3 +
                '}';
    }

    public boolean equals(Object other) {
        return
                other instanceof Couple3<?,?,?> &&
                        Objects.equals(o1, ((Couple3<?,?,?>)other).o1) &&
                        Objects.equals(o2, ((Couple3<?,?,?>)other).o2) &&
                        Objects.equals(o3, ((Couple3<?,?,?>)other).o3);
    }

    public int hashCode() {
        boolean an = o1 == null,bn = o2 == null, cn = o3 == null;
        if (an && bn && cn){
            return 0;
        } else if(an && bn) {
            return o3.hashCode()+2;
        } else if (an && cn){
            return o2.hashCode()+1;
        } else if (cn && bn){
            return o1.hashCode();
        }
        return 0;
    }

    public static <A,B,C> Couple3<A,B,C> of(A a, B b,C c) {
        return new Couple3<>(a,b,c);
    }
}
