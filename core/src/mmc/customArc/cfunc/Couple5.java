package mmc.customArc.cfunc;

import java.util.Objects;

public class Couple5<A,B,C,D,E> {
    public final A o1;
    public final B o2;
    public final C o3;
    public final D o4;
    public final E o5;

    public Couple5(A o1, B o2, C o3, D o4, E o5) {
        this.o1 = o1;
        this.o2 = o2;
        this.o3 = o3;
        this.o4 = o4;
        this.o5 = o5;
    }

    @Override
    public String toString() {
        return "Couple5{" +
                "o1=" + o1 +
                ", o2=" + o2 +
                ", o3=" + o3 +
                ", o4=" + o4 +
                ", o5=" + o5 +
                '}';
    }

    public boolean equals(Object other) {
        return
                other instanceof Couple5<?,?,?,?,?> &&
                        Objects.equals(o1, ((Couple5<?,?,?,?,?>)other).o1) &&
                        Objects.equals(o2, ((Couple5<?,?,?,?,?>)other).o2) &&
                        Objects.equals(o3, ((Couple5<?,?,?,?,?>)other).o3) &&
                        Objects.equals(o3, ((Couple5<?,?,?,?,?>)other).o4) &&
                        Objects.equals(o3, ((Couple5<?,?,?,?,?>)other).o5);
    }

    public int hashCode() {
        boolean an = o1 == null,bn = o2 == null, cn = o3 == null,dn= o4 ==null,en= o5 ==null;
        if (an && bn && cn && dn && en){
            return 0;
        } else if(bn && cn && dn && en) {
            return o1.hashCode();
        } else if(an && cn && dn && en) {
            return o2.hashCode()+1;
        } else if(an && bn  && dn && en) {
            return o3.hashCode()+2;
        } else if(an && bn && cn  && en) {
            return o4.hashCode()+3;
        } else if(an && bn && cn && dn) {
            return o5.hashCode()+4;
        }
        return 0;
    }

    public static <A,B,C,D,E> Couple5<A,B,C,D,E> of(A a, B b,C c,D d,E e) {
        return new Couple5<>(a,b,c,d,e);
    }
}
