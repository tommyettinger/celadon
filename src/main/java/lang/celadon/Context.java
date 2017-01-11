package lang.celadon;

import squidpony.squidmath.IntVLA;

import java.util.*;

/**
 * Created by Tommy Ettinger on 1/6/2017.
 */
public class Context extends StackMap<String, Token>{

    protected HashSet<String> reserved;
    public Context()
    {
        super(256, 0.625f);
        reserved = new HashSet<>(32, 0.625f);
        core();

    }
    public Context(Context existing)
    {
        super(existing);
        reserved = new HashSet<>(existing.reserved);

    }

    void reserve(String name, Object item)
    {
        reserved.add(name);
        put(name, Token.stable(item));
    }
    void reserveSymbol(String name, IMorph item)
    {
        reserved.add(name);
        put(name, Token.envoy(item));
    }
    void reserveBracket(String name, IMorph item)
    {
        reserved.add(name);
        put(name, Token.varying(item));

    }
    protected void core()
    {
        reserve("null",null);
        reserve("true", true);
        reserve("false", false);
        reserveBracket("(", new IMorph() {
            @Override
            public int morph(Context context, final List<Token> tokens, int start, int end) {
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
                }
                tokens.remove(start);
                tokens.remove(start);
                tokens.add(start, result);
                return 1;
            }
        });
    }

    public Token peek(String key)
    {
        if(containsKey(key))
        {
            return get(key);
        }
        throw new NoSuchElementException("Encountered unknown symbol: " + key);
    }
    public Context push(String key, Token value)
    {
        if(!reserved.contains(key))
            put(key, value);
        return this;
    }
    public Context pop(String key)
    {
        if(containsKey(key))
        {
            if(!reserved.contains(key))
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
                    i -= 1 + ((IMorph) t2.solid).morph(this, tokens, i, i + 1);
                } else
                    values.add(t.contents);
            } else if (t.bracket != null) {
                if (!t.closing) {
                    bracketPositions.add(i);
                } else if (t.bracketsMatch(t2 = tokens.get(i0 = bracketPositions.pop()))) {
                    t3 = peek(t2.mode);
                    if(t3.special < 0)
                    {
                        ((IMorph) t3.solid).morph(this, tokens, i0, i + 1);
                        i = i0 - 1;
                    }
                } else throw new UnsupportedOperationException("Brackets do not match: last bracket is " + t2.bracket
                        + ", first bracket is " + t.bracket);
            } else if ((i0 = t.special) < 0)
            {
                i -= 1 + ((IMorph) t.solid).morph(this, tokens, i, i - i0);
            } else
            {
                tokens.set(i--, peek(t.contents));
            }
        }
        return values;
    }

}
