package lang.celadon;

import regexodus.MatchIterator;
import regexodus.MatchResult;
import regexodus.Matcher;
import regexodus.Pattern;
import squidpony.StringKit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * The main class that handles script execution, including how symbols are associated to values, how parameters go to
 * functions, how results are given back (possibly multiple results), and what values are yielded back to the code
 * that called the script, which is probably not Celadon code.
 * Created by Tommy Ettinger on 8/20/2017.
 */
public class Manager extends StackMap<String, Object> {
    public ArrayDeque<Object> exchange = new ArrayDeque<>(32);

    public List<Cel> tokens;

    public Manager() {
    }

    public static final Pattern pattern = Pattern.compile("({=remove}(?://|^#!)(\\V*))" + // line comment
            "|({=char}`({=contents}[^\\\\]|(?:\\\\(?:(?:[uU][0-9a-fA-F]{4})|\\V)))`)" +
            "|({=string}({=bracket}[\"'])({=contents}[\\d\\D]*?)(?<!\\\\){\\bracket})" +
            "|({=split}\\v+|;)" +
            "|({=comma},)" +
            "|({=remove}({=bracket}~+!)(?:[\\d\\D]*?){\\/bracket})" +
            "|(?:({=double}({=sign}[+-]?)(?:(?:NaN)|(?:Infinity)" +
              "|(?:({=digits}0[xX][0-9a-fA-F]+(?:\\.[0-9a-fA-F]+)?" +
                "(?:[Pp](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))))" + // hex float
              "|(?:({=digits}[0-9]+\\.[0-9]*" +
                "(?:[Ee](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))?))" + // scientific notation
            "))(?:[fmFMdD]?)\\b)" +
            "|(?:({=long}({=sign}[+-]?)" +
              "(?:(?:({=hex}0[xX])({=digits}[0-9a-fA-F]{1,16}))" +
                "|(?:({=bin}0[bB])({=digits}[01]{1,64}))" +
                "|({=digits}[0-9]+)))" +
            "(?:[lnLN]?)\\b)" +
            "|({=open}({=parenthesis}\\()|({=brace}\\{)|({=bracket}\\[))" +
            "|({=close}({=parenthesis}\\))|({=brace}\\})|({=bracket}\\]))" +
            "|({=eval}[:@])" +
            "|({=contents}\\p{Js}\\p{Jp}*)" +
            "|({=op}[!@#%\\^\\&*=+|<>/?~`\\.\\-]+)"
    );
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
                    tokens.add(Cel.closeParenthesis);
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
            else if(mr.isCaptured("split"))
            {
                tokens.add(Cel.split);
            }
            else if(mr.isCaptured("comma"))
            {
                tokens.add(Cel.comma);
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
                    tokens.add(new Cel(mr.group("long"), mr.group("sign").equals("-")
                            ? -StringKit.longFromHex(mr.group("digits"))
                            : StringKit.longFromHex(mr.group("digits"))));
                else if(mr.isCaptured("bin"))
                    tokens.add(new Cel(mr.group("long"), mr.group("sign").equals("-")
                            ? -StringKit.longFromBin(mr.group("digits"))
                            : StringKit.longFromBin(mr.group("digits"))));
                else
                {
                    try {
                        tokens.add(new Cel(mr.group("long"), Long.parseLong(mr.group("long"))));
                    }catch (NumberFormatException nfe)
                    {
                        tokens.add(new Cel(mr.group("long"), 0x7FFFFFFFFFFFFFFFL));
                    }
                }
            } else if(mr.isCaptured("double"))
            {
                try {
                    tokens.add(new Cel(mr.group("double"), Double.parseDouble(mr.group("double"))));
                }catch (NumberFormatException nfe)
                {
                    tokens.add(new Cel(mr.group("double"), Double.POSITIVE_INFINITY));
                }
            } else if(mr.isCaptured("eval"))
            {
                String s = mr.group("eval");
                tokens.add(s.equals(":") ? Cel.evalLess : Cel.evalMore);
            }
            else if(mr.isCaptured("op"))
            {
                String s = mr.group("op");
                tokens.add(new Cel(s, Syntax.OPERATOR));
            }
            else
                tokens.add(new Cel(mr.group("contents"), Syntax.SYMBOL));
        }
    }
}
