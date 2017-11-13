package lang.celadon;

/**
 * Created by Tommy Ettinger on 8/24/2017.
 */
public enum Syntax {
    OPEN_PARENTHESIS,
    CLOSE_PARENTHESIS,
    OPEN_BRACE,
    CLOSE_BRACE,
    OPEN_BRACKET,
    CLOSE_BRACKET,
    /**
     * The ':' symbol, used as the "dummy object" to pass messages to for unary operators like '-' and '!'.
     */
    UNA,
    /**
     * Any variable name that is not yet associated with a value uses this for its effective value.
     */
    SYMBOL,
}
