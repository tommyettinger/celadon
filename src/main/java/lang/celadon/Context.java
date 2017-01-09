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
        core();

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
    static LinkedList<Token> reserveSymbol(IMorph item)
    {
        LinkedList<Token> ll = new LinkedList<>();
        ll.addLast(Token.envoy(item));
        ll.addLast(Token.RESERVED);
        return ll;
    }
    static LinkedList<Token> reserveBracket(IMorph item)
    {
        LinkedList<Token> ll = new LinkedList<>();
        ll.addLast(Token.varying(item));
        ll.addLast(Token.RESERVED);
        return ll;
    }
    protected void core()
    {
        put("null", reserve(null));
        put("true", reserve(true));
        put("false", reserve(false));
        put("(", reserveBracket(new IMorph() {
            @Override
            public int morph(final List<Token> tokens, int start, int end) {
                Token result;
                if(start + 2 == end)
                {
                    result = Token.stable(Collections.emptyList());
                }
                else {
                    Token f = tokens.remove(start+1);
                    for (int i = start + 2; i < end - 1; i++) {
                        tokens.remove(i); // this returns a parameter to give to f
                    }
                    result = Token.stable(Collections.emptyList()); // TODO: soon, actually calculate result
                    tokens.remove(start);
                    tokens.remove(start);
                }
                tokens.add(start, result);
                return 1;
            }
        }));
    }

    public Token peek(String key)
    {
        if(containsKey(key))
        {
            LinkedList<Token> items = get(key);
            if(items != null && !items.isEmpty())
                return items.getFirst();
        }
        throw new NoSuchElementException("Encountered unknown symbol: " + key);
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
            return this;
        }
        throw new NoSuchElementException("Tried to remove (from scope) an unknown symbol: " + key);
    }

    public List<Object> evaluate(List<Token> tokens)
    {
        if(tokens == null)
            return Collections.emptyList();
        List<Object> values = new ArrayList<>(16);
        Token t, t2, t3;
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
                    t3 = peek(t2.mode);
                    if(t3.special < 0)
                    {
                        ((IMorph) t3.solid).morph(tokens, i0, i + 1);
                        i = i0;
                    }
                } else throw new UnsupportedOperationException("Brackets do not match: last bracket is " + t2.bracket
                        + ", first bracket is " + t.bracket);
            } else if ((i0 = t.special) < 0)
            {
                i -= 1 + ((IMorph) t.solid).morph(tokens, i, i - i0);
            } else
            {
                tokens.set(i--, peek(t.contents));
            }
        }
        return values;
    }

}
