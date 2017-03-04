package lang.celadon;

import regexodus.*;
import squidpony.StringKit;

import java.io.Serializable;

/**
 * Celadon token data class, and a static tokenizer method.
 * Created by Tommy Ettinger on 1/3/2017.
 */
public class Token implements Serializable {
    private static final long serialVersionUID = 0;
    public String contents, bracket, mode;
    public boolean closing;
    public int special = 0;
    public Object solid = null;
    public Token()
    {
        contents = "";
    }

    static final Token RESERVED = new Token(0, (0.0f / 0.0f) * 3.14f);

    Token(int specialty, Object solidState)
    {
        special = specialty;
        solid = solidState;
    }
    public static Token stable(Object state)
    {
        return new Token(1, state);
    }
    public static Token envoy(IMorph changer)
    {
        return new Token(-1, changer);
    }
    public static Token function(ARun runner)
    {
        return new Token(16, runner);
    }
    public static Token macro(IMorph runner)
    {
        return new Token(8, runner);
    }
    public static Token varying(IMorph changer)
    {
        return new Token(-128, changer);
    }
    public static Token quoter(IMorph changer)
    {
        return new Token(-127, changer);
    }

    public static TList nameList(String... names)
    {
        TList tokens = new TList(names.length);
        for (int i = 0; i < names.length; i++) {
            tokens.add(new Token(names[i]));
        }
        return tokens;
    }

    public Token(String contents)
    {
        this.contents = contents;
    }
    public Token(String contents, String bracket, boolean isClosingBracket)
    {
        this.contents = contents;
        this.bracket = bracket;
        closing = isClosingBracket;
    }
    public Token(String contents, String bracket, boolean isClosingBracket, String mode)
    {
        this.contents = contents;
        this.bracket = bracket;
        this.mode = mode;
        closing = isClosingBracket;
    }

    public static final Pattern pattern = Pattern.compile("({=remove}(?:;|#!)(\\V*))" +
            "|({=string}({=mode}#({=remove}~)?[^\\s,\\\\:@`\\(\\)\\[\\]\\{\\}\"';#~]*)?({=bracket}[\"'])({=contents}[\\d\\D]*?)(?<!\\\\){\\bracket})" +
            "|({=string}({=bracket}[\"'])({=contents}[\\d\\D]*?)(?<!\\\\){\\bracket})" +
            "|({=remove}({=bracket}~+[%~+=*_\\$\\?\\|-]*/)(?:[\\d\\D]*?){\\/bracket})" +
            "|(?:({=double}({=sign}[+-]?)(?:(?:(?:NaN)|(?:Infinity))|(?:({=digits}[0-9]+\\.[0-9]*" +
              "(?:[Ee](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))?))))(?:[fmFM]?)(?![^\\s,\\(\\)\\[\\]\\{\\}\"';#~]))" +
            "|(?:({=long}({=sign}[+-]?)" +
              "(?:(?:({=hex}0[xX])({=digits}[0-9a-fA-F]{1,16}))" +
              "|(?:({=bin}0[bB])({=digits}[01]{1,64}))" +
              "|({=digits}[0-9]+)))(?:[lnLN]?)(?![^\\s,\\(\\)\\[\\]\\{\\}\"';#~]))" +
            "|({=open}({=mode}(?:#({=remove}~)?[^\\s,\\\\:@`\\(\\)\\[\\]\\{\\}\"';#~]*)?({=bracket}[\\(\\[\\{])))" +
            "|({=close}({=bracket}[\\)\\]\\}]))" +
            "|({=eval}[:@])" +
            "|({=contents}[^\\s,\\\\:@`\\(\\)\\[\\]\\{\\}\"';#~]+)"
    );
    public static final Matcher m = pattern.matcher();

    public static TList tokenize(CharSequence text)
    {
        return tokenize(text, 0, text.length());
    }
    public static TList tokenize(CharSequence text, int start, int end)
    {
        int len;
        if(text == null || (len = text.length()) == 0) return new TList(0);
        TList tokens = new TList(32 + len >>> 2);
        m.setTarget(text, start, end);
        MatchIterator mi = m.findAll();
        MatchResult mr;
        while (mi.hasNext())
        {
            mr = mi.next();
            if(mr.isCaptured("remove"))
                continue;
            if(mr.isCaptured("close"))
                tokens.add(new Token(null, mr.group("bracket"), true, null));
            else if(mr.isCaptured("open"))
                tokens.add(new Token(null, mr.group("bracket"), false, mr.group("mode")));
            else if(mr.isCaptured("string"))
            {
                if(mr.isCaptured("mode"))
                    tokens.add(new Token(mr.group("contents"), "'", false, mr.group("mode")));
                else
                    tokens.add(stable(mr.group("contents")));
            }
            else if(mr.isCaptured("long"))
            {
                if(mr.isCaptured("hex"))
                    tokens.add(stable(mr.group("sign").equals("-")
                            ? -StringKit.longFromHex(mr.group("digits"))
                            : StringKit.longFromHex(mr.group("digits"))));
                else if(mr.isCaptured("bin"))
                    tokens.add(stable(mr.group("sign").equals("-")
                            ? -StringKit.longFromBin(mr.group("digits"))
                            : StringKit.longFromBin(mr.group("digits"))));
                else
                {
                    try {
                        tokens.add(stable(Long.parseLong(mr.group("long"))));
                    }catch (NumberFormatException nfe)
                    {
                        tokens.add(stable(0x7FFFFFFFFFFFFFFFL));
                    }
                }
            } else if(mr.isCaptured("double"))
            {
                try {
                    tokens.add(stable(Double.parseDouble(mr.group("double"))));
                }catch (NumberFormatException nfe)
                {
                    tokens.add(stable(Double.POSITIVE_INFINITY));
                }
            } else if(mr.isCaptured("eval"))
            {
                String e = mr.group("eval");
                if(e.equals(":"))
                    tokens.add(Context.quote);
                //else if(e.equals("@")
                    //tokens.add(Context.unquote);
                else
                    tokens.add(new Token(mr.group("eval")));
            }
            else
                tokens.add(new Token(mr.group("contents"), null, false, mr.group("mode")));
        }
        return tokens;
    }

