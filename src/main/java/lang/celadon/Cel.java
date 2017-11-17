package lang.celadon;

import squidpony.squidmath.CrossHash;

import java.util.Objects;

/**
 * Indirection cell; has a name that may be used to look it up, and any Object for a value.
 * Created by Tommy Ettinger on 8/20/2017.
 */
public class Cel {
    public String title;
    public Object ref;

    public Cel() {
        title = null;
        ref = null;
    }
    public Cel(Object ref)
    {
        this.title = (ref == null) ? "null" : ref.toString();
        this.ref = ref;
    }

    public Cel(String title, Object ref) {
        this.title = title;
        this.ref = ref;
    }

    public static boolean isNumeric(Cel o)
    {
        return o != null && o.ref != null && ((o.ref instanceof Number) || (o.ref instanceof Boolean));
    }

    public static boolean isFloating(Cel o)
    {
        return o != null && o.ref != null && ((o.ref instanceof Double) || (o.ref instanceof Float));
    }


    @Override
    public String toString() {
        if(ref == null) return title == null ? "null" : title + " = UNBOUND";
        if(ref instanceof Syntax)
        {
            switch ((Syntax)ref)
            {
                case SYMBOL: return title;
                case GAP: return ":";
                case NOW: return "@";
                case ACCESS: return ".";
                case EMPTY: return "()";
                case OPEN_PARENTHESIS: return "(";
                case CLOSE_PARENTHESIS: return ")";
                case OPEN_BRACKET: return "[";
                case CLOSE_BRACKET: return "]";
                case OPEN_BRACE: return "{";
                case CLOSE_BRACE: return "}";
            }
        }
        if(ref instanceof CharSequence)
            return "'" + ref + "'";
        if(ref instanceof Character)
            return "`" + ref + "`";
        if(ref instanceof Procedural)
            return title;
        return ref.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(ref != null)
        {
            if (o == null) return false;
            if (getClass() == o.getClass()) {
                Cel item = (Cel) o;
                return ref.equals(item.ref);
            } else
                return ref.getClass() == o.getClass() && ref.equals(o);
        }
        else return o == null || (o.getClass() == getClass() && ((Cel)o).ref == null);
    }

    @Override
    public int hashCode() {
        return ref != null ? ref.hashCode() : 0;
    }

    public static class HasherByName implements CrossHash.IHasher
    {
        @Override
        public int hash(Object data)
        {
            return data == null || !(data instanceof Cel) ? 0 : CrossHash.hash(((Cel)data).title);
        }

        @Override
        public boolean areEqual(Object left, Object right)
        {
            return Objects.equals(left, right);
        }
    }
    public static final Cel openParenthesis = new Cel("(", Syntax.OPEN_PARENTHESIS);
    public static final Cel openBrace = new Cel("{", Syntax.OPEN_BRACE);
    public static final Cel openBracket = new Cel("[", Syntax.OPEN_BRACKET);
    public static final Cel closeParenthesis = new Cel(")", Syntax.CLOSE_PARENTHESIS);
    public static final Cel closeBrace = new Cel("}", Syntax.CLOSE_BRACE);
    public static final Cel closeBracket = new Cel("]", Syntax.CLOSE_BRACKET);
    public static final Cel gap = new Cel(":", Syntax.GAP);
    public static final Cel now = new Cel("@", Syntax.NOW);
    public static final Cel access = new Cel(".", Syntax.ACCESS);

    public static final Cel empty = new Cel("()", Syntax.EMPTY);

    public static final Cel backslash = new Cel("`\\\\`", '\\');
    public static final Cel carriageReturn = new Cel("`\\\r`", '\r');
    public static final Cel newline = new Cel("`\\\n`", '\n');
    public static final Cel tab = new Cel("`\\\t`", '\t');
    public static final Cel doubleQuote = new Cel("`\"`", '"');
    public static final Cel singleQuote = new Cel("`'`", '\'');
    public static final Cel backspace = new Cel("`\\\b`", '\b');
    public static final Cel formfeed = new Cel("`\\\f`", '\f');
    public static final Cel backtick = new Cel("```", '`');
    public static final Cel nul = new Cel("`\\\0`", '\0');

    public static final Cel yes = new Cel("true", true);
    public static final Cel no = new Cel("false", false);
    public static final Cel nothing = new Cel("null", null);
    public static final Cel zeroFloat = new Cel("0.0f", 0.0f);
    public static final Cel zeroDouble = new Cel("0.0", 0.0);
    public static final Cel zeroInt = new Cel("0", 0);
    public static final Cel zeroLong = new Cel("0L", 0L);


//    public static final Cel split = new Cel(";", Syntax.SPLIT);
//    public static final Cel comma = new Cel(",", Syntax.COMMA);
//    public static final Cel evalLess = new Cel(":", Syntax.EVAL_LESS);
//    public static final Cel evalMore = new Cel("@", Syntax.EVAL_MORE);

}
