package org.jjoost.collections;

import java.util.Map.Entry;

import org.jjoost.util.Function;

public interface ReadMap<K, V> extends Function<K, V>, AnyReadMap<K, V> {

	/**
	 * A convenience method, equivalent to first(key)
	 * 
	 * @param key the key
	 * 
	 * @return the value associated with the key, or null if none
	 */
	public abstract V get(K key);

	/**
	 * A convenience method, equivalent to both <code>totalCount()</code> and <code>uniqueKeyCount()</code>
	 * 
	 * @return the int
	 */
	public abstract int size();

	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#values(java.lang.Object)
	 */
	public abstract UnitarySet<V> values(K key);

	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#copy()
	 */
	public abstract ReadMap<K, V> copy();

	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#entries()
	 */
	public abstract Set<Entry<K, V>> entries();

	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#keys()
	 */
	public abstract Set<K> keys();

}