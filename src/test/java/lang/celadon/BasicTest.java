package lang.celadon;

import org.junit.Test;
import squidpony.StringKit;

/**
 * Created by Tommy Ettinger on 1/3/2017.
 */
public class BasicTest {
    @Test
    public void testTokenizer() {
        Manager m = new Manager();
        //m.tokenize("[1, 0x2, 3.141592, alpha, 'hello, world!', 0x1.Ep1, 0xF.0p-2, `!`] (10)");

        /*
        m.tokenize("[1, 0x2, 3.141592, alpha, 'hello, world!', 0x1.Ep1, ##0xF.0p-2, `!`]" +
                "\n(10 ~!block comment!~) ; this is also a comment\n null () ( ) :pause @:+");
        System.out.println(StringKit.join(", ", m.tokens));

        for(Cel cel : m.tokens)
        {
            System.out.println(cel.title + " : " + cel.ref);
        }
        */

        m.tokenize("4 + (8 * 5 - 7) / 3");
        m.shunt();
        System.out.println(StringKit.join(", ", m.items));

    }
//    @Test
//    public void testOld()
//    {
//
//        System.out.println(Token.tokenize("(+ 1 2 3)"));
//        System.out.println(Token.tokenize("(\n+\n1\n2\n3)\n"));
//        System.out.println(Token.tokenize(";nothing here"));
//        System.out.println(Token.tokenize("'something here';nothing here"));
//        System.out.println(Token.tokenize("~~/nothing here/~~'something here' ~/nothing here, either/~"));
//        System.out.println(Token.tokenize("#modified(+ 1 2 3)"));
//
//        System.out.println(Token.tokenize("#!/user/env/celadon\n" +
//                "(println 'Hello, World!')\n" +
//                "(println (* 2 (+ 10 11)))\n" +
//                "{defmacro duplicate [thing] [thing thing]}\n" +
//                "(+ {duplicate 333})"));
//        TList tokens = Token.tokenize(
//                "'Hello, World!'\n" +
//                        "~~/" +
//                        "'Hello, World!' 42 (- -Infinity) true null (% (+ 222 -111 555) (* 52 (/ 5 2) 3 2))\n"+
//                        "{= ten (+ 5 5)}\n" +
//                        "[1 2 3] [ten ten (+ ten ten)] {def m 1}\n" +
//                        "#map['hey' m 'you' {= m (+ m 1)} 'go' {def m (+ m 1)} m 'to' {= m (+ m 1)} 'ten' {= m ten}]\n" +
//                        "{if false 10 (- 20 40)}\n" +
//                        "(>= ten 1.5 1 -Infinity)\n" +
//                        "{or false false} {or true false} {or false true} {or true true}\n" +
//                        "{and false false} {and true false} {and false true} {and true true}\n" +
//                        "{or false false} {or 31337 false} {or false 31337} {or 31337 0xBEEF}\n" +
//                        "{and false false} {and 31337 false} {and false 31337} {and 31337 0xBEEF}\n" +
//                        "({fn [a b] (+ a b)} 10 32)\n" +
//                        "{def add {fn [a b] (+ a b)}} (add 222 444)\n" +
//                        "{defn add [a b] (add a b)} (add 111 222)\n" +
//                        "{defmacro dup [a] [a a]} (+ {dup 11} {dup 10}) {dup 23}\n" +
//                        "{defmacro plus [] [+]} ({plus} 11 22) {plus} plus\n" +
//                        "{def n 40}\n" +
//                        "{while (> 50 {++ n}) n}\n" +
//                        "[] [1] [1 2] [1 1]\n" +
//                        "; #set[] #set[1] #set[1 2] #set[1 1] #set[NaN (+ Infinity (/ -0.0 -0.0))]\n" +
//                        "{defn sip [a] a}\n" +
//                        "{defn gulp [] []}\n" +
//                        "{defn list [] ...}\n" +
//                        "{defmacro spray [coll] coll}\n" +
//                        "{def n 40}\n" +
//                        "(sip {while (> 45 {++ n}) n})\n" +
//                        "{def n 40}\n" +
//                        "(gulp {while (> 45 {++ n}) n})\n" +
//                        "{def n 40}\n" +
//                        "(list {while (> 45 {++ n}) n})\n" +
//                        "{def n 40}\n" +
//                        "#(spray [{while (> 45 {++ n}) n}])\n" +
//                        "{def s #set[1 2 3 5 8]}\n" +
//                        "#0(s:add 13)\n" +
//                        "s\n" +
//                        "/~~\n" +
//                        "(@:chaos:setState 999)\n" +
//                        //"#0(chaos:setState 99)\n" +
//                        "{mutant 1d20 (chaos:between 1 21)}\n" +
//                        "[1d20 1d20] [1d20 1d20]\n" +
//                        "{defn 2d20 [] (+ 1d20 1d20)}\n" +
//                        "#0(chaos:setState 999)\n" +
//                        "(2d20) (2d20)\n" +
//                        "{defmacro dice [name amount sides] {mutant @name (+ {repeat amount (chaos:between 1 (+ 1 sides))})} []}\n" +
//                        "{dice 7d7 7 7} 7d7 7d7 7d7\n"
//        );
//        Context context = new Context();
//        System.out.println(context.evaluate(tokens));
//    }
}
