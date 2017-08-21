package lang.celadon;

import squidpony.squidmath.CrossHash;
import squidpony.squidmath.OrderedMap;

import java.util.Map;

/**
 * An OrderedMap with some section of the iteration specialized for sequential integer keys (a list) and the rest still
 * available for iteration as a whole, including any integer keys. Meant to act like Lua's tables.
 * Created by Tommy Ettinger on 8/20/2017.
 */
public class Table extends OrderedMap<Object, Object> implements ICallable {
    public Table(int expected) {
        super(expected);
    }

    public Table() {
        super();
    }

    public Table(Map<?, ?> m) {
        super(m);
    }

    public Table(Object[] keyArray, Object[] valueArray) {
        super(keyArray, valueArray);
    }

    public Table(int expected, CrossHash.IHasher hasher) {
        super(expected, hasher);
    }

    public Table(CrossHash.IHasher hasher) {
        super(hasher);
    }

    public Table(Map<?, ?> m, CrossHash.IHasher hasher) {
        super(m, hasher);
    }

    public Table(Object[] keyArray, Object[] valueArray, CrossHash.IHasher hasher) {
        super(keyArray, valueArray, hasher);
    }

    @Override
    public void call(Exchange exchange, String name) {

    }
}
