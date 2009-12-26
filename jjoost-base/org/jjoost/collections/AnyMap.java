package org.jjoost.collections;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * This is the common super interface to all modifiable Jjoost maps. The methods 
 * declared here make no assumptions about the number of occurrences
 * of a given key or key->value pair.
 * <p>
 * WARNING: Note that in a <code>MultiMap</code> and <code>ListMap</code> the
 * <code>keys()</code> method returns a <code>MultiSet</code> and as such an
 * <code>Iterator</code> over this will yield each duplicate key the number of
 * times it occurs in the map. For unique keys, call
 * <code>keys().unique()</code>
 * <p>
 * Also note that an <code>Iterator</code> returned by concurrent implementors
 * of this class is permitted to return values more times than they actually
 * ever occurred <b>if a valid sequence of deletes and inserts happens</b> to
 * cause the <code>Iterator</code> to see the values multiple times. See the
 * javadoc of the implementing classes to determine their behaviour in this
 * case.
 * 
 * @author b.elliottsmith
 */
public interface AnyMap<K, V> extends AnyReadMap<K, V> {

	/**
	 * Ensures that the provided key binds to the provided value at least once;
	 * depending on the underlying implementation this may remove any existing 
	 * key->value pairs where the key is equal to the one provided (<code>Map</code>),
	 * the key and value are both equal (<code>MultiMap</code>), or simply append
	 * this key to all existing maplets regardless of their equality (<code>ListMap</code>).
	 * If any pair is removed as a result of this action, the value of that pair is returned.
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return the value of any maplet removed as a result of this action
	 */
	public V put(K key, V val) ;
	
	/**
	 * Put if absent.
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return the v
	 */
	public V putIfAbsent(K key, V val) ;
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#values(java.lang.Object)
	 */
	@Override public AnySet<V> values(K key) ;
	
	/**
	 * Removes the.
	 * 
	 * @param key the key
	 * 
	 * @return the int
	 */
	public int remove(K key) ;	
	
	/**
	 * Removes the.
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return the int
	 */
	public int remove(K key, V val) ;
	
	/**
	 * Removes the and return.
	 * 
	 * @param key the key
	 * 
	 * @return the iterable< entry< k, v>>
	 */
	public Iterable<Entry<K, V>> removeAndReturn(K key) ;	
	
	/**
	 * Removes the and return.
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return the iterable< entry< k, v>>
	 */
	public Iterable<Entry<K, V>> removeAndReturn(K key, V val) ;
	
	/**
	 * Removes the and return first.
	 * 
	 * @param key the key
	 * 
	 * @return the v
	 */
	public V removeAndReturnFirst(K key) ;	
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#keys()
	 */
	@Override public AnySet<K> keys() ;
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#entries()
	 */
	@Override public AnySet<Entry<K, V>> entries() ;

	/**
	 * Clear.
	 * 
	 * @return the int
	 */
	public int clear() ;
	
	/**
	 * Clear and return.
	 * 
	 * @return the iterator< entry< k, v>>
	 */
	public Iterator<Entry<K, V>> clearAndReturn() ;

	/**
	 * Shrink.
	 */
	public void shrink() ;
	
	/**
	 * Copy.
	 * 
	 * @return the any map< k, v>
	 */
	public AnyMap<K, V> copy() ;
	
	/**
	 * Inverse.
	 * 
	 * @return the any map< v, k>
	 */
	public AnyMap<V, K> inverse() ;

	/**
	 * Returns a set representing the range of the map. Operations on this
	 * set will typically be expensive (O(n) where n is the size of the map).
	 * 
	 * @return the range of the map
	 */
	public AnySet<V> values() ;	
	
}
