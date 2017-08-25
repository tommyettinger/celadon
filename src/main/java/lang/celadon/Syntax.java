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
     * Similar to quote in Lisps, this blocks the evaluation of a parameter until requested.
     */
    EVAL_LESS,
    /**
     * Similar to unquote in Lisps, this evaluates an otherwise-unevaluated symbol.
     */
    EVAL_MORE,
    /**
     * When this shows up, it usually means the specific operator is stored elsewhere, such as in the title of a Cel.
     */
    OPERATOR,
    /**
     * Any variable name that is not yet associated with a value uses this for its effective value.
     */
    SYMBOL,
    /**
     * Can be either a line break or an explicit semicolon.
     */
    SPLIT,
    /**
     * A literal comma, used to separate various kinds of list items and during multiple assignment/return.
     */
    COMMA,
}
