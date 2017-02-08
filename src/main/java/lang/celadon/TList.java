package lang.celadon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Tommy Ettinger on 2/6/2017.
 */
public class TList extends ArrayList<Token> implements Serializable {
    private static final long serialVersionUID = 0;
    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *                                  is negative
     */
    public TList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public TList() {
        super();
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public TList(Collection<? extends Token> c) {
        super(c);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        int n = this.size(), i = 0;
        boolean first = true;
        s.append("[");

        for(; i < n; s.append(this.get(i++))) {
            if(first) {
                first = false;
            } else {
                s.append(' ');
            }
        }
        return s.append(']').toString();
    }



    @SuppressWarnings("unchecked")
    public <TYPE> ArrayList<TYPE> as(Class<TYPE> clazz)
    {
        int sz = size();
        Token tk;
        ArrayList<TYPE> next = new ArrayList<>(sz);
        for (int i = 0; i < sz; i++) {
            if((tk = get(i)).special > 0)
                next.add((TYPE)tk.solid);
        }
        return next;
    }

    public static final TList empty = new EmptyTList();

    private static class EmptyTList extends TList
    {
        /**
         * Constructs an empty immutable TList.
         */
        public EmptyTList() {
            super();
        }

        /**
         * Throws an UnsupportedOperationException; this variety of TList is immutable.
         *
         * @param index   index of the element to replace
         * @param element element to be stored at the specified position
         * @return the element previously at the specified position
         * @throws UnsupportedOperationException always
         */
        @Override
        public Token set(int index, Token element) {
            throw new UnsupportedOperationException("Cannot modify this TList");
        }

        /**
         * Throws an UnsupportedOperationException; this variety of TList is immutable.
         *
         * @param token element to be appended to this list
         * @return <tt>true</tt> (as specified by {@link Collection#add})
         * @throws UnsupportedOperationException always
         */
        @Override
        public boolean add(Token token) {
            throw new UnsupportedOperationException("Cannot modify this TList");
        }

        /**
         * Throws an UnsupportedOperationException; this variety of TList is immutable.
         *
         * @param index   index at which the specified element is to be inserted
         * @param element element to be inserted
         * @throws UnsupportedOperationException always
         */
        @Override
        public void add(int index, Token element) {
            throw new UnsupportedOperationException("Cannot modify this TList");
        }

        /**
         * Throws an UnsupportedOperationException; this variety of TList is immutable.
         *
         * @param index the index of the element to be removed
         * @return the element that was removed from the list
         * @throws UnsupportedOperationException always
         */
        @Override
        public Token remove(int index) {
            throw new UnsupportedOperationException("Cannot modify this TList");
        }

        /**
         * Throws an UnsupportedOperationException; this variety of TList is immutable.
         *
         * @param o element to be removed from this list, if present
         * @return <tt>true</tt> if this list contained the specified element
         * @throws UnsupportedOperationException always
         */
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Cannot modify this TList");
        }

        /**
         * Does nothing; this variety of TList is always empty, so clearing it has no effect.
         */
        @Override
        public void clear() {
        }

        /**
         * Throws an UnsupportedOperationException; this variety of TList is immutable.
         *
         * @param c collection containing elements to be added to this list
         * @return <tt>true</tt> if this list changed as a result of the call
         * @throws NullPointerException if the specified collection is null
         */
        @Override
        public boolean addAll(Collection<? extends Token> c) {
            throw new UnsupportedOperationException("Cannot modify this TList");
        }

        /**
         * Throws an UnsupportedOperationException; this variety of TList is immutable.
         *
         * @param index index at which to insert the first element from the
         *              specified collection
         * @param c     collection containing elements to be added to this list
         * @return <tt>true</tt> if this list changed as a result of the call
         * @throws UnsupportedOperationException always
         */
        @Override
        public boolean addAll(int index, Collection<? extends Token> c) {
            throw new UnsupportedOperationException("Cannot modify this TList");
        }

        /**
         * Throws an UnsupportedOperationException; this variety of TList is immutable.
         *
         * @param fromIndex
         * @param toIndex
         * @throws UnsupportedOperationException always
         */
        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException("Cannot modify this TList");
        }

        /**
         * Throws an UnsupportedOperationException; this variety of TList is immutable.
         *
         * @param c collection containing elements to be removed from this list
         * @return {@code true} if this list changed as a result of the call
         * @throws UnsupportedOperationException always
         * @see Collection#contains(Object)
         */
        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Cannot modify this TList");
        }

        /**
         * Throws an UnsupportedOperationException; this variety of TList is immutable.
         *
         * @param c collection containing elements to be retained in this list
         * @return {@code true} if this list changed as a result of the call
         * @throws UnsupportedOperationException always
         * @see Collection#contains(Object)
         */
        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Cannot modify this TList");
        }
    }
}
