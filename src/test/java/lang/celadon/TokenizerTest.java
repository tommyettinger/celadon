package lang.celadon;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

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
                "(macro replicate [thing] [thing thing])\n" +
                "(+ {replicate 333})"));
        ArrayList<Token> tokens = Token.tokenize("'Hello, World!' 42 3.14");
        Context context = new Context();
        System.out.println(context.parse(Arrays.asList(tokens.get(0), tokens.get(1), tokens.get(2))));
    }
}
