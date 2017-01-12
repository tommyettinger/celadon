package lang.celadon;

import squidpony.squidmath.OrderedMap;

import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 1/11/2017.
 */
public class StackMapTest {
    public static void main(String[] args)
    {
        boolean printNormal = false, printUnusual = true;

        OrderedMap<String, String> normal;
        StackMap<String, String> unusual;
        normal = new OrderedMap<>(8, 0.5f);
        unusual = new StackMap<>(8, 0.5f);

        String[] keysMixed = {"alpha", "alpha", "alpha", "alpha", "beta", "alpha", "beta", "beta", "gamma", "gamma", "beta", "alpha"},
                keysSame = {"alpha", "alpha", "alpha", "alpha", "alpha", "alpha", "alpha", "alpha", "alpha", "alpha", "alpha", "alpha"},
                keys = keysMixed,
                vals = {"foo", "bar", "baz", "quo", "lim", "per", "ziv", "wel", "fus", "roh", "dah", "aaa"},
                pr = {"PUT ", "REM "};


        int[] instructions = {0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0};
        if(printNormal)
            System.out.println("normal ( ): " + normal);
        if(printUnusual)
            System.out.println("unusual ( ): " + unusual);

        System.out.println(Integer.toBinaryString("alpha".hashCode()));

        for (int i = instructions.length; i <= instructions.length; i++) {
            normal.clear();
            unusual.clear();
            for (int j = 0; j < i; j++) {

                if (instructions[j] == 0) {
                    normal.put(keys[j], vals[j]);
                    unusual.put(keys[j], vals[j]);
                } else {
                    normal.remove(keys[j]);
                    unusual.remove(keys[j]);
                }

                if (printNormal) {
                    System.out.print("normal (");
                    for (int k = 0; k <= j; k++) {
                        System.out.print(pr[instructions[k]]);
                    }
                    System.out.println("): " + normal);

                    System.out.println("normal[0] is " + normal.getAt(0));
                    System.out.println("normal[1] is " + normal.getAt(1));
                    System.out.println("normal[2] is " + normal.getAt(2));
                    System.out.println("normal[3] is " + normal.getAt(3));
                    System.out.println("normal[4] is " + normal.getAt(4));
                    System.out.println("normal[5] is " + normal.getAt(5));
                    System.out.println("normal[6] is " + normal.getAt(6));
                    System.out.println("normal[7] is " + normal.getAt(7));
                    System.out.println("normal[8] is " + normal.getAt(8));

                    System.out.println("normal['alpha'] is " + normal.get("alpha"));
                    System.out.println("normal['beta'] is " + normal.get("beta"));
                    System.out.println("normal['gamma'] is " + normal.get("gamma"));
                }
                if (printUnusual) {
                    System.out.print("unusual (");
                    for (int k = 0; k <= j; k++) {
                        System.out.print(pr[instructions[k]]);
                    }
                    System.out.println("): " + unusual);

                    System.out.println("unusual[0] is " + unusual.getAt(0));
                    System.out.println("unusual[1] is " + unusual.getAt(1));
                    System.out.println("unusual[2] is " + unusual.getAt(2));
                    System.out.println("unusual[3] is " + unusual.getAt(3));
                    System.out.println("unusual[4] is " + unusual.getAt(4));
                    System.out.println("unusual[5] is " + unusual.getAt(5));
                    System.out.println("unusual[6] is " + unusual.getAt(6));
                    System.out.println("unusual[7] is " + unusual.getAt(7));
                    System.out.println("unusual[8] is " + unusual.getAt(8));

                    System.out.println("unusual['alpha'] is " + unusual.get("alpha"));
                    System.out.println("unusual['beta'] is " + unusual.get("beta"));
                    System.out.println("unusual['gamma'] is " + unusual.get("gamma"));
                }
            }
        }
        
        
        /*
        
        normal.remove("alpha");
        unusual.remove("alpha");

        System.out.println("normal (put 2 remove 1): " + normal);
        System.out.println("unusual (put 2 remove 1): " + unusual);

        System.out.println("normal[0] is " + normal.getAt(0));
        System.out.println("normal[1] is " + normal.getAt(1));

        System.out.println("normal['alpha'] is " + normal.get("alpha"));

        System.out.println("unusual[0] is " + unusual.getAt(0));
        System.out.println("unusual[1] is " + unusual.getAt(1));

        System.out.println("unusual['alpha'] is " + unusual.get("alpha"));

        normal.put("alpha", "baz");
        unusual.put("alpha", "baz");

        System.out.println("normal (put 2 remove 1 put 1): " + normal);
        System.out.println("unusual (put 2 remove 1 put 1): " + unusual);

        System.out.println("normal[0] is " + normal.getAt(0));
        System.out.println("normal[1] is " + normal.getAt(1));

        System.out.println("normal['alpha'] is " + normal.get("alpha"));

        System.out.println("unusual[0] is " + unusual.getAt(0));
        System.out.println("unusual[1] is " + unusual.getAt(1));

        System.out.println("unusual['alpha'] is " + unusual.get("alpha"));

         */
        
    }
}
