/*
 * Copyright (C) 2002-2015 Sebastiano Vigna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package lang.celadon;

import squidpony.annotation.Beta;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.IntVLA;
import squidpony.squidmath.OrderedMap;

import java.io.Serializable;
import java.util.*;

/**
 * A hash map that allows duplicate keys and will refer to the most recently-added key for get() and remove().
 * Originally from fastutil as Object2ObjectLinkedOpenHashMap, modified to support indexed access as OrderedMap in
 * SquidLib, and modified again for the stack-of-duplicate-keys feature here.
 * <br>
 * <p>Instances of this class use a hash table to represent a map. The table is filled up to a specified <em>load factor</em>, and then doubled in size to accommodate new entries. If the table is
 * emptied below <em>one fourth</em> of the load factor, it is halved in size. However, halving is not performed when deleting entries from an iterator, as it would interfere with the iteration
 * process.
 * </p>
 * <p>Note that {@link #clear()} does not modify the hash table size. Rather, a family of {@linkplain #trim() trimming methods} lets you control the size of the table; this is particularly useful if
 * you reuse instances of this class.
 * </p>
 * <p>Iterators generated by this map will enumerate pairs in the same order in which they have been added to the map (addition of pairs whose key is already present in the set does not change the
 * iteration order). Note that this order has nothing in common with the natural order of the keys. The order is kept by means of a int-specialized list, {@link IntVLA}, and is modifiable with this
 * class' {@link #reorder(int...)} method, among other tools.
 * </p>
 * <p>This class implements the interface of a sorted map, so to allow easy access of the iteration order: for instance, you can get the first key in iteration order with {@code firstKey()} without
 * having to create an iterator; however, this class partially violates the {@link SortedMap} contract because all submap methods throw an exception and {@link #comparator()} returns always
 * <code>null</code>.
 * </p>
 * <p>Additional methods, such as <code>getAndMoveToFirst()</code>, make it easy to use instances of this class as a cache (e.g., with LRU policy).
 * </p>
 * <p>The iterators provided by the views of this class using are type-specific {@linkplain ListIterator list iterators}, and can be started at any element <em>which is a key of the map</em>,
 * or a {@link NoSuchElementException} exception will be thrown. If, however, the provided element is not the first or last key in the set, the first access to the list index will require linear time,
 * as in the worst case the entire key set must be scanned in iteration order to retrieve the positional index of the starting key.
 * </p>
 * <br>
 * Thank you, Sebastiano Vigna, for making FastUtil available to the public with such high quality.
 * <br>
 * See https://github.com/vigna/fastutil for the original library.
 * @author Sebastiano Vigna (responsible for all the hard parts)
 * @author Tommy Ettinger (responsible for the indexing stuff and the stack behavior)
 */
@Beta
public class StackMap<K, V> extends OrderedMap<K, V> implements Serializable {
    private static final long serialVersionUID = 0L;
    /**
     * The initial default size of a hash table.
     */
    public static final int DEFAULT_INITIAL_SIZE = 16;
    /**
     * The default load factor of a hash table.
     */
    public static final float DEFAULT_LOAD_FACTOR = .75f; // .1875f; // .75f;
    /**
     * The load factor for a (usually small) table that is meant to be particularly fast.
     */
    public static final float FAST_LOAD_FACTOR = .5f;
    /**
     * The load factor for a (usually very small) table that is meant to be extremely fast.
     */
    public static final float VERY_FAST_LOAD_FACTOR = .25f;

    /**
     * Creates a new StackMap.
     * <p>
     * <p>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
     *
     * @param expected the expected number of elements in the hash set.
     * @param f        the load factor.
     */

    @SuppressWarnings("unchecked")
    public StackMap(final int expected, final float f) {
        super(expected, f);
    }

