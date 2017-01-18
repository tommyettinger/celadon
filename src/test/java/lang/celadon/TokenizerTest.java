package lang.celadon;

import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 1/3/2017.
 */
public class TokenizerTest {
    @Test
    public void test1()
    {
        System.out.println(Token.tokenize("(+ 1 2 3)"));
        System.out.println(Token.tokenize("(\n+\n1\n2\n3)\n"));
        System.out.println(Token.tokenize(";nothing here"));
        System.out.println(Token.tokenize("'something here';nothing here"));
        System.out.println(Token.tokenize("~~/nothing here/~~'something here' ~/nothing here, either/~"));
        System.out.println(Token.tokenize("#modified(+ 1 2 3)"));

        System.out.println(Token.tokenize("#!/user/env/celadon\n" +
                "(println 'Hello, World!')\n" +
                "(println (* 2 (+ 10 11)))\n" +
                "{macro replicate [times thing] (repeat times thing)}\n" +
                "(+ {replicate 2 333})"));
        ArrayList<Token> tokens = Token.tokenize(
                "'Hello, World!' 42 (- -Infinity) true null (% (+ 222 -111 555) (* 52 (/ 5 2) 3 2))\n"+
                        "{def ten (+ 5 5)} ten\n" +
                        "[1 2 3] [ten ten (+ ten ten)] {def m 1}\n" +
                        "#map['hey' m 'you' {def m (+ m 1)} m 'go' {def m (+ m 1)} m 'to' {def m (+ m 1)} m 'ten' {def m ten} m]"
        );
        Context context = new Context();
        System.out.println(context.evaluate(tokens));
    }
}
