package celadon;

import org.junit.Test;

/**
 * Created by Tommy Ettinger on 1/3/2017.
 */
public class TokenizerTest {
    @Test
    public void test1()
    {
        System.out.println(Tokenizer.tokenize("(+ 1 2 3)"));
        System.out.println(Tokenizer.tokenize("(\n+\n1\n2\n3)\n"));
        System.out.println(Tokenizer.tokenize(";nothing here"));
        System.out.println(Tokenizer.tokenize("'something here';nothing here"));
        System.out.println(Tokenizer.tokenize("#modified(+ 1 2 3)"));
    }
}
