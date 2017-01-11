package lang.celadon;

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
    public ARun(Context context, final List<Token> tokens, int nameStart, int nameEnd, int bodyStart, int bodyEnd)
    {
        this.context = new Context(context);
        names = tokens.subList(nameStart, nameEnd);
        body = tokens.subList(bodyStart, bodyEnd);
    }

    public abstract Token run(List<Token> parameters);
}
