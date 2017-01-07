package lang.celadon;

import regexodus.Matcher;
import regexodus.Pattern;
import regexodus.REFlags;
import squidpony.StringKit;

/**
 * Created by Tommy Ettinger on 1/6/2017.
 */
public class Parsing {
    public static final Pattern longPattern = Pattern.compile(
            "^({=match}({=sign}[+-]?)" +
                    "(?:({=hex}0x)({=digits}[0-9a-f]{1,16}))" +
                    "|(?:({=bin}0b)({=digits}[01]{1,64}))" +
                    "|({=digits}[0-9]+))(?:[ln]?)$", REFlags.IGNORE_CASE),
    doublePattern = Pattern.compile("^({=match}({=sign}[+-]?)(?:(?:(?:NaN)|(?:Infinity))|(?:({=digits}[0-9]+(\\.[0-9]+)?)" +
            "(?:[Ee](?:[+-]?(?=[1-9]|0(?![0-9]))[0-9]+))?)))(?:[fmFM]?)$");
    public static final Matcher longMatcher = longPattern.matcher(),
            doubleMatcher = doublePattern.matcher();

    public static Double asDouble(Token t)
    {
        if(doubleMatcher.matches(t.contents))
            return Double.parseDouble(doubleMatcher.group("match"));
        return null;
    }
    public static Long asLong(Token t)
    {
        if(longMatcher.matches(t.contents))
        {
            if(longMatcher.isCaptured("hex"))
                return longMatcher.group("sign").equals("-")
                        ? -StringKit.longFromHex(longMatcher.group("digits"))
                        : StringKit.longFromHex(longMatcher.group("digits"));
            if(longMatcher.isCaptured("bin"))
                return longMatcher.group("sign").equals("-")
                        ? -StringKit.longFromBin(longMatcher.group("digits"))
                        : StringKit.longFromBin(longMatcher.group("digits"));
            else
            {
                try {
                    return Long.parseLong(longMatcher.group("match"));
                }catch (NumberFormatException nfe)
                {
                    return null;
                }
            }
        }
        return null;
    }
    public static String asString(Token t)
    {
        if(t.bracket != null && t.contents != null)
            return t.contents;
        return null;
    }
    public static Object parse(Token t)
    {
        Object r = null;
        if((r = asString(t)) != null)
            return r;
        if((r = asLong(t)) != null)
            return r;
        if((r = asDouble(t)) != null)
            return r;
        /// TODO: have this evaluate the token if it is a variable
        return null;
    }
}
