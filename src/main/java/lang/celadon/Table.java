package lang.celadon;

import squidpony.squidmath.OrderedMap;

import java.io.Serializable;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An OrderedMap with some section of the iteration specialized for sequential integer keys (a list) and the rest still
 * available for iteration as a whole, including any integer keys. Meant to act like Lua's tables.
 * Created by Tommy Ettinger on 8/20/2017.
 */
public class Table extends OrderedMap<Object, Object> implements Methodical {

    public int sequentialLimit = 0;

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

    @Override
    public Object put(Object k, Object v) {
        if(k instanceof Number && ((Number)k).intValue() == sequentialLimit)
        {
            ++sequentialLimit;
            while (containsKey(sequentialLimit)) ++sequentialLimit;
        }
        return super.put(k, v);
    }

    @Override
    public Object putAt(Object k, Object v, int idx) {
        if(idx <= 0)
            return putAndMoveToFirst(k, v);
        else if(idx >= size)
            return putAndMoveToLast(k, v);
        else
        {
            if(k instanceof Number && ((Number)k).intValue() == sequentialLimit)
            {
                ++sequentialLimit;
                while (containsKey(sequentialLimit)) ++sequentialLimit;
            }
            return super.putAt(k, v, idx);
        }
    }

    /**
     * Adds a pair to the map; if the key is already present, it is moved to the
     * first position of the iteration order.
     *
     * @param k the key.
     * @param v the value.
     * @return the old value, or the {@linkplain #defaultReturnValue() default
     * return value} if no value was present for the given key.
     */
    @Override
    public Object putAndMoveToFirst(Object k, Object v) {
        if(k instanceof Number && ((Number)k).intValue() == sequentialLimit)
        {
            ++sequentialLimit;
            while (containsKey(sequentialLimit)) ++sequentialLimit;
        }
        return super.putAndMoveToFirst(k, v);
    }

    /**
     * Adds a pair to the map; if the key is already present, it is moved to the
     * last position of the iteration order.
     *
     * @param k the key.
     * @param v the value.
     * @return the old value, or the {@linkplain #defaultReturnValue() default
     * return value} if no value was present for the given key.
     */
    @Override
    public Object putAndMoveToLast(Object k, Object v) {
        if(k instanceof Number && ((Number)k).intValue() == sequentialLimit)
        {
            ++sequentialLimit;
            while (containsKey(sequentialLimit)) ++sequentialLimit;
        }
        return super.putAndMoveToLast(k, v);
    }

    @Override
    public Object remove(Object k) {
        if(sequentialLimit > 0) {
            int s;
            if (k instanceof Number && (s = ((Number) k).intValue()) >= 0 && s < sequentialLimit)
                sequentialLimit = s;
        }
        return super.remove(k);
    }

    /**
     * Removes the mapping associated with the first key in iteration order.
     *
     * @return the value previously associated with the first key in iteration
     * order.
     * @throws NoSuchElementException is this map is empty.
     */
    @Override
    public Object removeFirst() {
        if(sequentialLimit > 0) {
            int s;
            Object k = key[order.first()];
            if (k instanceof Number && (s = ((Number) k).intValue()) >= 0 && s < sequentialLimit)
                sequentialLimit = s;
        }
        return super.removeFirst();
    }

    /**
     * Removes the mapping associated with the last key in iteration order.
     *
     * @return the value previously associated with the last key in iteration
     * order.
     * @throws NoSuchElementException is this map is empty.
     */
    @Override
    public Object removeLast() {
        if(sequentialLimit > 0) {
            int s;
            Object k = key[order.peek()];
            if (k instanceof Number && (s = ((Number) k).intValue()) >= 0 && s < sequentialLimit)
                sequentialLimit = s;
        }
        return super.removeLast();
    }

