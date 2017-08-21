package lang.celadon;

import java.util.ArrayDeque;

/**
 * Meant to be used to pass parameters to functions and get back results, as well as to handle the inputs and results
 * of the whole script/program when it is embedded in a larger application.
 * Created by Tommy Ettinger on 8/20/2017.
 */
public class Exchange {
    public ArrayDeque<?> items = new ArrayDeque<>(32);

    public Exchange() {
    }
}
