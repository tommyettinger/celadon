package lang.celadon;

import squidpony.squidmath.IntVLA;
import squidpony.squidmath.OrderedMap;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Tommy Ettinger on 1/6/2017.
 */
public class Context extends StackMap<String, Token> implements Serializable{
    private static final long serialVersionUID = 0;
    protected HashSet<String> reserved;
    public Context()
    {
        super(256, 0.625f, Tools.WispStringHasher.instance);
        reserved = new HashSet<>(32, 0.625f);
        core();
    }
    public Context(boolean empty)
    {
        super(empty ? 0 : 256, 0.625f, Tools.WispStringHasher.instance);
        reserved = new HashSet<>(32, 0.625f);
        if(!empty) core();
    }
    public Context(Context existing)
    {
        super(existing, existing.f, Tools.WispStringHasher.instance);
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

    void reserveUnusual(String name, Token item)
    {
        reserved.add(name);
        put(name, item);
    }

    static final Token quote = new Token(-256, new IMorph() {
        @Override
        public int morph(Context context, List<Token> tokens, int start, int end) {
            int ns = context.nextStop(tokens, start + 1);
            if(ns < start)
                return 0;
            else if(ns == start + 2)
            {
                Token t = tokens.get(start + 1);
                if(t.contents != null && !t.contents.isEmpty())
                {
                    Token r = Token.stable(t);
                    r.contents = t.contents;
                    tokens.set(start, r);
                    tokens.remove(start + 1);
                    return 1;
                }
                else
                {
                    tokens.remove(start);
                    return 0;
                }
            }
            List<Token> sl = tokens.subList(start + 1, ns);
            TList tl = new TList(sl);
            sl.clear();
            tokens.add(start, Token.stable(tl));
            return 1;
        }
    }), unquote = new Token(-257, new IMorph() {
        @Override
        public int morph(Context context, List<Token> tokens, int start, int end) {
            int ns = context.step(tokens, start);
            if(ns < start)
                return 0;

            Token t = tokens.get(ns);
            if(t.solid != null && t.solid instanceof Token)
            {
                tokens.set(ns, (Token) t.solid);
                return 1;
            }
            else if(t.solid != null && t.solid instanceof TList) {
                tokens.remove(ns);
                tokens.addAll(start, (TList) t.solid);
                return ((TList) t.solid).size();
            }
            else if(t.contents != null && !t.contents.isEmpty() && context.containsKey(t.contents))
            {
                tokens.set(ns, context.get(t.contents));
                return 1;
            }
            else
            {
                return 0;
            }
        }
    });

    protected void core()
    {
        reserve("null",null);
        reserve("true", true);
        reserve("false", false);
        put("chaos", Token.stable(new Wrappers._StatefulRNG()));
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
                    else if(f.solid instanceof ICallByName && start + 3 < end)
                    {
                        result = ((ICallByName)f.solid).call(tokens.get(start+1), tokens.subList(start+2, end - 2));
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
                    int r =((IMorph)f.solid).morph(context, tks, 0, tks.size());
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
                if(start + 1 >= end)
                {
                    result = Token.stable(new TList());
                }
                else {
                    TList tks = new TList(end - start - 1);
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
        reserveBracket("#(", new IMorph() {
            @Override
            public int morph(Context context, final List<Token> tokens, int start, int end) {
                Token result;
                if (start + 2 >= end) {
                    tokens.remove(start);
                    tokens.remove(start);
                    return 0;
                }
                Token f = tokens.remove(start + 1);
                if (f.special > 15) { // normal function call
                    List<Token> tks = tokens.subList(start + 1, end - 2);
                    //evaluate(tks);
                    result = ((ARun) f.solid).run(tks);
                    tks.clear();
                    tokens.remove(start);
                    tokens.set(start, result);
                    return 1;
                }
                else if(f.special > 7)
                {
                    List<Token> tks = tokens.subList(start + 1, end - 2);
                    int r =((IMorph)f.solid).morph(context, tks, 0, tks.size());
                    tokens.remove(start);
                    tokens.remove(start+r);
                    return r;
                }
                else if(f.solid instanceof ICallByName && start + 3 < end)
                {
                    List<Token> tks;
                    result = ((ICallByName)f.solid).call(tokens.remove(start+1), (tks = tokens.subList(start+1, end - 3)));
                    tks.clear();
                    tokens.remove(start);
                    tokens.set(start, result);
                    return 1;
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

        reserveBracket("#0(", new IMorph() {
            @Override
            public int morph(Context context, final List<Token> tokens, int start, int end) {
                if (start + 2 >= end) {
                    tokens.remove(start);
                    tokens.remove(start);
                    return 0;
                }
                Token f = tokens.remove(start + 1);
                if (f.special > 15) { // normal function call
                    List<Token> tks = tokens.subList(start + 1, end - 2);
                    //evaluate(tks);
                    ((ARun) f.solid).run(tks);
                    tks.clear();
                    tokens.remove(start);
                    tokens.remove(start);
                    return 0;
                }
                else if(f.special > 7)
                {
                    List<Token> tks = tokens.subList(start + 1, end - 2);
                    int r =((IMorph)f.solid).morph(context, tks, 0, tks.size());
                    tokens.remove(start);
                    for (int i = 0; i <= r; i++) {
                        tokens.remove(start);
                    }
                    return 0;
                }
                else if(f.solid instanceof ICallByName && start + 3 < end)
                {
                    List<Token> tks;
                    ((ICallByName)f.solid).call(tokens.remove(start+1), (tks = tokens.subList(start+1, end - 3)));
                    tks.clear();
                    tokens.remove(start);
                    tokens.remove(start);
                    return 0;
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

        reserveUnusual(":", quote);


        reserveUnusual("@", unquote);

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

        reserveBracket("#set[", new IMorph() {
            @Override
            public int morph(Context context, final List<Token> tokens, int start, int end) {
                Token result;
                if(start + 1 >= end)
                {
                    result = Token.stable(new TSet());
                }
                else {
                    TSet tks = new TSet(end - start - 1);
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

        reserveMacro("def", new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, int start, int end) {
                if(start + 1 < end)
                {
                    String name = tokens.get(start).contents;
                    int pt = step(tokens, start+1);
                    assign(name, tokens.get(pt));
                }
                tokens.clear();
                return 0;
            }
        });

        reserveMacro("mutant", new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, int start, int end) {
                if(start + 1 < end)
                {
                    String name = tokens.get(start).asString();
                    if(name.equals("@")) {
                        ((IMorph)unquote.solid).morph(context, tokens, ++start, start+1);
                        name = tokens.get(start).asString();
                    }
                    Token f = Token.envoy(new Mutant(context, name, tokens, start + 1, end));
                    tokens.clear();
                    assign(name, f);
                    return 0;
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
                    for (int i = ++start; i < end-1; i++) {
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

        reserveMacro("=", new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, int start, int end) {
                if(start + 1 < end)
                {
                    String name = tokens.remove(start).contents;
                    int pt = step(tokens, start);
                    Token tk = tokens.get(pt);
                    assign(name, tk);
                    tokens.clear();
                    tokens.add(tk);
                    return 1;
                }
                tokens.clear();
                return 0;
            }
        });

        reserveMacro("fn", new IMorph() {
            @Override
            public int morph(final Context ctx, List<Token> tokens, final int start, int end) {
                int lastBracket = nextStop(tokens, start);
                Token f = Token.function(new ARun(ctx, tokens, start + 1, lastBracket, lastBracket + 1, end) {
                    @Override
                    public Token run(List<Token> parameters) {
                        this.context.putTokenEntries(names, parameters);
                        Token r = this.context.get("null");
                        while (this.context.step(body, 0) >= 0)
                        {
                            r = body.remove(0);
                        }
                        body.addAll(bodyFixed);
                        return r;
                    }
                });
                tokens.clear();
                tokens.add(f);
                return 1;
            }
        });

        reserveMacro("defn", new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, int start, int end) {
                if(start + 2 < end)
                {
                    String name = tokens.get(start).contents;
                    ((IMorph)get("fn").solid).morph(context, tokens, start+1, end);
                    Token f = tokens.remove(0);
                    ((ARun)f.solid).title = name;
                    assign(name, f);
                    return 0;
                }
                tokens.clear();
                return 0;
            }
        });

        //There are currently issues with calling something other than a solid IMorph or ARun when it is the first
        //element of a quoted-call form (in curly braces). Since this next macro would produce a solid IMorph only
        //after it is evaluated, and a quoted-call form prevents that evaluation initially, this doesn't work. But,
        //defmacro does work, see below.
        /*
        reserveMacro("macro", new IMorph() {
            @Override
            public int morph(final Context ctx, List<Token> tokens, final int start, int end) {
                int lastBracket = nextStop(tokens, start);
                Token f = Token.macro(new Macro(ctx, tokens, start + 1, lastBracket, lastBracket + 1, end));
                tokens.clear();
                tokens.add(f);
                return 1;
            }
        });
        */

        reserveMacro("defmacro", new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, int start, int end) {
                if(start + 2 < end)
                {
                    String name = tokens.get(start).contents;
                    int lastBracket = nextStop(tokens, start+1);
                    Token f = Token.macro(new Macro(context, name, tokens, start + 2, lastBracket, lastBracket + 1, end));
                    tokens.clear();
                    assign(name, f);
                    return 0;
                }
                tokens.clear();
                return 0;
            }
        });

        reserveMacro("while", new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, final int start, final int end) {
                int pos = context.nextStop(tokens, start)+1, bodyStart = pos, startTemp;
                List<Token> condition = tokens.subList(start, pos),
                        conditionFixed = new TList(condition),
                        bodyFixed = new TList(tokens.subList(bodyStart, end)),
                        results = new TList();
                while ((pos = context.step(tokens, start)) >= 0) {
                    if (tokens.remove(pos).asBoolean()) {
                        startTemp = pos;
                        while ((startTemp = step(tokens, startTemp)+1) > 0)
                        {}
                        results.addAll(tokens);
                        tokens.clear();
                        tokens.addAll(start, conditionFixed);
                        tokens.addAll(bodyFixed);
                    }
                    else
                    {
                        tokens.clear();
                        /*
                        for (int i = tokens.size()-1; i >= startTemp; i--) {
                            tokens.remove(i);
                        }*/
                        break;
                    }
                }
                tokens.addAll(start, results);
                return results.size();
            }
        });


        reserveMacro("and", new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, int start, int end) {
                if(start >= end) {
                    tokens.clear();
                    tokens.add(get("true"));
                }
                else
                {
                    start = context.step(tokens, start);
                    Token tk;
                    if(start + 1 == end)
                    {
                        tk = tokens.get(start);
                        tokens.clear();
                        tokens.add(tk);
                    }
                    else
                    {
                        while ((tk = tokens.remove(start)).asBoolean())
                        {
                            if(context.step(tokens, start) < 0) break;
                        }
                        if(tokens.isEmpty())
                        {
                            tokens.add(tk.asBoolean() ? tk : get("false"));
                        }
                        else
                        {
                            tokens.clear();
                            tokens.add(get("false"));
                        }
                    }
                }
                return 1;
            }
        });

        reserveMacro("or", new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, int start, int end) {
                if(start >= end) {
                    tokens.clear();
                    tokens.add(get("true"));
                }
                else
                {
                    start = context.step(tokens, start);
                    Token tk = tokens.get(start);
                    if(start + 1 == end)
                    {
                        tokens.clear();
                        tokens.add(tk);
                    }
                    else
                    {
                        if (tk.asBoolean())
                        {
                            tokens.clear();
                            tokens.add(tk);
                        }
                        else
                        {
                            start = context.step(tokens, start);
                            while (start >= 0 && !(tk = tokens.get(start)).asBoolean()) {
                                start = context.step(tokens, start+1);
                            }
                            tokens.clear();
                            if(start >= 0)
                                tokens.add(tk);
                            else
                                tokens.add(get("false"));
                        }
                    }
                }
                return 1;
            }
        });

        put("repeat", Token.macro(new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, int start, int end) {
                if(end - 2 < start)
                    return 0;
                int pos = context.step(tokens, start),
                        ct = tokens.get(pos).asInt(),
                        ns = context.nextStop(tokens, pos+1);
                if(ns == pos+2)
                {
                    Token body = tokens.get(pos+1);
                    tokens.clear();
                    for (int i = 0; i < ct; i++) {
                        tokens.add(body);
                    }
                    return ct;
                }
                else
                {
                    List<Token> body = new TList(tokens.subList(pos+1, ns+1));
                    tokens.clear();
                    for (int i = 0; i < ct; i++) {
                        tokens.addAll(body);
                    }
                    return ct * body.size();
                }
            }
        }));

        put("==", Token.function(new ARun(this, "==") {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 0:
                        return get("true");
                    case 1:
                        return get("true");
                    case 2:
                        return ((parameters.get(0).solid != null && parameters.get(0).solid.equals(parameters.get(1)))
                                || parameters.get(0).solid == parameters.get(1).solid
                                || parameters.get(0).numericallyEqual(parameters.get(1))) ? get("true") : get("false");
                    default:
                    {
                        if(!((parameters.get(0).solid != null && parameters.get(0).solid.equals(parameters.get(1)))
                                || parameters.get(0).solid == parameters.get(1).solid
                                || parameters.get(0).numericallyEqual(parameters.get(1))))
                            return get("false");
                        for (int i = 2; i < parameters.size(); i++) {
                            if(!((parameters.get(0).solid != null && parameters.get(0).solid.equals(parameters.get(i)))
                                    || parameters.get(0).solid == parameters.get(i).solid
                                    || parameters.get(0).numericallyEqual(parameters.get(i))))
                                return get("false");
                        }
                        return get("true");
                    }
                }
            }
        }));

        put("<", Token.function(new ARun(this, "<") {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 0:
                        return get("true");
                    case 1:
                        return get("true");
                    case 2:
                        return (parameters.get(0).lt(parameters.get(1))) ? get("true") : get("false");
                    default:
                    {
                        if(!parameters.get(0).lt(parameters.get(1)))
                            return get("false");
                        for (int i = 2; i < parameters.size(); i++) {
                            if(!parameters.get(i-1).lt(parameters.get(i)))
                                return get("false");
                        }
                        return get("true");
                    }
                }
            }
        }));

        put(">", Token.function(new ARun(this, ">") {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 0:
                        return get("true");
                    case 1:
                        return get("true");
                    case 2:
                        return (parameters.get(0).gt(parameters.get(1))) ? get("true") : get("false");
                    default:
                    {
                        if(!parameters.get(0).gt(parameters.get(1)))
                            return get("false");
                        for (int i = 2; i < parameters.size(); i++) {
                            if(!parameters.get(i-1).gt(parameters.get(i)))
                                return get("false");
                        }
                        return get("true");
                    }
                }
            }
        }));

        put("<=", Token.function(new ARun(this, "<=") {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 0:
                        return get("true");
                    case 1:
                        return get("true");
                    case 2:
                        return (parameters.get(0).lte(parameters.get(1))) ? get("true") : get("false");
                    default:
                    {
                        if(!parameters.get(0).lte(parameters.get(1)))
                            return get("false");
                        for (int i = 2; i < parameters.size(); i++) {
                            if(!parameters.get(i-1).lte(parameters.get(i)))
                                return get("false");
                        }
                        return get("true");
                    }
                }
            }
        }));

        put(">=", Token.function(new ARun(this, ">=") {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 0:
                        return get("true");
                    case 1:
                        return get("true");
                    case 2:
                        return (parameters.get(0).gte(parameters.get(1))) ? get("true") : get("false");
                    default:
                    {
                        if(!parameters.get(0).gte(parameters.get(1)))
                            return get("false");
                        for (int i = 2; i < parameters.size(); i++) {
                            if(!parameters.get(i-1).gte(parameters.get(i)))
                                return get("false");
                        }
                        return get("true");
                    }
                }
            }
        }));

        put("+", Token.function(new ARun(this, "+") {
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
        put("-", Token.function(new ARun(this, "-") {
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
        put("*", Token.function(new ARun(this, "*") {
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
        put("/", Token.function(new ARun(this, "/") {
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
        put("%", Token.function(new ARun(this, "%") {
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
        put("<<", Token.function(new ARun(this, "<<") {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 2:
                        return Token.stable(parameters.get(0).asLong() << parameters.get(1).asLong());
                    default:
                    {
                        return Token.stable(0);
                    }
                }
            }
        }));
        put(">>", Token.function(new ARun(this, ">>") {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 2:
                        return Token.stable(parameters.get(0).asLong() >> parameters.get(1).asLong());
                    default:
                    {
                        return Token.stable(0);
                    }
                }
            }
        }));
        put(">>>", Token.function(new ARun(this, ">>>") {
            @Override
            public Token run(List<Token> parameters) {
                //context.putTokenEntries(names, parameters); // commonly called at the start of a normal run impl
                switch (parameters.size()) {
                    case 2:
                        return Token.stable(parameters.get(0).asLong() >>> parameters.get(1).asLong());
                    default:
                    {
                        return Token.stable(0);
                    }
                }
            }
        }));

        put("++", Token.macro(new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, int start, int end) {
                if(start + 1 == end)
                {
                    String name = tokens.get(start).contents;
                    Token tk = tokens.get(step(tokens, start));
                    if(tk.floating())
                        assign(name, tk = Token.stable(tk.asDouble()+1.0));
                    else if(tk.numeric())
                        assign(name, tk = Token.stable(tk.asLong()+1L));
                    tokens.clear();
                    tokens.add(tk);
                    return 1;
                }
                tokens.clear();
                return 0;
            }
        }));

        put("--", Token.macro(new IMorph() {
            @Override
            public int morph(Context context, List<Token> tokens, int start, int end) {
                if(start + 1 == end)
                {
                    String name = tokens.get(start).contents;
                    Token tk = tokens.get(step(tokens, start));
                    if(tk.floating())
                        assign(name, tk = Token.stable(tk.asDouble()-1.0));
                    else if(tk.numeric())
                        assign(name, tk = Token.stable(tk.asLong()-1L));
                    tokens.clear();
                    tokens.add(tk);
                    return 1;
                }
                tokens.clear();
                return 0;
            }
        }));
        put("rng", Token.function(new ARun(this, "rng") {
            @Override
            public Token run(List<Token> parameters) {
                return parameters.isEmpty()
                        ? Token.stable(new Wrappers._StatefulRNG())
                        : parameters.get(0).numeric()
                        ? Token.stable(new Wrappers._StatefulRNG(parameters.get(0).asLong()))
                        : Token.stable(new Wrappers._StatefulRNG(parameters.get(0).asString()));
            }
        }));
    }

    public Token peek(String key)
    {
        if(containsKey(key))
        {
            return get(key);
        }
        return get("null");
        //throw new NoSuchElementException("Encountered unknown symbol: " + key);
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
        TList etc = new TList(valueColl.size());
        while (vi.hasNext())
        {
            if(ki.hasNext())
                put(ki.next().contents, vi.next());
            else
                etc.add(vi.next());
        }
        put("...", Token.stable(etc));

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
                        if(t2.bracket.equals("{"))
                        {
                            ql--;
                        }
                        if(ql == 0) {
                            ((IMorph) t3.solid).morph(this, tokens, i0, i + 1);
                            i = i0 - 1;
                        }
                    }
                } else throw new UnsupportedOperationException("Brackets do not match: last bracket is " + t.bracket
                        + ", first bracket is " + t2.bracket);
            } else if ((i0 = t.special) < 0)
            {
                i -= ((IMorph) t.solid).morph(this, tokens, i, i - i0);
            } else if(ql == 0)
            {
                tokens.set(i--, peek(t.contents));
            }
            else if(bracketPositions.size == 0) // only happens if quotes have somehow gotten out of a macro
            {
                return i;
            }
        }
        return -1;


    }
    public TList solidify (TList tokens)
    {
        if(tokens == null || tokens.isEmpty())
            return TList.empty;
        int n = 0;
        while (true)
        {
            n = step(tokens, n);
            if(n >= 0)
                n++;
            else
                break;
        }
        return tokens;
    }
    public List<Object> evaluate(TList tokens)
    {
        if(tokens == null || tokens.isEmpty())
            return Collections.emptyList();
        return solidify(tokens).as(Object.class);
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
                } else throw new UnsupportedOperationException("Brackets do not match: last bracket is " + t.bracket
                        + ", first bracket is " + t2.bracket);
            }
            else
            {
                tokens.remove(i);
                if((-t.special & 259) < 256)
                    return i;
            }
        }
        return -1;
    }
    public int nextStop(List<Token> tokens, int start)
    {
        if(tokens == null || tokens.isEmpty() || start >= tokens.size())
            return -1;
        Token t, t2, t3;
        IntVLA bracketPositions = new IntVLA(16);
        for (int i = start; i < tokens.size(); i++) {
            t = tokens.get(i);
            if (t.special > 0) {
                if(bracketPositions.size == 0)
                {
                    return i+1;
                }
            } else if (t.bracket != null && t.contents != null && t.mode != null) {
                if(bracketPositions.size == 0) {
                    return i+1;
                }
            } else if (t.bracket != null) {
                if (!t.closing) {
                    bracketPositions.add(i);
                } else if (t.bracketsMatch(t2 = tokens.get(bracketPositions.pop()))) {
                    t3 = peek(t2.mode);
                    if(t3.special < 0 && bracketPositions.size == 0)
                    {
                        return i; // note this ends before the others, omitting the closing bracket
                    }
                } else throw new UnsupportedOperationException("Brackets do not match: last bracket is " + t.bracket
                        + ", first bracket is " + t2.bracket);
            }
            else
            {
                if((-t.special & 259) < 256 && bracketPositions.size == 0)
                    return i+1;
            }
        }
        return -1;
    }

}
