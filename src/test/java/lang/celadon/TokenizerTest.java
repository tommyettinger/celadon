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
                "~~/" +
                        "'Hello, World!' 42 (- -Infinity) true null (% (+ 222 -111 555) (* 52 (/ 5 2) 3 2))\n"+
                        "{= ten (+ 5 5)}\n" +
                        "[1 2 3] [ten ten (+ ten ten)] {def m 1}\n" +
                        "#map['hey' m 'you' {= m (+ m 1)} 'go' {def m (+ m 1)} m 'to' {= m (+ m 1)} 'ten' {= m ten}]\n" +
                        "{if false 10 (- 20 40)}\n" +
                        "(>= ten 1.5 1 -Infinity)" +
                        "/~~" +
                        "{or false false} {or true false} {or false true} {or true true}" +
                        "{and false false} {and true false} {and false true} {and true true}" + 
                        "{or false false} {or 31337 false} {or false 31337} {or 31337 0xBEEF}" +
                        "{and false false} {and 31337 false} {and false 31337} {and 31337 0xBEEF}"
        );
        Context context = new Context();
        System.out.println(context.evaluate(tokens));
    }
}
