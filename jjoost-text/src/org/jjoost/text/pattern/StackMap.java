package org.jjoost.text.pattern;

import java.io.Serializable;
import java.util.Random;

import org.jjoost.util.Function;

// this is not an arbitrary stack - it is specifically designed to follow a function call stack
// i.e., any items x1 and x2 where x1 was pushed before x2 should be popped in reverse order, i.e. with x2 popped before x1   
public abstract class StackMap<K, V> implements Function<K, V> {

	private static final long serialVersionUID = 1867804375186058123L;

	private static final class Entry<K, V> implements Serializable {
		private static final long serialVersionUID = 2408050514427108514L;
		final K key;
		final int hash;
		final V value;
		final Entry<K, V> next;
		final int gen;
		int depth;
		private Entry(K key, int hash, V value, Entry<K, V> next, int gen) {
			this.key = key;
			this.hash = hash;
			this.value = value;
			this.next = next;
			this.gen = gen;
			if (next == null) {
				depth = 1;
			} else {
				depth = next.depth + 1;
			}			
		}
	}
	
	private final float loadFactor = 0.5f;
	private int tableSize = 8;
	private int tableOverflow = 2;
	@SuppressWarnings("unchecked")
	private Entry<K, V>[] table = new Entry[10];
	private int keyCount;
	private int keyCapacity = 4;
	private int gen = 0;
	
	@SuppressWarnings("unchecked")
	public void grow() {
		final Entry<K, V>[] oldTable = table;
		final Entry<K, V>[] newTable = new Entry[tableOverflow + (tableSize <<= 1)];
		for (int i = 0 ; i != oldTable.length ; i++) {
			Entry<K, V> e = oldTable[i];
			if (e == null) {
				continue;
			}
			int j = e.hash & (tableSize - 1);
			while (newTable[j] != null) {
				if (newTable[j].gen > e.gen) {
					Entry<K, V> t = newTable[j];
					newTable[j] = e;
					e = t;
				}
				// cannot get past overflow, as must at most usr as much overflow as prior table 
				j += 1;
			}
			newTable[j] = e;
		}
		table = newTable;
		keyCapacity = (int) (tableSize * loadFactor);
	}
	
	public static void main(String[] args) {
		for (int r = 0 ; r != 1 << 10 ; r++) {
			final ObjStackMap<Integer, Integer> is = new ObjStackMap<Integer, Integer>();
			final Random rnd = new Random();
			int c;
			for (c = 0 ; c < 1 << 20 ; c++) {
				is.push(rnd.nextInt(), c);
			}
			System.out.println(c);
		}
	}
	
	protected abstract int hash(Object key);
	protected abstract boolean equals(Object a, Object b);
	
	public int push(K key, V value) {
		final int hash = hash(key);
		if (keyCount == keyCapacity) {
			grow();
		}
		Entry<K, V>[] table = this.table;
		int j = hash & (tableSize - 1);
		while (j != table.length && table[j] != null && (table[j].hash != hash || !equals(key, table[j].key))) {
			j += 1;
		}
		if (j == table.length) {
			tableOverflow <<= 1;
			this.table = table = java.util.Arrays.copyOf(table, tableOverflow + tableSize);
		}
		Entry<K, V> e = new Entry<K, V>(key, hash, value, table[j], (table[j] == null ? gen++ : table[j].gen));
		table[j] = e;
		if (e.next == null) {
			keyCount += 1;
		}
		return e.depth;
	}
	
	public V peek(Object key) {
		final Entry<K, V> e = head(key);
		if (e != null) {
			return e.value;
		}
		return null;
	}
	
	private final Entry<K, V> head(Object key) {
		final int j = index(key);
		return j < table.length ? table[j] : null;		
	}
	
	private final int index(Object key) {
		final int hash = hash(key);
		int j = hash & (tableSize - 1);
		while (j != table.length && table[j] != null && (table[j].hash != hash || !equals(key, table[j].key))) {
			j += 1;
		}
		return j;		
	}
	
	public boolean isSingletonStack(K key) {
		final Entry<K, V> e = head(key);
		if (e != null) {
			return e.next == null;
		}
		throw new IllegalArgumentException(key + " is not present in the map, so the stack cannot be singleton");
	}
	
	public void pop(K key) {
		final int j = index(key);
		final Entry<K, V> e = j < table.length ? table[j] : null;
		if (e != null) {
			table[j] = e.next;
		} else {
			throw new IllegalArgumentException(key + " is not present in the map, so cannot pop any item off its stack");			
		}
		if (e.next == null) {
			keyCount -= 1;
		}
	}	
	
	public void popIfNotLast(K key) {
		final int j = index(key);
		final Entry<K, V> e = j < table.length ? table[j] : null;
		if (e != null) {
			if (e.next != null) {
				table[j] = e.next;
			} else {
				table[j].depth -= 1;
			}
		} else {
			throw new IllegalArgumentException(key + " is not present in the map, so cannot pop any item off its stack");			
		}
	}	
	
    public static int rehash(int hash) {
        hash += (hash <<  15) ^ 0xffffcd7d;
        hash ^= (hash >>> 10);
        hash += (hash <<   3);
        hash ^= (hash >>>  6);
        hash += (hash <<   2) + (hash << 14);
        return hash ^ (hash >>> 16) ;
    }

    public final V apply(K k) {
    	return peek(k);
    }
    
	public V commonValue(K thisKey, StackMap<K, V> that, K thatKey) {
		Entry<K, V> thisHead = this.head(thisKey);
		Entry<K, V> thatHead = that.head(thatKey);
		for (Entry<K, V> i = thisHead ; i != null ; i = i.next) {
			for (Entry<K, V> j = thatHead ; j != null ; j = j.next) {
				if (i.value == j.value) {
					return i.value;
				}
			}
		}
		return null;
	}

}
