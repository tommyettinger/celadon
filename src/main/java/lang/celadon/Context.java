package lang.celadon;

import squidpony.squidmath.IntVLA;

import java.util.*;

/**
 * Created by Tommy Ettinger on 1/6/2017.
 */
public class Context extends HashMap<String, LinkedList<Token>>{

    public Context()
    {
        super(256, 0.625f);

    }
    public Context(HashMap<String, LinkedList<Token>> existing)
    {
        super(existing);
    }
    public static LinkedList<Token> makeLL(Object... items)
    {
        LinkedList<Token> ll = new LinkedList<>();
        for (int i = items.length - 1; i >= 0; i--) {
            ll.addFirst(Token.stable(items[i]));
        }
        return ll;
    }
    public static LinkedList<Token> stableLL(Object item)
    {
        LinkedList<Token> ll = new LinkedList<>();
        ll.add(Token.stable(item));
        return ll;
    }
    static LinkedList<Token> reserve(Object item)
    {
        LinkedList<Token> ll = new LinkedList<>();
        ll.addLast(Token.stable(item));
        ll.addLast(Token.RESERVED);
        return ll;
    }
    protected void core()
    {
        put("null", reserve(null));
        put("true", reserve(true));
        put("false", reserve(false));
    }

    public Token peek(String key)
    {
        if(containsKey(key))
        {
            LinkedList<Token> items = get(key);
            if(!items.isEmpty())
                return items.getFirst();
        }
        return null;
    }
    public Context push(String key, Token value)
    {
        LinkedList<Token> items = get(key);
        if(items != null)
        {
            if(items.getLast() != Token.RESERVED)
                items.addFirst(value);
        }
        else
        {
            items = new LinkedList<>();
            items.addFirst(value);
            put(key, items);
        }
        return this;
    }
    public Context pop(String key)
    {
        LinkedList<Token> items = get(key);
        if(items != null)
        {
            if(items.getLast() == Token.RESERVED)
                return this;
            items.removeFirst();
            if(items.isEmpty())
                remove(key);
        }
        return this;
    }

    public List<Object> parse(List<Token> tokens)
    {
        if(tokens == null)
            return Collections.emptyList();
        List<Object> values = new ArrayList<>(16);
        Token t, t2;
        int i0;
        IntVLA bracketPositions = new IntVLA(16);
        for (int i = 0; i < tokens.size(); i++) {
            t = tokens.get(i);
            if (t.special > 0) {
                values.add(t.solid);
            } else if (t.bracket != null && t.contents != null && t.mode != null) {
                t2 = peek(t.mode);
                if (t2.special < 0) {
                    i -= 1 + ((IMorph) t2.solid).morph(tokens, i, i + 1);
                } else
                    values.add(t.contents);
            } else if (t.bracket != null) {
                if (!t.closing) {
                    bracketPositions.add(i);
                } else if (t.bracketsMatch(t2 = tokens.get(i0 = bracketPositions.pop()))) {
                    if (t2.special < 0) {
                        i -= 1 + ((IMorph) t2.solid).morph(tokens, i0, i + 1);
                    }
                } else throw new UnsupportedOperationException("Brackets do not match: opening bracket is " + t2.bracket
                        + ", closing bracket is " + t2.bracket);
            } else if ((i0 = t.special) < 0)
            {
                i -= 1 + ((IMorph) t.solid).morph(tokens, i, i -i0);
            }
        }
        return values;
    }

}
