package lang.celadon;

import regexodus.MatchIterator;
import regexodus.MatchResult;
import regexodus.Matcher;
import regexodus.Pattern;
import squidpony.StringKit;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * The main class that handles script execution, including how symbols are associated to values, how parameters go to
 * functions, how results are given back (possibly multiple results), and what values are yielded back to the code
 * that called the script, which is probably not Celadon code.
 * Created by Tommy Ettinger on 8/20/2017.
 */
public class Manager extends StackMap<String, Cel> {
    /**
     * For exchanging program parameters and results with calling code.
     */
    public ArrayDeque<Object> exchange = new ArrayDeque<>(32);
    /**
     * Shunting-yard output, in reverse Polish notation.
     */
    public ArrayList<Cel> items = new ArrayList<>(256);
    /**
     * Shunting-yard operator stack.
     */
    public ArrayDeque<Cel> operations = new ArrayDeque<>(128);

    /**
     * Direct-from-source-code Cel tokens, before shunting-yard rearrangement.
     */
    public ArrayList<Cel> tokens;

    public Manager() {
        super();
        standardLib();
    }

    public static final Pattern pattern = Pattern.compile(
                    "({=remove}(?:;|^#!)(\\V*))" + // line comment
                    "|(?:({=remove}##\\s*)?(?:" +
                    "({=char}`({=contents}[^\\\\]|(?:\\\\(?:(?:[uU][0-9a-fA-F]{4})|\\V)))`)" +
                    "|({=string}({=bracket}[\"'])({=contents}[\\d\\D]*?)(?<!\\\\){\\bracket})" +
                    "|({=remove}({=bracket}~+!)(?:[\\d\\D]*?){\\/bracket})" +
                    "|(?:({=float}({=sign}[+-]?)(?:(?:NaN)|(?:Infinity)" +
                    "|(?:({=digits}0[xX][0-9a-fA-F]*\\.(?:[0-9a-fA-F]+)" +
                    "(?:[Pp](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))))" + // hex float
                    "|(?:({=digits}[0-9]+\\.[0-9]*" +
                    "(?:[Ee](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))?))" + // scientific notation
                    "))(?:[fF])\\b)" +
                    "|(?:({=double}({=sign}[+-]?)(?:(?:NaN)|(?:Infinity)" +
                    "|(?:({=digits}0[xX][0-9a-fA-F]*\\.(?:[0-9a-fA-F]+)" +
                    "(?:[Pp](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))))" + // hex float
                    "|(?:({=digits}[0-9]+\\.[0-9]*" +
                    "(?:[Ee](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))?))" + // scientific notation
                    "))(?:[dD]?)\\b)" +
                    "|(?:({=long}({=sign}[+-]?)" +
                    "(?:(?:({=hex}0[xX])({=digits}[0-9a-fA-F]{1,16}))" +
                    "|(?:({=bin}0[bB])({=digits}[01]{1,64}))" +
                    "|({=digits}[0-9]+)))" +
                    "[lL]\\b)" +
                    "|(?:({=int}({=sign}[+-]?)" +
                    "(?:(?:({=hex}0[xX])({=digits}[0-9a-fA-F]{1,16}))" +
                    "|(?:({=bin}0[bB])({=digits}[01]{1,64}))" +
                    "|({=digits}[0-9]+)))" +
                    "\\b)" +
                    "|({=open}({=parenthesis}\\()|({=brace}\\{)|({=bracket}\\[))" +
                    "|({=close}({=parenthesis}\\))|({=brace}\\})|({=bracket}\\]))" +
                    "|({=gap}:)" +
                    "|({=now}@)" +
                    "|({=access}\\.)" +
                    "|({=contents}[^,\\[\\]\\(\\)\\{\\}\\:\\@\\.\\s]+)" +
                    "))"
    );
//    public static final Pattern pattern = Pattern.compile(
//            "({=remove}##)?" +
//                    "({=remove}(?:;|^#!)(\\V*))" + // line comment
//                    "|({=char}`({=contents}[^\\\\]|(?:\\\\(?:(?:[uU][0-9a-fA-F]{4})|\\V)))`)" +
//                    "|({=string}({=bracket}[\"'])({=contents}[\\d\\D]*?)(?<!\\\\){\\bracket})" +
//                    "|({=remove}({=bracket}~+!)(?:[\\d\\D]*?){\\/bracket})" +
//                    "|(?:({=float}({=sign}[+-]?)(?:(?:NaN)|(?:Infinity)" +
//                    "|(?:({=digits}0[xX][0-9a-fA-F]*\\.(?:[0-9a-fA-F]+)" +
//                    "(?:[Pp](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))))" + // hex float
//                    "|(?:({=digits}[0-9]+\\.[0-9]*" +
//                    "(?:[Ee](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))?))" + // scientific notation
//                    "))(?:[fF])\\b)" +
//                    "|(?:({=double}({=sign}[+-]?)(?:(?:NaN)|(?:Infinity)" +
//                    "|(?:({=digits}0[xX][0-9a-fA-F]*\\.(?:[0-9a-fA-F]+)" +
//                    "(?:[Pp](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))))" + // hex float
//                    "|(?:({=digits}[0-9]+\\.[0-9]*" +
//                    "(?:[Ee](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))?))" + // scientific notation
//                    "))(?:[dD]?)\\b)" +
//                    "|(?:({=long}({=sign}[+-]?)" +
//                    "(?:(?:({=hex}0[xX])({=digits}[0-9a-fA-F]{1,16}))" +
//                    "|(?:({=bin}0[bB])({=digits}[01]{1,64}))" +
//                    "|({=digits}[0-9]+)))" +
//                    "[lL]\\b)" +
//                    "|(?:({=int}({=sign}[+-]?)" +
//                    "(?:(?:({=hex}0[xX])({=digits}[0-9a-fA-F]{1,16}))" +
//                    "|(?:({=bin}0[bB])({=digits}[01]{1,64}))" +
//                    "|({=digits}[0-9]+)))" +
//                    "\\b)" +
//                    "|({=open}({=parenthesis}\\()|({=brace}\\{)|({=bracket}\\[))" +
//                    "|({=close}({=parenthesis}\\))|({=brace}\\})|({=bracket}\\]))" +
//                    "|({=gap}[:])" +
//                    "|({=contents}[^,\\[\\]\\(\\)\\{\\}\\:\\s]+)"
//    );
    public static final Matcher m = pattern.matcher();