    /**
     * Creates a new StackMap with 0.75f as load factor.
     *
     * @param expected the expected number of elements in the StackMap.
     */
    public StackMap(final int expected) {
        this(expected, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new StackMap with initial expected 16 entries and 0.75f as load factor.
     */
    public StackMap() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new StackMap copying a given one.
     *
     * @param m a {@link Map} to be copied into the new StackMap.
     * @param f the load factor.
     */
    public StackMap(final Map<? extends K, ? extends V> m, final float f) {
        this(m.size(), f);
        putAll(m);
    }

    /**
     * Creates a new StackMap with 0.75f as load factor copying a given one.
     *
     * @param m a {@link Map} to be copied into the new StackMap.
     */
    public StackMap(final Map<? extends K, ? extends V> m) {
        this(m, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new StackMap using the elements of two parallel arrays.
     *
     * @param keyArray the array of keys of the new StackMap.
     * @param valueArray the array of corresponding values in the new StackMap.
     * @param f the load factor.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public StackMap(final K[] keyArray, final V[] valueArray, final float f) {
        this(keyArray.length, f);
        if (keyArray.length != valueArray.length) {
            throw new IllegalArgumentException("The key array and the value array have different lengths (" + keyArray.length + " and " + valueArray.length + ")");
        }
        for (int i = 0; i < keyArray.length; i++) {
            put(keyArray[i], valueArray[i]);
        }
    }
    /**
     * Creates a new StackMap using the elements of two parallel arrays.
     *
     * @param keyColl the collection of keys of the new StackMap.
     * @param valueColl the collection of corresponding values in the new StackMap.
     * @param f the load factor.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public StackMap(final Collection<K> keyColl, final Collection<V> valueColl, final float f) {
        this(keyColl.size(), f);
        if (keyColl.size() != valueColl.size()) {
            throw new IllegalArgumentException("The key array and the value array have different lengths (" + keyColl.size() + " and " + valueColl.size() + ")");
        }
        Iterator<K> ki = keyColl.iterator();
        Iterator<V> vi = valueColl.iterator();
        while (ki.hasNext() && vi.hasNext())
        {
            put(ki.next(), vi.next());
        }
    }

    /**
     * Creates a new StackMap with 0.75f as load factor using the elements of two parallel arrays.
     *
     * @param keyArray the array of keys of the new StackMap.
     * @param valueArray the array of corresponding values in the new StackMap.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public StackMap(final K[] keyArray, final V[] valueArray) {
        this(keyArray, valueArray, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new StackMap.
     * <p>
     * <p>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
     *
     * @param expected the expected number of elements in the hash set.
     * @param f        the load factor.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */

    @SuppressWarnings("unchecked")
    public StackMap(final int expected, final float f, CrossHash.IHasher hasher) {
        super(expected, f, hasher);
    }
    /**
     * Creates a new StackMap with 0.75f as load factor.
     *
     * @param expected the expected number of elements in the StackMap.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public StackMap(final int expected, CrossHash.IHasher hasher) {
        this(expected, DEFAULT_LOAD_FACTOR, hasher);
    }

    /**
     * Creates a new StackMap with initial expected 16 entries and 0.75f as load factor.
     */
    public StackMap(CrossHash.IHasher hasher) {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR, hasher);
    }

    /**
     * Creates a new StackMap copying a given one.
     *
     * @param m a {@link Map} to be copied into the new StackMap.
     * @param f the load factor.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public StackMap(final Map<? extends K, ? extends V> m, final float f, CrossHash.IHasher hasher) {
        this(m.size(), f, hasher);
        putAll(m);
    }

    /**
     * Creates a new StackMap with 0.75f as load factor copying a given one.
     * @param m a {@link Map} to be copied into the new StackMap.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public StackMap(final Map<? extends K, ? extends V> m, CrossHash.IHasher hasher) {
        this(m, DEFAULT_LOAD_FACTOR, hasher);
    }

    /**
     * Creates a new StackMap using the elements of two parallel arrays.
     *
     * @param keyArray the array of keys of the new StackMap.
     * @param valueArray the array of corresponding values in the new StackMap.
     * @param f the load factor.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public StackMap(final K[] keyArray, final V[] valueArray, final float f, CrossHash.IHasher hasher) {
        this(keyArray.length, f, hasher);
        if (keyArray.length != valueArray.length) {
            throw new IllegalArgumentException("The key array and the value array have different lengths (" + keyArray.length + " and " + valueArray.length + ")");
        }
        for (int i = 0; i < keyArray.length; i++) {
            put(keyArray[i], valueArray[i]);
        }
    }
    /**
     * Creates a new StackMap with 0.75f as load factor using the elements of two parallel arrays.
     *
     * @param keyArray the array of keys of the new StackMap.
     * @param valueArray the array of corresponding values in the new StackMap.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public StackMap(final K[] keyArray, final V[] valueArray, CrossHash.IHasher hasher) {
        this(keyArray, valueArray, DEFAULT_LOAD_FACTOR, hasher);
    }

    private int realSize() {
        return containsNullKey ? size - 1 : size;
    }
    private void ensureCapacity(final int capacity) {
        final int needed = arraySize(capacity, f);
        if (needed > n) {
            rehash(needed);
        }
    }
    private void tryCapacity(final long capacity) {
        final int needed = (int) Math.min(
                1 << 30,
                Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity
                        / f))));
        if (needed > n) {
            rehash(needed);
        }
    }
    private V removeEntry(final int pos, final int h) {
        final V oldValue = value[pos];
        int posEnd = pos;
        final K[] key = this.key;
        // The starting point.
        if (key[h & mask] != null) {
            posEnd = h & mask;
            while (key[(posEnd + 1) & mask] != null) {
                posEnd = (posEnd + 1) & mask;
            }
        }

        value[pos] = value[posEnd];
        key[posEnd] = null;
        value[posEnd] = null;
        size--;
        fixOrder(posEnd);
        shiftKeys(posEnd);
        if (size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) {
            rehash(n / 2);
        }
        return oldValue;
    }
    private V removeNullEntry() {
        containsNullKey = false;
        key[n] = null;
        final V oldValue = value[n];
        value[n] = null;
        size--;
        fixOrder(n);
        if (size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) {
            rehash(n / 2);
        }
        return oldValue;
    }
    /** {@inheritDoc} */
    public void putAll(Map<? extends K, ? extends V> m) {
        if (f <= .5) {
            ensureCapacity(m.size()); // The resulting map will be sized for
        }
// m.size() elements
        else {
            tryCapacity(size() + m.size()); // The resulting map will be
        }
        int n = m.size();
        final Iterator<? extends Entry<? extends K, ? extends V>> i = m
                .entrySet().iterator();
        if (m instanceof StackMap) {
            Entry<? extends K, ? extends V> e;
            while (n-- != 0) {
                e = i.next();
                put(e.getKey(), e.getValue());
            }
        } else {
            Entry<? extends K, ? extends V> e;
            while (n-- != 0) {
                e = i.next();
                put(e.getKey(), e.getValue());
            }
        }
    }

    public void putAll(Collection<K> keyColl, Collection<V> valueColl)
    {
        Iterator<K> ki = keyColl.iterator();
        Iterator<V> vi = valueColl.iterator();
        while (ki.hasNext() && vi.hasNext())
        {
            put(ki.next(), vi.next());
        }
    }

    private int append(final K k, final V v) {
        int pos;
        if (k == null) {
            if (containsNullKey) {
                return n;
            }
            containsNullKey = true;
            pos = n;
        } else {
            final K[] key = this.key;
            // The starting point.
            if (key[pos = HashCommon.mix(hasher.hash(k)) & mask] != null) {
                //if (hasher.areEqual(curr, k))
                //    return pos;
                while (key[pos = (pos + 1) & mask] != null) {
                }
                //if (hasher.areEqual(curr, k))
                //    return pos;
            }
        }
        key[pos] = k;
        value[pos] = v;
        if (size == 0) {
            first = last = pos;
        } else {
            last = pos;
        }
        order.add(pos);
        if (size++ >= maxFill) {
            rehash(arraySize(size + 1, f));
        }
        return -1;
    }




    private int appendAndOverwrite(final K k, final V v, final V v0) {

        K curr;
        final K[] key = this.key;
        int pos, primary, secondary;
        // The starting point.
        if ((curr = key[pos = HashCommon.mix(hasher.hash(k)) & mask]) == null) {
            return append(k, v);
        }
        if (hasher.areEqual(k, curr)) {
            primary = pos;
        }// There's always an unused entry.
        else
        {
            while (true) {
                if ((curr = key[pos = (pos + 1) & mask]) == null) {
                    return append(k, v);
                }
                if (hasher.areEqual(k, curr))
                {
                    primary = pos;
                    break;
                }
            }
        }
        key[primary] = k;
        value[primary] = v;

        // The starting point.
        if (key[secondary = HashCommon.mix(hasher.hash(k)) & mask] != null) {
            //if (hasher.areEqual(curr, k))
            //    return pos;
            while (key[secondary = (secondary + 1) & mask] != null) {
            }
            //if (hasher.areEqual(curr, k))
            //    return pos;
        }
        key[secondary] = k;
        value[secondary] = v0;
        last = secondary;
        order.add(secondary);
        if (size++ >= maxFill) {
            rehash(arraySize(size + 1, f));
        }
        return -1;
    }



    private int insertAt(final K k, final V v, final int idx) {
        int pos;
        if (k == null) {
            if (containsNullKey)
            {
                fixOrder(n);
                order.insert(idx, n);
                return n;
            }
            containsNullKey = true;
            pos = n;
        } else {
            final K[] key = this.key;
            // The starting point.
            if (key[pos = HashCommon.mix(hasher.hash(k)) & mask] != null) {
                /*if (hasher.areEqual(curr, k))
                {
                    fixOrder(pos);
                    order.insert(idx, pos);
                    return pos;
                }*/
                while (key[pos = (pos + 1) & mask] != null) {
                }
                /*
                    if (hasher.areEqual(curr, k))
                    {
                        fixOrder(pos);
                        order.insert(idx, pos);
                        return pos;
                    }*/
            }
        }
        key[pos] = k;
        value[pos] = v;
        if (size == 0) {
            first = last = pos;
        }
        order.insert(idx, pos);
        if (size++ >= maxFill) {
            rehash(arraySize(size + 1, f));
        }
        return -1;
    }
    public V put(final K k, final V v) {
        if(!containsKey(k))
        {
            append(k, v);
            return defRetValue;
        }
        V v0 = get(k);
        appendAndOverwrite(k, v, v0);
        return v0;
    }
    public V putAt(final K k, final V v, final int idx) {
        if (idx <= 0) {
            return putAndMoveToFirst(k, v);
        } else if (idx >= size) {
            return putAndMoveToLast(k, v);
        }

        if (!containsKey(k)) {
            insertAt(k, v, idx);
            return defRetValue;
        }


        if (k == null) {
            if (containsNullKey) {
                return removeNullEntry();
            }
            return defRetValue;
        }
        else {
            K curr;
            V v0;
            final K[] key = this.key;
            int pos, idx0 = idx + 1;
            // The starting point.
            if ((curr = key[pos = HashCommon.mix(hasher.hash(k)) & mask]) == null) {
                v0 = defRetValue;
            } else if (hasher.areEqual(k, curr)) {
                final V oldValue = value[pos];
                value[pos] = null;
                size--;
                idx0 = fixOrder(pos);
                if (idx0 == idx) {
                    idx0++;
                }
                shiftKeys(pos);
                v0 = oldValue;
            } else {
                while (true) {
                    if ((curr = key[pos = (pos + 1) & mask]) == null) {
                        v0 = defRetValue;
                        break;
                    } else if (hasher.areEqual(k, curr)) {
                        final V oldValue = value[pos];
                        value[pos] = null;
                        size--;
                        idx0 = fixOrder(pos);
                        if (idx0 == idx) {
                            idx0++;
                        }
                        shiftKeys(pos);
                        v0 = oldValue;
                        break;
                    }
                }
            }

            insertAt(k, v, idx);
            insertAt(k, v0, idx0);
            return v0;
        }
    }

    /**
     * Unlike the normal {@link #put(Object, Object)} method, this will overwrite the most recent
     * key if there is one present, or it will add a new key if there is none.
     * @param k the key to add or overwrite
     * @param v the value to associate
     * @return the previous value associated with k, or {@link #defaultReturnValue()} if there was none
     */
    public V set(final K k, final V v) {
        return super.put(k, v);
    }
    @SuppressWarnings("unchecked")
    public V remove(final Object k) {
        if ((K) k == null) {
            if (containsNullKey) {
                return removeNullEntry();
            }
            return defRetValue;
        }
        K curr;
        final K[] key = this.key;
        int pos, h = HashCommon.mix(hasher.hash(k));
        // The starting point.
        if ((curr = key[pos = h & mask]) == null) {
            return defRetValue;
        }
        if (hasher.areEqual(k, curr)) {
            return removeEntry(pos, h);
        }
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == null) {
                return defRetValue;
            }
            if (hasher.areEqual(k, curr)) {
                return removeEntry(pos, h);
            }
        }
    }
    private V setValue(final int pos, final V v) {
        final V oldValue = value[pos];
        value[pos] = v;
        return oldValue;
    }

    private void moveIndexToFirst(final int i) {
        if(size <= 1 || first == i) {
            return;
        }
        order.moveToFirst(i);
        if (last == i) {
            last = order.peek();
            //last = (int) (link[i] >>> 32);
            // Special case of SET_NEXT( link[ last ], -1 );
            //link[last] |= -1 & 0xFFFFFFFFL;
        }/* else {
            final long linki = link[i];
            final int prev = (int) (linki >>> 32);
            final int next = (int) linki;
            link[prev] ^= ((link[prev] ^ (linki & 0xFFFFFFFFL)) & 0xFFFFFFFFL);
            link[next] ^= ((link[next] ^ (linki & 0xFFFFFFFF00000000L)) & 0xFFFFFFFF00000000L);
        }
        link[first] ^= ((link[first] ^ ((i & 0xFFFFFFFFL) << 32)) & 0xFFFFFFFF00000000L);
        link[i] = ((-1 & 0xFFFFFFFFL) << 32) | (first & 0xFFFFFFFFL);
        */
        first = i;
    }
    private void moveIndexToLast(final int i) {
        if(size <= 1 || last == i) {
            return;
        }
        order.moveToLast(i);
        if (first == i) {
            first = order.get(0);
            //first = (int) link[i];
            // Special case of SET_PREV( link[ first ], -1 );
            //link[first] |= (-1 & 0xFFFFFFFFL) << 32;
        } /*else {
            final long linki = link[i];
            final int prev = (int) (linki >>> 32);
            final int next = (int) linki;
            link[prev] ^= ((link[prev] ^ (linki & 0xFFFFFFFFL)) & 0xFFFFFFFFL);
            link[next] ^= ((link[next] ^ (linki & 0xFFFFFFFF00000000L)) & 0xFFFFFFFF00000000L);
        }
        link[last] ^= ((link[last] ^ (i & 0xFFFFFFFFL)) & 0xFFFFFFFFL);
        link[i] = ((last & 0xFFFFFFFFL) << 32) | (-1 & 0xFFFFFFFFL);
        */
        last = i;
    }
    /**
     * Adds a pair to the map; if the key is already present, it is moved to the
     * first position of the iteration order.
     *
     * @param k
     *            the key.
     * @param v
     *            the value.
     * @return the old value, or the {@linkplain #defaultReturnValue() default
     *         return value} if no value was present for the given key.
     */
    public V putAndMoveToFirst(final K k, final V v) {
        int pos;
        if (k == null) {
            if (containsNullKey) {
                moveIndexToFirst(n);
                return setValue(n, v);
            }
            containsNullKey = true;
            pos = n;
        } else {
            final K[] key = this.key;
            // The starting point.
            if (key[pos = HashCommon.mix(hasher.hash(k)) & mask] != null) {
                /*if (hasher.areEqual(curr, k)) {
                    moveIndexToFirst(pos);
                    return setValue(pos, v);
                }*/
                while (key[pos = (pos + 1) & mask] != null) {
                }
                    /*if (hasher.areEqual(curr, k)) {
                        moveIndexToFirst(pos);
                        return setValue(pos, v);
                    }*/
            }
        }
        key[pos] = k;
        value[pos] = v;
        if (size == 0) {
            first = last = pos;
            // Special case of SET_UPPER_LOWER( link[ pos ], -1, -1 );
            //link[pos] = -1L;
        } else {
            //link[first] ^= ((link[first] ^ ((pos & 0xFFFFFFFFL) << 32)) & 0xFFFFFFFF00000000L);
            //link[pos] = ((-1 & 0xFFFFFFFFL) << 32) | (first & 0xFFFFFFFFL);
            first = pos;
        }
        order.insert(0, pos);
        if (size++ >= maxFill) {
            rehash(arraySize(size, f));
        }
        return defRetValue;
    }
    /**
     * Adds a pair to the map; if the key is already present, it is moved to the
     * last position of the iteration order.
     *
     * @param k
     *            the key.
     * @param v
     *            the value.
     * @return the old value, or the {@linkplain #defaultReturnValue() default
     *         return value} if no value was present for the given key.
     */
    public V putAndMoveToLast(final K k, final V v) {
        int pos;
        if (k == null) {
            if (containsNullKey) {
                moveIndexToLast(n);
                return setValue(n, v);
            }
            containsNullKey = true;
            pos = n;
        } else {
            final K[] key = this.key;
            // The starting point.
            if (key[pos = HashCommon.mix(hasher.hash(k)) & mask] != null) {
                /*if (hasher.areEqual(curr, k)) {
                    moveIndexToLast(pos);
                    return setValue(pos, v);
                }*/
                while (key[pos = (pos + 1) & mask] != null) {
                }
                    /*if (hasher.areEqual(curr, k)) {
                        moveIndexToLast(pos);
                        return setValue(pos, v);
                    }*/
            }
        }
        key[pos] = k;
        value[pos] = v;
        if (size == 0) {
            first = last = pos;
            // Special case of SET_UPPER_LOWER( link[ pos ], -1, -1 );
            //link[pos] = -1L;
        } else {
            //link[last] ^= ((link[last] ^ (pos & 0xFFFFFFFFL)) & 0xFFFFFFFFL);
            //link[pos] = ((last & 0xFFFFFFFFL) << 32) | (-1 & 0xFFFFFFFFL);
            last = pos;
        }
        if(order.peek() != pos) {
            order.add(pos);
        }
        if (size++ >= maxFill) {
            rehash(arraySize(size, f));
        }
        return defRetValue;
    }

    /*
     * Removes all elements from this map.
     *
     * <P>To increase object reuse, this method does not change the table size.
     * If you want to reduce the table size, you must use {@link #trim()}.
     */
    public void clear() {
        if (size == 0) {
            return;
        }
        size = 0;
        containsNullKey = false;
        Arrays.fill(key, null);
        Arrays.fill(value, null);
        first = last = -1;
        order.clear();
    }

    /**
     * The entry class for a StackMap does not record key and value, but rather the position in the hash table of the corresponding entry. This is necessary so that calls to
     * {@link Entry#setValue(Object)} are reflected in the map
     */
    final class MapEntry
            implements
            Entry<K, V> {
        // The table index this entry refers to, or -1 if this entry has been
        // deleted.
        int index;
        MapEntry(final int index) {
            this.index = index;
        }
        MapEntry() {
        }
        public K getKey() {
            return key[index];
        }
        public V getValue() {
            return value[index];
        }
        public V setValue(final V v) {
            final V oldValue = value[index];
            value[index] = v;
            return oldValue;
        }
        @SuppressWarnings("unchecked")
        public boolean equals(final Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Entry<K, V> e = (Entry<K, V>) o;
            return (key[index] == null
                    ? e.getKey() == null
                    : hasher.areEqual(key[index], e.getKey()))
                    && (value[index] == null
                    ? e.getValue() == null
                    : value[index].equals(e.getValue()));
        }
        public int hashCode() {
            return hasher.hash(key[index])
                    ^ (value[index] == null ? 0 : value[index].hashCode());
        }
        @Override
        public String toString() {
            return key[index] + "=>" + value[index];
        }
    }

    /**
     * Modifies the ordering so that the given entry is removed. This method will complete in logarithmic time.
     *
     * @param i the index of an entry.
     * @return the iteration-order index of the removed entry
     */
    protected int fixOrder(final int i) {
        if (size == 0) {
            order.clear();
            first = last = -1;
            return 0;
        }
        int idx = order.removeValue(i);
        if (first == i) {
            first = order.get(0);
        }
        if (last == i) {
            last = order.peek();
        }
        return idx;
    }

    /**
     * Modifies the ordering for a shift from s to d.
     * <br>
     * This method will complete in logarithmic time or better.
     *
     * @param s the source position.
     * @param d the destination position.
     */
    protected void fixOrder(int s, int d) {
        if (size == 1) {
            first = last = d;
            order.set(0, d);
        }
        else if (first == s) {
            first = d;
            order.set(0, d);
        }
        else if (last == s) {
            last = d;
            order.set(order.size - 1, d);
        }
        else
        {
            order.set(order.indexOf(s), d);
        }
    }


    /**
     * Returns a hash code for this map.
     *
     * This method overrides the generic method provided by the superclass.
     * Since <code>equals()</code> is not overriden, it is important that the
     * value returned by this method is the same value as the one returned by
     * the overriden method.
     *
     * @return a hash code for this map.
     */
    public int hashCode() {
        int h = 0;
        for (int j = realSize(), i = 0, t = 0; j-- != 0;) {
            while (key[i] == null) {
                i++;
            }
            if (this != key[i]) {
                t = hasher.hash(key[i]);
            }
            if (this != value[i]) {
                t ^= value[i] == null ? 0 : value[i].hashCode();
            }
            h += t;
            i++;
        }
        // Zero / null keys have hash zero.
        if (containsNullKey) {
            h += value[n] == null ? 0 : value[n].hashCode();
        }
        return h;
    }

    private static class HashCommon {

        private HashCommon() {
        }

        /**
         * This reference is used to fill keys and values of removed entries (if
         * they are objects). <code>null</code> cannot be used as it would confuse the
         * search algorithm in the presence of an actual <code>null</code> key.
         */
        public static final Object REMOVED = new Object();

        /**
         * 2<sup>32</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2.
         */
        private static final int INT_PHI = 0x9E3779B9;
        /**
         * The reciprocal of {@link #INT_PHI} modulo 2<sup>32</sup>.
         */
        private static final int INV_INT_PHI = 0x144cbc89;
        /**
         * 2<sup>64</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2.
         */
        private static final long LONG_PHI = 0x9E3779B97F4A7C15L;
        /**
         * The reciprocal of {@link #LONG_PHI} modulo 2<sup>64</sup>.
         */
        private static final long INV_LONG_PHI = 0xF1DE83E19937733DL;

        /**
         * Quickly mixes the bits of an integer.
         * <br>This method mixes the bits of the argument by multiplying by the golden ratio and
         * xorshifting the result. It is borrowed from <a href="https://github.com/OpenHFT/Koloboke">Koloboke</a>, and
         * it has slightly worse behaviour than murmurHash3 (in open-addressing hash tables the average number of probes
         * is slightly larger), but it's much faster.
         *
         * @param x an integer.
         * @return a hash value obtained by mixing the bits of {@code x}.
         */
        static int mix(final int x) {
            final int h = x * INT_PHI;
            return h ^ (h >>> 16);
        }

        /**
         * Return the least power of two greater than or equal to the specified value.
         * <br>Note that this function will return 1 when the argument is 0.
         *
         * @param x a long integer smaller than or equal to 2<sup>62</sup>.
         * @return the least power of two greater than or equal to the specified value.
         */
        static long nextPowerOfTwo(long x) {
            if (x == 0) {
                return 1;
            }
            x--;
            x |= x >> 1;
            x |= x >> 2;
            x |= x >> 4;
            x |= x >> 8;
            x |= x >> 16;
            return (x | x >> 32) + 1;
        }

    }

    /**
     * Unwraps an iterator into an array starting at a given offset for a given number of elements.
     * <p>
     * <P>This method iterates over the given type-specific iterator and stores the elements returned, up to a maximum of <code>length</code>, in the given array starting at <code>offset</code>. The
     * number of actually unwrapped elements is returned (it may be less than <code>max</code> if the iterator emits less than <code>max</code> elements).
     *
     * @param i      a type-specific iterator.
     * @param array  an array to contain the output of the iterator.
     * @param offset the first element of the array to be returned.
     * @param max    the maximum number of elements to unwrap.
     * @return the number of elements unwrapped.
     */
    private int unwrap(final ValueIterator i, final Object[] array, int offset, final int max) {
        if (max < 0) {
            throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
        }
        if (offset < 0 || offset + max > array.length) {
            throw new IllegalArgumentException();
        }
        int j = max;
        while (j-- != 0 && i.hasNext()) {
            array[offset++] = i.next();
        }
        return max - j - 1;
    }

    /**
     * Unwraps an iterator into an array.
     * <p>
     * <P>This method iterates over the given type-specific iterator and stores the elements returned in the given array. The iteration will stop when the iterator has no more elements or when the end
     * of the array has been reached.
     *
     * @param i     a type-specific iterator.
     * @param array an array to contain the output of the iterator.
     * @return the number of elements unwrapped.
     */
    private int unwrap(final ValueIterator i, final Object[] array) {
        return unwrap(i, array, 0, array.length);
    }


    /** Unwraps an iterator into an array starting at a given offset for a given number of elements.
     *
     * <P>This method iterates over the given type-specific iterator and stores the elements returned, up to a maximum of <code>length</code>, in the given array starting at <code>offset</code>. The
     * number of actually unwrapped elements is returned (it may be less than <code>max</code> if the iterator emits less than <code>max</code> elements).
     *
     * @param i a type-specific iterator.
     * @param array an array to contain the output of the iterator.
     * @param offset the first element of the array to be returned.
     * @param max the maximum number of elements to unwrap.
     * @return the number of elements unwrapped. */
    private static <K> int objectUnwrap(final Iterator<? extends K> i, final K array[], int offset, final int max ) {
        if ( max < 0 ) {
            throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
        }
        if ( offset < 0 || offset + max > array.length ) {
            throw new IllegalArgumentException();
        }
        int j = max;
        while ( j-- != 0 && i.hasNext() ) {
            array[offset++] = i.next();
        }
        return max - j - 1;
    }

    /** Unwraps an iterator into an array.
     *
     * <P>This method iterates over the given type-specific iterator and stores the elements returned in the given array. The iteration will stop when the iterator has no more elements or when the end
     * of the array has been reached.
     *
     * @param i a type-specific iterator.
     * @param array an array to contain the output of the iterator.
     * @return the number of elements unwrapped. */
    private static <K> int objectUnwrap(final Iterator<? extends K> i, final K array[] ) {
        return objectUnwrap(i, array, 0, array.length );
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        int n = size(), i = 0;
        boolean first = true;
        s.append("StackMap{");
        while (i < n) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }
            s.append(entryAt(i++));
        }
        s.append("}");
        return s.toString();
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Map)) {
            return false;
        }
        Map<?, ?> m = (Map<?, ?>) o;
        if (m.size() != size()) {
            return false;
        }
        return entrySet().containsAll(m.entrySet());
    }

    public V removeAt(final int idx) {

        if (idx < 0 || idx >= order.size) {
            return defRetValue;
        }
        int pos = order.get(idx);
        if (key[pos] == null) {
            if (containsNullKey) {
                return removeNullEntry();
            }
            return defRetValue;
        }
        return removeEntry(pos, pos);
    }

    public List<V> getMany(Collection<K> keys)
    {
        if(keys == null) {
            return new ArrayList<>(1);
        }
        ArrayList<V> vals = new ArrayList<>(keys.size());
        for(K k : keys)
        {
            vals.add(get(k));
        }
        return vals;
    }
}
