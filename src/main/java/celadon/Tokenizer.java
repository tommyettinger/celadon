package celadon;

import regexodus.MatchIterator;
import regexodus.MatchResult;
import regexodus.Pattern;

import java.util.ArrayList;

/**
 * Tokenizes Celadon source to form an ArrayList of Token values.
 * Created by Tommy Ettinger on 1/3/2017.
 */
public class Tokenizer {
    protected static final Pattern pattern = Pattern.compile("({=remove};(\\V*))" +
            "|(?:(?:#({=remove}~)?({=mode}[^\\h\\v,.\\(\\)\\[\\]\\{\\}\"';#~]+)?)?({=bracket}[\"'])({=contents}[\\d\\D]*?)(?<!\\\\){\\bracket})" +
            "|({=remove}({=bracket}(?:~+)/)(?:[\\d\\D]*?){\\/bracket})" +
            "|({=open}(?:#({=remove}~)?({=mode}[^\\h\\v,.\\(\\)\\[\\]\\{\\}\"';#~]+)?)?({=bracket}[\\(\\[\\{]))" +
            "|({=close}({=bracket}[\\)\\]\\}]))" +
            "|({=contents}\\.+)" +
            "|({=contents}[^\\h\\v,.\\(\\)\\[\\]\\{\\}\"';#~]+)"
            );
    public static ArrayList<Token> tokenize(CharSequence text)
    {
        int len;
        if(text == null || (len = text.length()) == 0) return new ArrayList<>(0);
        ArrayList<Token> tokens = new ArrayList<>(32 + len >>> 2);
        MatchIterator mi = pattern.matcher(text).findAll();
        MatchResult mr;
        while (mi.hasNext())
        {
            mr = mi.next();
            if(mr.isCaptured("remove"))
                continue;
            if(mr.isCaptured("close"))
                tokens.add(new Token(mr.group("contents"), mr.group("bracket"), true, mr.group("mode")));
            else
                tokens.add(new Token(mr.group("contents"), mr.group("bracket"), false, mr.group("mode")));
        }
        return tokens;
    }
}
