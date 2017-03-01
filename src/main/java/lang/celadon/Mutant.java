package lang.celadon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract class to be extended by classes that can be run as Celadon code and return one Token as a result. This makes
 * it different from IMorph because an IMorph can return zero or more tokens, but also because this stores parameter
 * names and a body as Token lists, as well as storing a copy of the Context at the time this ARun was declared.
 * Created by Tommy Ettinger on 1/9/2017.
 */
public class Mutant implements IMorph, Serializable {
    private static final long serialVersionUID = 0;
    public Context context;
    public List<Token> body;
    public String title = null;
    protected final List<Token> bodyFixed;
    public Mutant(final Context context, final List<Token> tokens,
                  final int bodyStart, final int bodyEnd)
    {
        this(context, "", tokens, bodyStart, bodyEnd);
    }
    public Mutant(final Context context, final String title, final List<Token> tokens,
                  final int bodyStart, final int bodyEnd)
    {
        this.context = new Context(context);
        this.title = title;
        body = new ArrayList<>(tokens.subList(bodyStart, bodyEnd));
        bodyFixed = new ArrayList<>(body);
    }

    /**
     * Changes the parameter tokens, within the range of start (inclusive) to end (exclusive). This must modify tokens
     * to avoid an "infinite loop" (which will be caught by the sandbox), and this normally means removing all tokens in
     * the start-to-end range and filling 0 or more tokens back in their place. The return value should be the number of
     * tokens "refilled" into tokens after removal of the start-to-end range.
     * <br>
     * For example, an IMorph might cause some side effect when evaluated and then remove the Tokens that produced it.
     * This IMorph would remove all Tokens from start until end (exclusive on end), and insert zero tokens after causing
     * its effects, so it returns 0. Another might be an alias that replaces some range of tokens with one or more other
     * tokens, like code that turns infix into prefix notation; this might take code like {@code #infix(3 * 5 + 8)} and
     * produce {@code (+ (* 3 5) 8)}. The input here is 7 tokens (the opening bracket with modifier, three numbers, two
     * functions, and the closing bracket), and the output is 9 tokens (it includes an extra pair of brackets; the
     * missing modifier doesn't affect the count). Because this particular case produces 9 tokens, the morph method
     * should return 9 here, but for cases where it produces more tokens, it should produce larger return values, too.
     *
     * @param context the Context used to look up or assign values for names; may be modified
     * @param tokens  a List of Token that must be modified by this method, almost always removing the tokens that
     *                caused this IMorph to evaluate
     * @param start   the starting index in tokens (inclusive) to read from, typically removing from start until end
     * @param end     the ending index in tokens (exclusive) to read until, typically removing from start until end
     * @return the number of tokens
     */
    @Override
    @SuppressWarnings("unchecked")
    public int morph(Context context, List<Token> tokens, int start, int end) {
        Token r = null;
        while (this.context.step(body, 0) >= 0)
        {
            r = body.remove(0);
        }
        body.addAll(bodyFixed);
        tokens.subList(start, end).clear();
        if(r == null || r.solid == null || (r.solid instanceof Collection && ((Collection) r.solid).isEmpty()))
            return 0;
        if(r.solid instanceof Collection)
        {
            tokens.addAll(start, (Collection<? extends Token>) r.solid);
            return tokens.size();
        }
        if(r.solid instanceof Token)
        {
            tokens.add(start, (Token)r.solid);
            return 1;
        }
        tokens.add(start, r);
        return 1;
    }

    @Override
    public String toString()
    {
        if(title != null)
            return "{mutant " + title + "}";

        if(bodyFixed == null) {
            return "{mutant ~|/unknown name, non-native/|~)";
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("{mutant ");
        if(!bodyFixed.isEmpty()) {
            sb.append(bodyFixed.get(0));
            for (int i = 1; i < bodyFixed.size(); ++i) {
                sb.append(' ').append(bodyFixed.get(i));
            }
        }
        else
            sb.append("~|/unknown body/|~ ");
        return sb.append('}').toString();

    }

}