    public void tokenize(CharSequence text)
    {
        tokenize(text, 0, text.length());
    }
    public void tokenize(CharSequence text, int start, int end)
    {
        int len;
        if(text == null || (len = text.length()) == 0) {
            tokens = new ArrayList<>(0);
            return;
        }
        tokens = new ArrayList<>(32 + len >>> 2);
        m.setTarget(text, start, end);
        MatchIterator mi = m.findAll();
        MatchResult mr;
        while (mi.hasNext()) {
            mr = mi.next();
            if (mr.isCaptured("remove"))
                continue;
            if (mr.isCaptured("close")) {
                if (mr.isCaptured("parenthesis"))
                {
                    if(tokens.get(tokens.size() - 1).equals(Cel.openParenthesis))
                        tokens.set(tokens.size() - 1,Cel.empty);
                    else
                        tokens.add(Cel.closeParenthesis);
                }
                else if (mr.isCaptured("brace"))
                    tokens.add(Cel.closeBrace);
                else
                    tokens.add(Cel.closeBracket);
            } else if (mr.isCaptured("open")) {
                if (mr.isCaptured("parenthesis"))
                    tokens.add(Cel.openParenthesis);
                else if (mr.isCaptured("brace"))
                    tokens.add(Cel.openBrace);
                else
                    tokens.add(Cel.openBracket);
            }
            else if(mr.isCaptured("string"))
            {
                tokens.add(new Cel(mr.group("string"), mr.group("contents")));
            }
            else if(mr.isCaptured("char"))
            {
                String s = mr.group("contents");
                if(s.length() == 1)
                {
                    tokens.add(new Cel(mr.group("char"), s.charAt(0)));
                }
                else if(s.length() == 2)
                {
                    switch (s.charAt(1))
                    {
                        case '\\': tokens.add(Cel.backslash);
                            break;
                        case 'r': tokens.add(Cel.carriageReturn);
                            break;
                        case 'n': tokens.add(Cel.newline);
                            break;
                        case 't': tokens.add(Cel.tab);
                            break;
                        case '"': tokens.add(Cel.doubleQuote);
                            break;
                        case '\'': tokens.add(Cel.singleQuote);
                            break;
                        case 'b': tokens.add(Cel.backspace);
                            break;
                        case 'f': tokens.add(Cel.formfeed);
                            break;
                        case '`': tokens.add(Cel.backtick);
                            break;
                        case '0': tokens.add(Cel.nul);
                            break;
                        default:  tokens.add(new Cel(mr.group("char"), s.charAt(1)));
                    }
                }
                else if(s.length() == 6)
                {
                    tokens.add(new Cel(mr.group("char"), (char)StringKit.intFromHex(s, 2, 6)));
                }
            }
            else if(mr.isCaptured("long"))
            {
                if(mr.isCaptured("hex"))
                    tokens.add(new Cel(mr.group("long"), "-".equals(mr.group("sign"))
                            ? -StringKit.longFromHex(mr.group("digits"))
                            : StringKit.longFromHex(mr.group("digits"))));
                else if(mr.isCaptured("bin"))
                    tokens.add(new Cel(mr.group("long"), "-".equals(mr.group("sign"))
                            ? -StringKit.longFromBin(mr.group("digits"))
                            : StringKit.longFromBin(mr.group("digits"))));
                else
                {
                    tokens.add(new Cel(mr.group("long"), StringKit.longFromDec(mr.group("long"))));
                }
            }
            else if(mr.isCaptured("float"))
            {
                try {
                    tokens.add(new Cel(mr.group("float"), Float.parseFloat(mr.group("float"))));
                }catch (NumberFormatException nfe)
                {
                    tokens.add(new Cel(mr.group("float"), Float.POSITIVE_INFINITY));
                }
            }
            else if(mr.isCaptured("double"))
            {
                try {
                    tokens.add(new Cel(mr.group("double"), Double.parseDouble(mr.group("double"))));
                }catch (NumberFormatException nfe)
                {
                    tokens.add(new Cel(mr.group("double"), Double.POSITIVE_INFINITY));
                }
            }
            else if(mr.isCaptured("int"))
            {
                if(mr.isCaptured("hex"))
                    tokens.add(new Cel(mr.group("int"), "-".equals(mr.group("sign"))
                            ? -StringKit.intFromHex(mr.group("digits"))
                            : StringKit.intFromHex(mr.group("digits"))));
                else if(mr.isCaptured("bin"))
                    tokens.add(new Cel(mr.group("int"), "-".equals(mr.group("sign"))
                            ? -StringKit.intFromBin(mr.group("digits"))
                            : StringKit.intFromBin(mr.group("digits"))));
                else
                {
                    tokens.add(new Cel(mr.group("int"), StringKit.intFromDec(mr.group("int"))));
                }
            }
            else if(mr.isCaptured("gap"))
            {
                tokens.add(Cel.gap);
            }
            else if(mr.isCaptured("now"))
            {
                tokens.add(Cel.now);
            }
            else if(mr.isCaptured("access"))
            {
                tokens.add(Cel.access);
            }
            else
                tokens.add(new Cel(mr.group("contents"), Syntax.SYMBOL));
        }
    }