    /**
     * Swaps a key, original, for another key, replacement, while keeping replacement at the same point in the iteration
     * order as original and keeping it associated with the same value (which also keeps its iteration index).
     * Be aware that if both original and replacement are present in the OrderedMap, this will still replace original
     * with replacement but will also remove the other occurrence of replacement to avoid duplicate keys. This can throw
     * off the expected order because the duplicate could be at any point in the ordering when it is removed. You may
     * want to prefer {@link #alterCarefully(Object, Object)} if you don't feel like checking by hand for whether
     * replacement is already present, but using this method is perfectly reasonable if you know overlaps won't happen.
     *
     * @param original    the key to find and swap out
     * @param replacement the key to replace original with
     * @return the value associated with original before, and replacement now
     */
    @Override
    public Object alter(Object original, Object replacement) {
        int s;
        if(original instanceof Number && (s = ((Number)original).intValue()) >= 0 && s < sequentialLimit)
            sequentialLimit = s;
        if(replacement instanceof Number && ((Number)replacement).intValue() == sequentialLimit)
        {
            ++sequentialLimit;
            while (containsKey(sequentialLimit)) ++sequentialLimit;
        }
        return super.alter(original, replacement);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        int n = this.size();
        int i = 0;
        boolean first = true;
        s.append("{");

        for(; i < n; i++) {
            s.append(this.keyAt(i));
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }
        }

        s.append("}");
        return s.toString();
    }

    public final class SequenceKeyIterator implements ListIterator<Integer>, Serializable {
        private static final long serialVersionUID = 0L;
        private int curr = 0;

        @Override
        public boolean hasNext() {
            return curr < sequentialLimit;
        }

        @Override
        public boolean hasPrevious() {
            return sequentialLimit > 0 && curr > 0;
        }

        @Override
        public int nextIndex() {
            return curr + 1;
        }

        @Override
        public int previousIndex() {
            return curr - 1;
        }

        public Integer previous()
        {
            if(!hasPrevious())
                throw new NoSuchElementException("No previous element in SequenceKeyIterator");
            return curr--;
        }
        public void set(Integer k) {
            throw new UnsupportedOperationException("SequenceKeyIterator.set() not supported");
        }
        public void add(Integer k) {
            throw new UnsupportedOperationException("SequenceKeyIterator.add() not supported");
        }
        public SequenceKeyIterator() {}
        public Integer next() {
            if(!hasNext())
                throw new NoSuchElementException("No next element in SequenceKeyIterator");
            return curr++;
        }
        public void remove() {
            if(curr >= 1 && curr - 1 < sequentialLimit)
                Table.this.remove(curr - 1);
            else
                throw new UnsupportedOperationException("Cannot remove via SequenceKeyIterator if sequence is exhausted");
        }
    }

    public final class SequenceValueIterator implements ListIterator<Object>, Serializable {
        private static final long serialVersionUID = 0L;
        private int curr = 0;

        @Override
        public boolean hasNext() {
            return curr < sequentialLimit;
        }

        @Override
        public boolean hasPrevious() {
            return sequentialLimit > 0 && curr > 0;
        }

        @Override
        public int nextIndex() {
            return curr + 1;
        }

        @Override
        public int previousIndex() {
            return curr - 1;
        }

        public Object previous()
        {
            if(!hasPrevious())
                throw new NoSuchElementException("No previous element in SequenceValueIterator");
            return get(curr--);
        }
        public void set(Object k) {
            throw new UnsupportedOperationException("SequenceValueIterator.set() not supported");
        }
        public void add(Object k) {
            throw new UnsupportedOperationException("SequenceValueIterator.add() not supported");
        }
        public SequenceValueIterator() {}
        public Object next() {
            if(!hasNext())
                throw new NoSuchElementException("No next element in SequenceValueIterator");
            return get(curr++);
        }
        public void remove() {
            if(curr >= 1 && curr - 1 < sequentialLimit)
                Table.this.remove(curr - 1);
            else
                throw new UnsupportedOperationException("Cannot remove via SequenceValueIterator if sequence is exhausted");
        }
    }

    public ListIterator<Integer> seqKeyIterator()
    {
        return new SequenceKeyIterator();
    }
    public ListIterator<Object> seqValueIterator()
    {
        return new SequenceValueIterator();
    }

    @Override
    public Cel run(Cel right, String name) {
        return Cel.nothing;
    }
}
