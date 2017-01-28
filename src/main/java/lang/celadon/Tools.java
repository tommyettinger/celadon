package lang.celadon;

import squidpony.squidmath.CrossHash;

import java.util.Objects;

/**
 * Created by Tommy Ettinger on 1/13/2017.
 */
public class Tools {
    public static class WispStringHasher implements CrossHash.IHasher
    {
        @Override
        public int hash(Object data) {
            return (data instanceof CharSequence) ? CrossHash.Wisp.hash((CharSequence) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left != null && left instanceof CharSequence && right instanceof CharSequence)
                    ? left.equals(right) : Objects.equals(left, right));
        }
        public static final WispStringHasher instance = new WispStringHasher();
    }
}
