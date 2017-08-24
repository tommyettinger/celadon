package lang.celadon;

import squidpony.squidmath.CrossHash;

import java.util.Objects;

/**
 * Indirection cell; has a name that may be used to look it up, and any Object for a value.
 * Created by Tommy Ettinger on 8/20/2017.
 */
public class Cel {
    public String title;
    public Object ref;

    public Cel() {
    }

    public Cel(String title, Object ref) {
        this.title = title;
        this.ref = ref;
    }

    @Override
    public String toString() {
        if(ref == null) return title == null ? "null" : title + " = UNBOUND";
        return ref.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(ref != null)
        {
            if (o == null) return false;
            if (getClass() == o.getClass()) {
                Cel item = (Cel) o;
                return ref.equals(item.ref);
            } else
                return ref.getClass() == o.getClass() && ref.equals(o);
        }
        else return o == null || (o.getClass() == getClass() && ((Cel)o).ref == null);
    }

    @Override
    public int hashCode() {
        return ref != null ? ref.hashCode() : 0;
    }

    public static class HasherByName implements CrossHash.IHasher
    {
        @Override
        public int hash(Object data)
        {
            return data == null || !(data instanceof Cel) ? 0 : CrossHash.hash(((Cel)data).title);
        }

        @Override
        public boolean areEqual(Object left, Object right)
        {
            return Objects.equals(left, right);
        }
    }
}