    public Cel resolve(Cel item)
    {
        if(item == null || item.ref == null)
            return Cel.nothing;
        if(Syntax.SYMBOL.equals(item.ref)) {
            Object o;
            for (int i = 0; i < 256; i++) { // hard-code 256 max indirections to avoid cycles
                o = get(item.title);
                if (o == null)
                    return null;
                if (o instanceof Cel)
                    item = ((Cel) o); // move current item to what it references
                if(!Syntax.SYMBOL.equals(item.ref))
                    return item;
            }
        }
        return item;
    }

    public void shunt()
    {
        int len = tokens.size();
        if(len <= 0)
            return;
        Cel current, topOperator = null;
        Object item;
        String title;
        for (int i = 0; i < len; i++) {
            current = resolve(tokens.get(i));
            if(current == null)
                continue;
            item = current.ref;
            //title = (current == null) ? null : current.title;
            if(item != null && Operator.registry.contains(item))
            {
                Operator operator = (Operator)item;
                topOperator = operations.peekFirst();
                while (topOperator != null && (topOperator.ref instanceof Operator) && ((Operator)topOperator.ref).precedence > operator.precedence) {
                    items.add(topOperator);
                    operations.pollFirst();
                    topOperator = operations.peekFirst();
                }

                operations.addFirst(current);
            }
            else if(Syntax.OPEN_PARENTHESIS.equals(item))
            {
                operations.addFirst(current);
            }
            else if(Syntax.CLOSE_PARENTHESIS.equals(item))
            {
                topOperator = operations.peekFirst();
                while (topOperator != null && !Syntax.OPEN_PARENTHESIS.equals(topOperator.ref)) {
                    items.add(topOperator);
                    operations.pollFirst();
                    topOperator = operations.peekFirst();
                }
                operations.pollFirst();
            }
            else
            {
                items.add(current);
            }
        }
        items.addAll(operations);
        operations.clear();
    }
    
