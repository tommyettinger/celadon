package celadon;

import regexodus.Category;

/**
 * Created by Tommy Ettinger on 1/3/2017.
 */
public class Token {
    public String contents, bracket, mode;
    public boolean closing;
    public Token()
    {
        contents = "";
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

    public boolean bracketsMatch(Token other) {
        if (bracket == null || other == null || other.bracket == null) return false;
        if(closing == other.closing) return false;
        if (bracket.length() != other.bracket.length()) return false;
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

    @Override
    public int hashCode() {
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
        if(bracket != null)
        {
            if(contents != null)
                return (mode != null)
                        ? "#" + mode + bracket + contents + bracket
                        : bracket + contents + bracket;
            return (mode != null)
                    ? "#" + mode + bracket
                    : bracket;
        }
        if(contents == null) return "null";
        return contents;
    }
}
