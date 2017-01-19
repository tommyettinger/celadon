package lang.celadon;

import squidpony.squidmath.IntVLA;
import squidpony.squidmath.OrderedMap;

import java.util.*;

/**
 * Created by Tommy Ettinger on 1/6/2017.
 */
public class Context extends StackMap<String, Token>{

    protected HashSet<String> reserved;
    public Context()
    {
        super(256, 0.625f, Tools.FalconStringHasher.instance);
        reserved = new HashSet<>(32, 0.625f);
        core();
    }
    public Context(Context existing)
    {
        super(existing, existing.f, Tools.FalconStringHasher.instance);
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
    void reserveFunction(String name, ARun item)
    {
        reserved.add(name);
        put(name, Token.function(item));
    }
    void reserveMacro(String name, IMorph item)
    {
        reserved.add(name);
        put(name, Token.macro(item));
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
                if(start + 2 >= end)
                {
                    result = Token.stable(Collections.emptyList());
                }
                else {
                    Token f = tokens.remove(start+1);
                    if(f.special > 15)
                    {
                        result = ((ARun)f.solid).run(tokens.subList(start+1, end-2));
                    }
                    else
                        result = Token.stable(Collections.emptyList());
                    for (int i = start + 1; i < end - 2; i++) {
                        tokens.remove(start+1); // this was already given to f
                    }
                }
                tokens.remove(start);
                tokens.remove(start);
                tokens.add(start, result);
                return 1;
            }
        });
        reserveBracket("{", new IMorph() {
            @Override
            public int morph(Context context, final List<Token> tokens, int start, int end) {
                Token result;
                if (start + 2 >= end) {
                    tokens.remove(start);
                    tokens.remove(start);
                    return 0;
                }
                Token f = peek(tokens.remove(start + 1).contents);
                if (f.special > 15) { // calling a function as a macro, quoting all arguments
                    List<Token> tks = tokens.subList(start + 1, end - 2);
                    //evaluate(tks);
                    result = ((ARun) f.solid).run(tks);
                    tks.clear();
                    tokens.remove(start);
                    tokens.remove(start);
                    tokens.add(start, result);
                    return 1;
                }
                else if(f.special > 7)
                {
                    List<Token> tks = tokens.subList(start + 1, end - 2);
                    int r =((IMorph)f.solid).morph(Context.this, tks, 0, tks.size());
                    tokens.remove(start);
                    tokens.remove(start+r);
                    return r;
                }

                for (int i = start + 1; i < end - 2; i++) {
                    tokens.remove(start + 1); // this was already given to f
                }
                tokens.remove(start);
                tokens.remove(start);
                //tokens.add(start, result);
                return 0;
            }
        });
        reserveBracket("[", new IMorph() {
            @Override
            public int morph(Context context, final List<Token> tokens, int start, int end) {
                Token result;
                if(start + 2 >= end)
                {
                    result = Token.stable(new ArrayList<Token>());
                }
                else {
                    ArrayList<Token> tks = new ArrayList<Token>(end - start - 1);
                    for (int i = start + 1; i < end - 1; i++) {
                        tks.add(tokens.remove(start + 1));
                    }
                    result = Token.stable(tks);
                }
                tokens.remove(start);
                tokens.remove(start);
                tokens.add(start, result);
                return 1;
            }
        });
        reserveBracket("#map[", new IMorph() {
            @Override
            public int morph(Context context, final List<Token> tokens, int start, int end) {
                Token result;
                if(start + 2 >= end)
                {
                    result = Token.stable(new OrderedMap<Token, Token>());
                }
                else {
                    OrderedMap<Token, Token> tks = new OrderedMap<Token, Token>(end - start - 1);
                    for (int i = start + 1; i < end - 2; i+=2) {
                        tks.put(tokens.remove(start + 1), tokens.remove(start + 1));
                    }
                    if((end - start & 1) == 1)
                        tokens.remove(start+1);
                    result = Token.stable(tks);
                }
                tokens.remove(start);
                tokens.remove(start);
                tokens.add(start, result);
                return 1;
            }
        });

        reserveMacro("def", new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, int start, int end) {
                if(start + 1 < end)
                {
                    String name = tokens.get(start).contents;
                    int pt = step(tokens, start+1);
                    set(name, tokens.get(pt));
                }
                tokens.clear();
                return 0;
            }
        });

        reserveMacro("if", new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, int start, int end) {
                context.step(tokens, start);
                if(tokens.remove(start).asBoolean())
                {
                    start = context.step(tokens, start);
                    for (int i = (start+=1); i < end-1; i++) {
                        tokens.remove(start);
                    }
                }
                else
                {
                    context.step(tokens, context.skip(tokens, start));
                }
                return 1;
            }
        });

        put("+", Token.function(new ARun(this, Collections.<Token>emptyList()) {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 0:
                        return Token.stable(0);
                    case 1:
                        return parameters.get(0);
                    case 2:
                        if(parameters.get(0).floating() || parameters.get(1).floating())
                                return Token.stable(parameters.get(0).asDouble() + parameters.get(1).asDouble());
                        else
                            return Token.stable(parameters.get(0).asLong() + parameters.get(1).asLong());
                    default:
                    {
                        Number num;
                        if(parameters.get(0).floating() || parameters.get(1).floating())
                            num = parameters.get(0).asDouble() + parameters.get(1).asDouble();
                        else
                            num = parameters.get(0).asLong() + parameters.get(1).asLong();
                        for (int i = 2; i < parameters.size(); i++) {
                            if(num instanceof Double || parameters.get(i).floating())
                                num = num.doubleValue() + parameters.get(i).asDouble();
                            else
                                num = num.longValue() + parameters.get(i).asLong();
                        }
                        return Token.stable(num);
                    }
                }
            }
        }));
        put("-", Token.function(new ARun(this, Collections.<Token>emptyList()) {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 0:
                        return Token.stable(0);
                    case 1:
                        if(parameters.get(0).floating())
                            return Token.stable(-parameters.get(0).asDouble());
                        else
                            return Token.stable(-parameters.get(0).asLong());
                    case 2:
                        if(parameters.get(0).floating() || parameters.get(1).floating())
                            return Token.stable(parameters.get(0).asDouble() - parameters.get(1).asDouble());
                        else
                            return Token.stable(parameters.get(0).asLong() - parameters.get(1).asLong());
                    default:
                    {
                        Number num;
                        if(parameters.get(0).floating() || parameters.get(1).floating())
                            num = parameters.get(0).asDouble() - parameters.get(1).asDouble();
                        else
                            num = parameters.get(0).asLong() - parameters.get(1).asLong();
                        for (int i = 2; i < parameters.size(); i++) {
                            if(num instanceof Double || parameters.get(i).floating())
                                num = num.doubleValue() - parameters.get(i).asDouble();
                            else
                                num = num.longValue() - parameters.get(i).asLong();
                        }
                        return Token.stable(num);
                    }

                }
            }
        }));
        put("*", Token.function(new ARun(this, Collections.<Token>emptyList()) {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 0:
                        return Token.stable(1);
                    case 1:
                        if(parameters.get(0).floating())
                            return Token.stable(parameters.get(0).asDouble());
                        else
                            return Token.stable(parameters.get(0).asLong());
                    case 2:
                        if(parameters.get(0).floating() || parameters.get(1).floating())
                            return Token.stable(parameters.get(0).asDouble() * parameters.get(1).asDouble());
                        else
                            return Token.stable(parameters.get(0).asLong() * parameters.get(1).asLong());
                    default:
                    {
                        Number num;
                        if(parameters.get(0).floating() || parameters.get(1).floating())
                            num = parameters.get(0).asDouble() * parameters.get(1).asDouble();
                        else
                            num = parameters.get(0).asLong() * parameters.get(1).asLong();
                        for (int i = 2; i < parameters.size(); i++) {
                            if(num instanceof Double || parameters.get(i).floating())
                                num = num.doubleValue() * parameters.get(i).asDouble();
                            else
                                num = num.longValue() * parameters.get(i).asLong();
                        }
                        return Token.stable(num);
                    }
                }
            }
        }));
        put("/", Token.function(new ARun(this, Collections.<Token>emptyList()) {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 0:
                        return Token.stable(1);
                    case 1:
                        if(parameters.get(0).floating())
                            return Token.stable(1.0 / parameters.get(0).asDouble());
                        else
                            return Token.stable(1L);
                    case 2:
                        if(parameters.get(0).floating() || parameters.get(1).floating())
                            return Token.stable(parameters.get(0).asDouble() / parameters.get(1).asDouble());
                        else
                            return Token.stable(parameters.get(0).asLong() / parameters.get(1).asLong());
                    default:
                    {
                        Number num;
                        if(parameters.get(0).floating() || parameters.get(1).floating())
                            num = parameters.get(0).asDouble() / parameters.get(1).asDouble();
                        else
                            num = parameters.get(0).asLong() / parameters.get(1).asLong();
                        for (int i = 2; i < parameters.size(); i++) {
                            if(num instanceof Double || parameters.get(i).floating())
                                num = num.doubleValue() / parameters.get(i).asDouble();
                            else
                                num = num.longValue() / parameters.get(i).asLong();
                        }
                        return Token.stable(num);
                    }
                }
            }
        }));
        put("%", Token.function(new ARun(this, Collections.<Token>emptyList()) {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 0:
                        return Token.stable(0);
                    case 1:
                        if(parameters.get(0).floating())
                            return Token.stable(parameters.get(0).asDouble());
                        else
                            return Token.stable(parameters.get(0).asLong());
                    default:
                        if(parameters.get(0).floating() || parameters.get(1).floating())
                            return Token.stable(parameters.get(0).asDouble() % parameters.get(1).asDouble());
                        else
                            return Token.stable(parameters.get(0).asLong() % parameters.get(1).asLong());
                }
            }
        }));

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

    public Context assign(String key, Token value)
    {
        if(!reserved.contains(key))
            set(key, value);
        return this;
    }
    public void putTokenEntries(Collection<Token> keyColl, Collection<Token> valueColl)
    {
        Iterator<Token> ki = keyColl.iterator();
        Iterator<Token> vi = valueColl.iterator();
        while (ki.hasNext() && vi.hasNext())
        {
            put(ki.next().contents, vi.next());
        }
    }

    public int step(List<Token> tokens, int start)
    {
        if(tokens == null || tokens.isEmpty() || start >= tokens.size())
            return -1;
        Token t, t2, t3;
        int i0, ql = 0;
        IntVLA bracketPositions = new IntVLA(16);
        for (int i = start; i < tokens.size(); i++) {
            t = tokens.get(i);
            if (t.special > 0) {
                if(bracketPositions.size == 0)
                {
                    return i;
                }
            } else if (t.bracket != null && t.contents != null && t.mode != null) {
                t2 = peek(t.mode);
                if (t2.special < 0) {
                    i -= 1 + ((IMorph) t2.solid).morph(this, tokens, i, i + 1);
                } else
                {
                    if(bracketPositions.size == 0)
                    {
                        return i;
                    }
                }
            } else if (t.bracket != null) {
                if (!t.closing) {
                    bracketPositions.add(i);
                    if(t.bracket.equals("{"))
                        ql++;
                } else if (t.bracketsMatch(t2 = tokens.get(i0 = bracketPositions.pop()))) {
                    t3 = peek(t2.mode);
                    if(t3.special < 0)
                    {
                        if(ql == 0) {
                            ((IMorph) t3.solid).morph(this, tokens, i0, i + 1);
                            i = i0 - 1;
                        }
                        if(t2.bracket.equals("{"))
                            ql--;
                    }
                } else throw new UnsupportedOperationException("Brackets do not match: last bracket is " + t2.bracket
                        + ", first bracket is " + t.bracket);
            } else if ((i0 = t.special) < 0)
            {
                i -= 1 + ((IMorph) t.solid).morph(this, tokens, i, i - i0);
            } else if(ql == 0)
            {
                tokens.set(i--, peek(t.contents));
            }
            else if(bracketPositions.size == 0) // only happens if quotes have somehow gotten out of a macro
            {
                return i;
            }
        }
        return tokens.size();


    }
    public List<Object> evaluate(List<Token> tokens)
    {
        if(tokens == null || tokens.isEmpty())
            return Collections.emptyList();
        List<Object> values = new ArrayList<>(16);
        Token t, t2, t3;
        int i0, ql = 0;
        IntVLA bracketPositions = new IntVLA(16);
        for (int i = 0; i < tokens.size(); i++) {
            t = tokens.get(i);
            if (t.special > 0) {
                if(bracketPositions.size == 0)
                {
                    values.add(t.solid);
                }
            } else if (t.bracket != null && t.contents != null && t.mode != null) {
                t2 = peek(t.mode);
                if (t2.special < 0) {
                    i -= 1 + ((IMorph) t2.solid).morph(this, tokens, i, i + 1);
                } else
                {
                    if(bracketPositions.size == 0)
                    {
                        values.add(t.contents);
                    }
                }
            } else if (t.bracket != null) {
                if (!t.closing) {
                    bracketPositions.add(i);
                    if(t.bracket.equals("{"))
                        ql++;
                } else if (t.bracketsMatch(t2 = tokens.get(i0 = bracketPositions.pop()))) {
                    t3 = peek(t2.mode);
                    if(t3.special < 0)
                    {
                        if(t2.bracket.equals("{"))
                            ql--;
                        if(ql == 0) {
                            ((IMorph) t3.solid).morph(this, tokens, i0, i + 1);
                            i = i0 - 1;
                        }
                    }
                } else throw new UnsupportedOperationException("Brackets do not match: last bracket is " + t2.bracket
                        + ", first bracket is " + t.bracket);
            } else if ((i0 = t.special) < 0)
            {
                i -= 1 + ((IMorph) t.solid).morph(this, tokens, i, i - i0);
            } else if(ql == 0)
            {
                tokens.set(i--, peek(t.contents));
            }
            else if(bracketPositions.size == 0) // only happens if quotes have somehow gotten out of a macro
            {
                values.add(t.contents);
            }
        }
        return values;
    }

    public int skip(List<Token> tokens, int start)
    {
        if(tokens == null || tokens.isEmpty() || start >= tokens.size())
            return -1;
        Token t, t2, t3;
        int i0;
        IntVLA bracketPositions = new IntVLA(16);
        for (int i = start; i < tokens.size(); i++) {
            t = tokens.get(i);
            if (t.special > 0) {
                if(bracketPositions.size == 0)
                {
                    tokens.remove(i);
                    return i;
                }
            } else if (t.bracket != null && t.contents != null && t.mode != null) {
                if(bracketPositions.size == 0) {
                    tokens.remove(i);
                    return i;
                }
            } else if (t.bracket != null) {
                if (!t.closing) {
                    bracketPositions.add(i);
                } else if (t.bracketsMatch(t2 = tokens.get(i0 = bracketPositions.pop()))) {
                    t3 = peek(t2.mode);
                    if(t3.special < 0)
                    {
                        for (int j = i0; j < i; j++) {
                            tokens.remove(i0);
                        }
                        return i0;
                    }
                } else throw new UnsupportedOperationException("Brackets do not match: last bracket is " + t2.bracket
                        + ", first bracket is " + t.bracket);
            }
            else if(bracketPositions.size == 0) // only happens if quotes have somehow gotten out of a macro
            {
                tokens.remove(i);
                return i;
            }
            else
            {
                tokens.remove(i);
                return i;
            }
        }
        return tokens.size();


    }

}
