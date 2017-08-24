package lang.celadon;

import org.junit.Test;

import java.util.ListIterator;

/**
 * Created by Tommy Ettinger on 1/3/2017.
 */
public class TableTest {
    @Test
    public void test1()
    {
        Table t = new Table(16);
        t.put("hello", "world");
        t.put(0, 42);
        t.put(1, 421);
        t.put(10, "not originally part of seq");
        System.out.println(t.toString() + "\n");
        ListIterator<Integer> itKey = t.seqKeyIterator();
        ListIterator<Object> itVal  = t.seqValueIterator();
        while (itKey.hasNext())
        {
            Integer i = itKey.next();
            System.out.println(i + ", " + itVal.next());
        }
        System.out.println();
        t.remove(1);
        itKey = t.seqKeyIterator();
        itVal = t.seqValueIterator();
        while (itKey.hasNext())
        {
            Integer i = itKey.next();
            System.out.println(i + ", " + itVal.next());
        }
        System.out.println();
        t.alter(10, 1);
        t.put(2, "yay");
        itKey = t.seqKeyIterator();
        itVal = t.seqValueIterator();
        while (itKey.hasNext())
        {
            Integer i = itKey.next();
            System.out.println(i + ", " + itVal.next());
        }
    }
}
