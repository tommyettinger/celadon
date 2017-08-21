package lang.celadon.old;

import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.RNG;

import java.util.Collection;
import java.util.List;

/**
 * Created by Tommy Ettinger on 2/8/2017.
 */
public class TSet extends OrderedSet<Token> implements ICallByName {
    /**
     * Creates a new hash map.
     * <p>
     * <p>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
     *
     * @param expected the expected number of elements in the hash set.
     * @param f        the load factor.
     */
    public TSet(int expected, float f) {
        super(expected, f, Tools.TokenHasher.instance);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor.
     *
     * @param expected the expected number of elements in the hash set.
     */
    public TSet(int expected) {
        super(expected, Tools.TokenHasher.instance);
    }

    /**
     * Creates a new hash set with initial expected
     * {@link #DEFAULT_INITIAL_SIZE} elements and
     * {@link #DEFAULT_LOAD_FACTOR} as load factor.
     */
    public TSet() {
        super(Tools.TokenHasher.instance);
    }

    /**
     * Creates a new hash set copying a given collection.
     *
     * @param c a {@link Collection} to be copied into the new hash set.
     * @param f the load factor.
     */
    public TSet(Collection<? extends Token> c, float f) {
        super(c, f, Tools.TokenHasher.instance);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor copying a given collection.
     *
     * @param c a {@link Collection} to be copied into the new hash set.
     */
    public TSet(Collection<? extends Token> c) {
        super(c, Tools.TokenHasher.instance);
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
        super(a, offset, length, f, Tools.TokenHasher.instance);
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
        super(a, offset, length, Tools.TokenHasher.instance);
    }

    /**
     * Creates a new hash set copying the elements of an array.
     *
     * @param a an array to be copied into the new hash set.
     * @param f the load factor.
     */
    public TSet(Token[] a, float f) {
        super(a, f, Tools.TokenHasher.instance);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor copying the elements of an array.
     *
     * @param a an array to be copied into the new hash set.
     */
    public TSet(Token[] a) {
        super(a, Tools.TokenHasher.instance);
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

    @Override
    public Token call(Token name, List<Token> args)
    {
        return Methods.valueOf(name.asString()).call(this, args);
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

    enum Methods {
        contains {
            public Token call(OrderedSet<Token> me, List<Token> args) {return Token.stable(me.contains(args.get(0)));}
        },
        add {
            public Token call(OrderedSet<Token> me, List<Token> args) {return Token.stable(me.add(args.get(0)));}
        },
        remove {
            public Token call(OrderedSet<Token> me, List<Token> args) {return Token.stable(me.remove(args.get(0)));}
        },
        alter {
            public Token call(OrderedSet<Token> me, List<Token> args) {return Token.stable(me.alter(args.get(0), args.get(1)));}
        },
        randomItem {
            public Token call(OrderedSet<Token> me, List<Token> args) {return Token.stable(me.randomItem((RNG)args.get(0).solid));}
        },
        addAt {
            public Token call(OrderedSet<Token> me, List<Token> args) {return Token.stable(me.addAt(args.get(0), args.get(1).asInt()));}
        },
        getAt {
            public Token call(OrderedSet<Token> me, List<Token> args) {return Token.stable(me.getAt(args.get(0).asInt()));}
        },
        removeAt {
            public Token call(OrderedSet<Token> me, List<Token> args) {return Token.stable(me.removeAt(args.get(0).asInt()));}
        },
        addAll {
            public Token call(OrderedSet<Token> me, List<Token> args) {return Token.stable(me.addAll(args));}
        },
        size {
            public Token call(OrderedSet<Token> me, List<Token> args) {return Token.stable(me.size());}
        };

        public abstract Token call(OrderedSet<Token> me, List<Token> args);
    }
}