    public void evaluate()
    {
        int len = items.size();
        Cel item, left, right;
        for (int i = 0; i < len; i++) {
            item = resolve(items.get(i));
            if(item.ref instanceof Procedural)
            {
                if(i < 2 || (left = resolve(items.get(i - 2))).ref instanceof Procedural || Syntax.CLOSE_PARENTHESIS.equals(left.ref))
                {
                    item = ((Procedural) item.ref).run(Cel.empty, resolve(items.remove(--i)));
                    items.set(i, item);
                    --len;
                }
                else
                {
                    item = ((Procedural) item.ref).run(left, resolve(items.remove(--i)));
                    items.remove(--i);
                    items.set(i, item);
                    len -= 2;
                }
            }
        }
        exchange.addAll(items);
    }

    public void learn(String name, Object value)
    {
        put(name, new Cel(name, value));
    }

    public void standardLib()
    {
        if(containsKey("+"))
            return;
        learn("+", new Operator(10) {
            @Override
            public Cel run(Cel left, Cel right) {
                if(Cel.isNumeric(left) && Cel.isNumeric(right)) {
                    if (Cel.isFloating(left) || Cel.isFloating(right)) {
                        return new Cel(Core.asDouble(left.ref) + Core.asDouble(right.ref));
                    }
                    else
                    {
                        return new Cel(Core.asLong(left.ref) + Core.asLong(right.ref));
                    }
                }
                return Cel.zeroInt;
            }
        });
        learn("-", new Operator(10) {
            @Override
            public Cel run(Cel left, Cel right) {
                if(Cel.isNumeric(left) && Cel.isNumeric(right)) {
                    if (Cel.isFloating(left) || Cel.isFloating(right)) {
                        return new Cel(Core.asDouble(left.ref) - Core.asDouble(right.ref));
                    }
                    else
                    {
                        return new Cel(Core.asLong(left.ref) - Core.asLong(right.ref));
                    }
                }
                else if(Syntax.EMPTY.equals(left.ref) && Cel.isNumeric(right))
                {
                    if(Cel.isFloating(right))
                        return new Cel(-Core.asDouble(right.ref));
                    else
                        return new Cel(-Core.asLong(right.ref));
                }
                return Cel.zeroInt;

            }
        });
        learn("*", new Operator(11) {
            @Override
            public Cel run(Cel left, Cel right) {
                if(Cel.isNumeric(left) && Cel.isNumeric(right)) {
                    if (Cel.isFloating(left) || Cel.isFloating(right)) {
                        return new Cel(Core.asDouble(left.ref) * Core.asDouble(right.ref));
                    }
                    else
                    {
                        return new Cel(Core.asLong(left.ref) * Core.asLong(right.ref));
                    }
                }
                return Cel.zeroInt;
            }
        });
        learn("/", new Operator(11) {
            @Override
            public Cel run(Cel left, Cel right) {
                if(Cel.isNumeric(left) && Cel.isNumeric(right)) {
                    if (Cel.isFloating(left) || Cel.isFloating(right)) {
                        return new Cel(Core.asDouble(left.ref) / Core.asDouble(right.ref));
                    }
                    else
                    {
                        return new Cel(Core.asLong(left.ref) / Core.asLong(right.ref));
                    }
                }
                return Cel.zeroInt;
            }
        });
        learn("%", new Operator(11) {
            @Override
            public Cel run(Cel left, Cel right) {
                if(Cel.isNumeric(left) && Cel.isNumeric(right)) {
                    if (Cel.isFloating(left) || Cel.isFloating(right)) {
                        return new Cel(Core.asDouble(left.ref) % Core.asDouble(right.ref));
                    }
                    else
                    {
                        return new Cel(Core.asLong(left.ref) % Core.asLong(right.ref));
                    }
                }
                return Cel.zeroInt;
            }
        });
        learn("sin", new Procedural() {
            @Override
            public Cel run(Cel left, Cel right) {
                if(Cel.isFloating(right)) {
                    return new Cel(Math.sin(Core.asDouble(right.ref)));
                }
                return Cel.zeroInt;
            }
        });
        learn("cos", new Procedural() {
            @Override
            public Cel run(Cel left, Cel right) {
                if(Cel.isFloating(right)) {
                    return new Cel(Math.cos(Core.asDouble(right.ref)));
                }
                return Cel.zeroInt;
            }
        });
        learn("tan", new Procedural() {
            @Override
            public Cel run(Cel left, Cel right) {
                if(Cel.isFloating(right)) {
                    return new Cel(Math.tan(Core.asDouble(right.ref)));
                }
                return Cel.zeroInt;
            }
        });



    }
}
