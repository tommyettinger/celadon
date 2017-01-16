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

        System.out.println(Token.tokenize("#!/user/env/lang.celadon\n" +
                "{println 'Hello, World!'}\n" +
                "{println (* 2 (+ 10 11))}\n" +
                "(macro repeat [thing] [thing thing])\n" +
                "(+ {repeat 2 333})"));
        ArrayList<Token> tokens = Token.tokenize(
                "'Hello, World!' 42 (- -Infinity) true null (% (+ 222 -111 555) (* 52 (/ 5 2) 3 2))");
        Context context = new Context();
        System.out.println(context.evaluate(tokens));
    }
}
