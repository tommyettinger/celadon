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
     * The '.' symbol, used to access fields and methods of objects.
     */
    ACCESS,
    /**
     * The ':' symbol, used to cause a delay after reading, causing a gap in evaluation. Similar to Lisp quotation.
     */
    GAP,
    /**
     * The '@' symbol, used to read a delayed-evaluation term now. Similar to unquote in Lisp.
     */
    NOW,
    /**
     * Any variable name that is not yet associated with a value uses this for its effective value.
     */
    SYMBOL,
    /**
     * The special case for '()'; since parentheses return exactly one value, and empty parentheses wouldn't normally
     * return a value, here they return EMPTY. There is exactly one Cel referring to any occurrence of empty
     * parentheses, since unlike empty square brackets (referring to a 0-size list) or empty curly braces (referring to
     * a 0-size map), the empty parentheses aren't an object that could be mutated later.
     */
    EMPTY,
}
