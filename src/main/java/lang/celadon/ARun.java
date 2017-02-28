package lang.celadon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class to be extended by classes that can be run as Celadon code and return one Token as a result. This makes
 * it different from IMorph because an IMorph can return zero or more tokens, but also because this stores parameter
 * names and a body as Token lists, as well as storing a copy of the Context at the time this ARun was declared.
 * Created by Tommy Ettinger on 1/9/2017.
 */
public abstract class ARun implements Serializable {
    private static final long serialVersionUID = 0;
    public Context context;
    public List<Token> names, body;
    public String title;
    protected final List<Token> bodyFixed;
    public ARun()
    {
        this(new Context(true));
    }
    public ARun(final Context context, final List<Token> tokens,
                final int nameStart, final int nameEnd, final int bodyStart, final int bodyEnd)
    {
        this(context, null, tokens, nameStart, nameEnd, bodyStart, bodyEnd);
    }
    public ARun(final Context context, final String title, final List<Token> tokens,
                final int nameStart, final int nameEnd, final int bodyStart, final int bodyEnd)
    {
        this.context = new Context(context);
        this.title = title;
        names = new ArrayList<>(tokens.subList(nameStart, nameEnd));
        body = new ArrayList<>(tokens.subList(bodyStart, bodyEnd));
        bodyFixed = new ArrayList<>(body);
    }
    public ARun(final Context context)
    {
        this(context, null);
    }
    public ARun(final Context context, final String title)
    {
        this.context = new Context(context);
        this.title = title;
        names = null;
        body = null;
        bodyFixed = null;
    }

    /**
     * Abstract method that gets a value from a defined ARun by assigning values to {@link #names} from the items in
     * {@code parameters} and running through {@link #body}, possibly starting other ARun instances.
     * @param parameters values to assign to names; must have size no less that the size of {@link #names}
     * @return a single returned Token; this can potentially be a Token that resolves to an IMorph and so yields more
     */
    public abstract Token run(List<Token> parameters);

    @Override
    public String toString()
    {
        if(title != null)
            return "{fn " + title + "}";
        if(names == null || bodyFixed == null) {
                return "{fn ~|/unknown name, non-native/|~)";
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("{fn [");
        if(!names.isEmpty()) {
            sb.append(names.get(0));
            for (int i = 1; i < names.size(); ++i) {
                sb.append(' ').append(names.get(i));
            }
        }
        sb.append(" ] ");
        if(!bodyFixed.isEmpty()) {
            sb.append(bodyFixed.get(0));
            for (int i = 1; i < bodyFixed.size(); ++i) {
                sb.append(' ').append(bodyFixed.get(i));
            }
        }
        sb.append('}');
        return sb.toString();

    }
}
