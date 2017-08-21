package lang.celadon;

/**
 * Indirection cell; has a name that may be used to look it up, and any Object for a value.
 * Created by Tommy Ettinger on 8/20/2017.
 */
public class Name {
    public String title;
    public Object ref;

    public Name() {
    }

    public Name(String title, Object ref) {
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
                Name item = (Name) o;
                return ref.equals(item.ref);
            } else
                return ref.getClass() == o.getClass() && ref.equals(o);
        }
        else return o == null || (o.getClass() == getClass() && ((Name)o).ref == null);
    }

    @Override
    public int hashCode() {
        return ref != null ? ref.hashCode() : 0;
    }
}
