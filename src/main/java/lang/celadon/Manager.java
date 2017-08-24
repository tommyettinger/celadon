package lang.celadon;

import java.util.ArrayDeque;

/**
 * The main class that handles script execution, including how symbols are associated to values, how parameters go to
 * functions, how results are given back (possibly multiple results), and what values are yielded back to the code
 * that called the script, which is probably not Celadon code.
 * Created by Tommy Ettinger on 8/20/2017.
 */
public class Manager extends StackMap<String, Object> {
    public ArrayDeque<?> exchange = new ArrayDeque<>(32);

    public Manager() {
    }
}
