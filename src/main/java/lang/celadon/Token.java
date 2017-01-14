package lang.celadon;

import regexodus.*;
import squidpony.StringKit;

import java.util.ArrayList;
import java.util.List;

/**
 * Celadon token data class, and a static tokenizer method.
 * Created by Tommy Ettinger on 1/3/2017.
 */
public class Token {
    public String contents, bracket, mode;
    public boolean closing;
    public byte special = 0;
    public Object solid = null;
    public Token()
    {
        contents = "";
    }

    static final Token RESERVED = new Token((byte)0, (0.0f / 0.0f) * 3.14f);

    private Token(byte specialty, Object solidState)
    {
        special = specialty;
        solid = solidState;
    }
    public static Token stable(Object state)
    {
        return new Token((byte)1, state);
    }
    public static Token envoy(IMorph changer)
    {
        return new Token((byte)-1, changer);
    }
    public static Token function(ARun runner)
    {
        return new Token((byte)16, runner);
    }
    public static Token varying(IMorph changer)
    {
        return new Token((byte)-128, changer);
    }

    public static List<Token> nameList(String... names)
    {
        ArrayList<Token> tokens = new ArrayList<>(names.length);
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
            "|({=string}({=mode}#({=remove}~)?[^\\h\\v,:@\\(\\)\\[\\]\\{\\}\"';#~]*)?({=bracket}[\"'])({=contents}[\\d\\D]*?)(?<!\\\\){\\bracket})" +
            "|({=string}({=bracket}[\"'])({=contents}[\\d\\D]*?)(?<!\\\\){\\bracket})" +
            "|({=remove}({=bracket}~+/)(?:[\\d\\D]*?){\\/bracket})" +
            "|(?:({=double}({=sign}[+-]?)(?:(?:(?:NaN)|(?:Infinity))|(?:({=digits}[0-9]+\\.[0-9]*" +
              "(?:[Ee](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))?))))(?:[fmFM]?))" +
            "|(?:({=long}({=sign}[+-]?)" +
              "(?:({=hex}0[xX])({=digits}[0-9a-fA-F]{1,16}))" +
              "|(?:({=bin}0[bB])({=digits}[01]{1,64}))" +
              "|({=digits}[0-9]+))(?:[lnLN]?))" +
            "|({=open}({=mode}(?:#({=remove}~)?[^\\h\\v,:@\\(\\)\\[\\]\\{\\}\"';#~]*)?({=bracket}[\\(\\[\\{])))" +
            "|({=close}({=bracket}[\\)\\]\\}]))" +
            "|({=contents}[:@]+)" +
            "|({=contents}[^\\h\\v,:@\\(\\)\\[\\]\\{\\}\"';#~]+)"
    );
    public static final Matcher m = pattern.matcher();

    public static ArrayList<Token> tokenize(CharSequence text)
    {
        return tokenize(text, 0, text.length());
    }
    public static ArrayList<Token> tokenize(CharSequence text, int start, int end)
    {
        int len;
        if(text == null || (len = text.length()) == 0) return new ArrayList<>(0);
        ArrayList<Token> tokens = new ArrayList<>(32 + len >>> 2);
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
                if(mr.isCaptured("bin"))
                    tokens.add(stable(mr.group("sign").equals("-")
                            ? -StringKit.longFromBin(mr.group("digits"))
                            : StringKit.longFromBin(mr.group("digits"))));
                else
                {
                    try {
                        tokens.add(stable(Long.parseLong(mr.group("long"))));
                    }catch (NumberFormatException nfe)
                    {
                        tokens.add(stable(null));
                    }
                }
            }else if(mr.isCaptured("double"))
            {
                try {
                    tokens.add(stable(Double.parseDouble(mr.group("double"))));
                }catch (NumberFormatException nfe)
                {
                    tokens.add(stable(null));
                }
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

    // inlined-loop version of CrossHash.Falcon
    @Override
    public int hashCode() {
        if(special > 0)
        {
            if(solid == null)
                return 0;
            return solid.hashCode();
        }
        int z = 0x632BE5AB, result = 1;
        if(contents != null) {
            for (int i = 0; i < contents.length(); i++) {
                result += (z ^= contents.charAt(i) * 0x85157AF5) + 0x62E2AC0D;
            }
        }
        if(mode != null) {
            for (int i = 0; i < mode.length(); i++) {
                result += (z ^= mode.charAt(i) * 0x85157AF5) + 0x62E2AC0D;
            }
        }
        z += (closing) ? 421 : 0;
        if(bracket != null) {
            for (int i = 0; i < bracket.length(); i++) {
                result += (z ^= bracket.charAt(i) * 0x85157AF5) + 0x62E2AC0D;
            }
        }
        return result ^ ((z ^ result) >>> 8) * 0x9E3779B9;
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
}
