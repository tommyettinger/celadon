package lang.celadon;

import squidpony.squidmath.OrderedMap;

/**
 * Created by Tommy Ettinger on 1/11/2017.
 */
public class StackMapTest {
    public static void main(String[] args)
    {
        OrderedMap<String, String> normal;
        StackMap<String, String> unusual;
        normal = new OrderedMap<>(16, 0.5f);
        unusual = new StackMap<>(16, 0.5f);

        System.out.println("normal (put 0): " + normal);
        System.out.println("unusual (put 0): " + unusual);

        normal.put("alpha", "foo");
        unusual.put("alpha", "foo");

        System.out.println("normal (put 1): " + normal);
        System.out.println("unusual (put 1): " + unusual);

        normal.put("alpha", "bar");
        unusual.put("alpha", "bar");

        System.out.println("normal (put 2): " + normal);
        System.out.println("unusual (put 2): " + unusual);

        System.out.println("normal[0] is " + normal.getAt(0));
        System.out.println("normal[1] is " + normal.getAt(1));

        System.out.println("normal['alpha'] is " + normal.get("alpha"));

        System.out.println("unusual[0] is " + unusual.getAt(0));
        System.out.println("unusual[1] is " + unusual.getAt(1));

        System.out.println("unusual['alpha'] is " + unusual.get("alpha"));

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



    }
}
