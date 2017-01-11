package lang.celadon;

import java.util.List;

/**
 * Created by Tommy Ettinger on 1/7/2017.
 */
public interface IMorph {
    /**
     * Changes the parameter tokens, within the range of start (inclusive) to end (exclusive). This must modify tokens
     * to avoid an "infinite loop" (which will be caught by the sandbox), and this normally means removing all tokens in
     * the start-to-end range and filling 0 or more tokens back in their place. The return value should be the number of
     * tokens "refilled" into tokens after removal of the start-to-end range.
     * <br>
     * For example, an IMorph might cause some side effect when evaluated and then remove the Tokens that produced it.
     * This IMorph would remove all Tokens from start until end (exclusive on end), and insert zero tokens after causing
     * its effects, so it returns 0. Another might be an alias that replaces some range of tokens with one or more other
     * tokens, like code that turns infix into prefix notation; this might take code like {@code #infix(3 * 5 + 8)} and
     * produce {@code (+ (* 3 5) 8)}. The input here is 7 tokens (the opening bracket with modifier, three numbers, two
     * functions, and the closing bracket), and the output is 9 tokens (it includes an extra pair of brackets; the
     * missing modifier doesn't affect the count). Because this particular case produces 9 tokens, the morph method
     * should return 9 here, but for cases where it produces more tokens, it should produce larger return values, too.
     * @param context the Context used to look up or assign values for names; may be modified
     * @param tokens a List of Token that must be modified by this method, almost always removing the tokens that
     *                caused this IMorph to evaluate
     * @param start the starting index in tokens (inclusive) to read from, typically removing from start until end
     * @param end the ending index in tokens (exclusive) to read until, typically removing from start until end
     * @return the number of tokens
     */
    int morph(Context context, List<Token> tokens, int start, int end);
}
