package lang.celadon;

import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.RNG;

import java.util.Collection;

/**
 * Created by Tommy Ettinger on 2/8/2017.
 */
public class TSet extends OrderedSet<Token> {
    /**
     * Creates a new hash map.
     * <p>
     * <p>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
     *
     * @param expected the expected number of elements in the hash set.
     * @param f        the load factor.
     */
    public TSet(int expected, float f) {
        super(expected, f, Tools.WispTokenHasher.instance);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor.
     *
     * @param expected the expected number of elements in the hash set.
     */
    public TSet(int expected) {
        super(expected, Tools.WispTokenHasher.instance);
    }

    /**
     * Creates a new hash set with initial expected
     * {@link #DEFAULT_INITIAL_SIZE} elements and
     * {@link #DEFAULT_LOAD_FACTOR} as load factor.
     */
    public TSet() {
        super(Tools.WispTokenHasher.instance);
    }

    /**
     * Creates a new hash set copying a given collection.
     *
     * @param c a {@link Collection} to be copied into the new hash set.
     * @param f the load factor.
     */
    public TSet(Collection<? extends Token> c, float f) {
        super(c, f, Tools.WispTokenHasher.instance);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor copying a given collection.
     *
     * @param c a {@link Collection} to be copied into the new hash set.
     */
    public TSet(Collection<? extends Token> c) {
        super(c, Tools.WispTokenHasher.instance);
    }

    /**
     * Creates a new hash set and fills it with the elements of a given array.
     *
     * @param a      an array whose elements will be used to fill the set.
     * @param offset the first element to use.
     * @param length the number of elements to use.
     * @param f      the load factor.
     */
    public TSet(Token[] a, int offset, int length, float f) {
        super(a, offset, length, f, Tools.WispTokenHasher.instance);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor and fills it with the elements of a given array.
     *
     * @param a      an array whose elements will be used to fill the set.
     * @param offset the first element to use.
     * @param length the number of elements to use.
     */
    public TSet(Token[] a, int offset, int length) {
        super(a, offset, length, Tools.WispTokenHasher.instance);
    }

    /**
     * Creates a new hash set copying the elements of an array.
     *
     * @param a an array to be copied into the new hash set.
     * @param f the load factor.
     */
    public TSet(Token[] a, float f) {
        super(a, f, Tools.WispTokenHasher.instance);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor copying the elements of an array.
     *
     * @param a an array to be copied into the new hash set.
     */
    public TSet(Token[] a) {
        super(a, Tools.WispTokenHasher.instance);
    }

    @SuppressWarnings("unchecked")
    public <TYPE> OrderedSet<TYPE> as(Class<TYPE> clazz)
    {
        int sz = size();
        Token tk;
        OrderedSet<TYPE> next = new OrderedSet<>(sz);
        for (int i = 0; i < sz; i++) {
            if((tk = getAt(i)).special > 0)
                next.add((TYPE)tk.solid);
        }
        return next;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        int n = this.size(), i = 0;
        boolean first = true;
        s.append("#set[");

        for(; i < n; s.append(this.getAt(i++))) {
            if(first) {
                first = false;
            } else {
                s.append(' ');
            }
        }
        return s.append(']').toString();
    }

    public static final TSet empty = new EmptyTSet();

    private static class EmptyTSet extends TSet
    {
        /**
         * Creates a new hash set with initial expected
         * {@link #DEFAULT_INITIAL_SIZE} elements and
         * {@link #DEFAULT_LOAD_FACTOR} as load factor.
         */
        public EmptyTSet() {
            super(0);
        }

        @Override
        public boolean addAll(Collection<? extends Token> c) {
            throw new UnsupportedOperationException("Cannot modify this TSet");

        }

        @Override
        public boolean add(Token token) {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public boolean addAt(Token token, int idx) {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public Token addOrGet(Token token) {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        protected boolean rem(Object k) {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public Token removeFirst() {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public Token removeLast() {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public boolean addAndMoveToFirst(Token token) {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public boolean addAndMoveToLast(Token token) {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public void clear() {
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Cannot modify this TSet");

        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public boolean trim() {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public boolean trim(int n) {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public boolean removeAt(int idx) {
            throw new UnsupportedOperationException("Cannot modify this TSet");

        }

        @Override
        public OrderedSet<Token> shuffle(RNG rng) {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public OrderedSet<Token> reorder(int... ordering) {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }

        @Override
        public boolean alter(Token original, Token replacement) {
            throw new UnsupportedOperationException("Cannot modify this TSet");
        }
    }

}