    public boolean bracketsMatch(Token other) {
        if (bracket == null || other == null || other.bracket == null
                || closing == other.closing
                || bracket.length() != other.bracket.length()) return false;
        for (int l = 0, r = bracket.length() - 1; r >= 0; r--, l++) {
            if (bracket.charAt(l) != Category.matchBracket(other.bracket.charAt(r))) return false;
        }
        return true;
    }

    public boolean numeric()
    {
        return solid != null && solid instanceof Number;
    }
    public boolean floating()
    {
        return solid != null && (solid instanceof Double || solid instanceof Float);
    }
    public double asDouble()
    {
        return ((Number)solid).doubleValue();
    }
    public int asInt()
    {
        if(solid != null) {
            if (solid instanceof Number)
                return ((Number) solid).intValue();
            else if (solid instanceof Boolean)
                return ((Boolean) solid) ? 1 : 0;
        }
        throw new UnsupportedOperationException("Tried to get an int value from an incompatible Token (not a number or a boolean)");
    }
    public long asLong()
    {
        if(solid != null) {
            if (solid instanceof Number)
                return ((Number) solid).longValue();
            else if (solid instanceof Boolean)
                return ((Boolean) solid) ? 1L : 0L;
        }
        throw new UnsupportedOperationException("Tried to get a long value from an incompatible Token (not a number or a boolean)");
    }
    public String asString()
    {
        return (solid != null && solid instanceof String) ? (String)solid : (contents != null) ? contents : null;
    }
    public boolean asBoolean()
    {
        return solid != null && !solid.equals(Boolean.FALSE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        if (closing != token.closing) return false;
        if (contents != null ? !contents.equals(token.contents) : token.contents != null) return false;
        if (bracket != null ? !bracket.equals(token.bracket) : token.bracket != null) return false;
        return mode != null ? mode.equals(token.mode) : token.mode == null;
    }

    // inlined-loop version of CrossHash.Wisp
    @Override
    public int hashCode() {
        if(special > 0)
        {
            if(solid == null)
                return 0;
            return solid.hashCode();
        }
        int result = 0x9E3779B9, a = 0x632BE5AB, len;
        if(contents != null) {
            len = contents.length();
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * contents.charAt(i));
            }
            result = result * (a | 1) ^ (result >>> 11 | result << 21);
        }
        if(mode != null) {
            len = mode.length();
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * mode.charAt(i));
            }
            result = result * (a | 1) ^ (result >>> 11 | result << 21);
        }
        result += (closing) ? 421 : 0;
        if(bracket != null) {
            len = bracket.length();
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * bracket.charAt(i));
            }
            result = result * (a | 1) ^ (result >>> 11 | result << 21);
        }
        return result;
    }

    @Override
    public String toString() {
        if(special > 0)
        {
            if(solid == null)
                return "null";
            return solid.toString();
        }
        if(bracket != null)
        {
            if(contents != null)
                return (mode != null)
                        ? mode + bracket + contents + bracket
                        : bracket + contents + bracket;
            return (mode != null)
                    ? mode
                    : bracket;
        }
        if(contents == null) return "null";
        return contents;
    }

    public boolean numericallyEqual(Token token) {
        if(solid == null || token.solid == null)
            return false;
        if(!(solid instanceof Number && token.solid instanceof Number))
            return false;
        if((solid instanceof Double || solid instanceof Float)
                || (token.solid instanceof Double || token.solid instanceof Float))
            return asDouble() == token.asDouble();
        return asLong() == token.asLong();
    }

    @SuppressWarnings("unchecked")
    public boolean lt(Token token) {
        if(solid == null || token.solid == null)
            return false;
        if(!(solid instanceof Number && token.solid instanceof Number)) {
            return solid instanceof Comparable && token.solid instanceof Comparable
                    && ((Comparable) solid).compareTo(token.solid) < 0;
        }
        return (floating() ? asDouble() : asLong()) < (token.floating() ? token.asDouble() : token.asLong());
    }
    @SuppressWarnings("unchecked")
    public boolean gt(Token token) {
        if(solid == null || token.solid == null)
            return false;
        if(!(solid instanceof Number && token.solid instanceof Number)) {
            return solid instanceof Comparable && token.solid instanceof Comparable
                    && ((Comparable) solid).compareTo(token.solid) > 0;
        }
        return (floating() ? asDouble() : asLong()) > (token.floating() ? token.asDouble() : token.asLong());
    }
    @SuppressWarnings("unchecked")
    public boolean lte(Token token) {
        if(solid == null || token.solid == null)
            return false;
        if(!(solid instanceof Number && token.solid instanceof Number)) {
            return solid instanceof Comparable && token.solid instanceof Comparable
                    && ((Comparable) solid).compareTo(token.solid) <= 0;
        }
        return (floating() ? asDouble() : asLong()) <= (token.floating() ? token.asDouble() : token.asLong());
    }
    @SuppressWarnings("unchecked")
    public boolean gte(Token token) {
        if(solid == null || token.solid == null)
            return false;
        if(!(solid instanceof Number && token.solid instanceof Number)) {
            return solid instanceof Comparable && token.solid instanceof Comparable
                    && ((Comparable) solid).compareTo(token.solid) >= 0;
        }
        return (floating() ? asDouble() : asLong()) >= (token.floating() ? token.asDouble() : token.asLong());
    }
}
