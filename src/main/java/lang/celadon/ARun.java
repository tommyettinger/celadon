package lang.celadon;

import java.util.Collections;
import java.util.List;

/**
 * Abstract class to be extended by classes that can be run as Celadon code and return one Token as a result. This makes
 * it different from IMorph because an IMorph can return zero or more tokens, but also because this stores parameter
 * names and a body as Token lists, as well as storing a copy of the Context at the time this ARun was declared.
 * Created by Tommy Ettinger on 1/9/2017.
 */
public abstract class ARun {
    public Context context;
    public List<Token> names, body;
    public ARun(final Context context, final List<Token> tokens,
                final int nameStart, final int nameEnd, final int bodyStart, final int bodyEnd)
    {
        this.context = new Context(context);
        names = tokens.subList(nameStart, nameEnd);
        body = tokens.subList(bodyStart, bodyEnd);
    }
    public ARun(final Context context, final List<Token> nameTokens)
    {
        this.context = new Context(context);
        names = nameTokens;
        body = Collections.emptyList();
    }

    /**
     * Abstract method that gets a value from a defined ARun by assigning values to {@link #names} from the items in
     * {@code parameters} and running through {@link #body}, possibly starting other ARun instances.
     * @param parameters values to assign to names; must have size no less that the size of {@link #names}
     * @return a single returned Token; this can potentially be a Token that resolves to an IMorph and so yields more
     */
    public abstract Token run(List<Token> parameters);
}
