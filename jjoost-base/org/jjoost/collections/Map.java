package org.jjoost.collections;

import java.util.Map.Entry ;

import org.jjoost.util.Factory;
import org.jjoost.util.Function;

/**
 * This interface is the 
 */
public interface Map<K, V> extends AnyMap<K, V>, Function<K, V> {

	/**
	 * Ensures that the provided key binds to the provided value, removing and
	 * returning the value currently associated with the key, or null if none.
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return the value of any maplet removed as a result of this action
	 */
	public V put(K key, V val) ;
	
	/**
	 * Attempts to bind the provided key to the provided value. If the key does not occur
	 * in the map then the value will be associated with it and null returned. If the key
	 * occurs in the map and is bound to a different value then this existing value will 
	 * be returned
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return the value already associated with the key in the map, or null if none
	 */
	public V putIfAbsent(K key, V val) ;
	
	/**
	 * Equivalent to <code>putIfAbsent(key, putIfNotPresent.create())</code>, except that 
	 * <code>putIfNotPresent.create()</code> is only executed if there is no key associated
	 * with the value. In concurrent maps this is not a guarantee, but a best effort,
	 * as it is possible for another thread to set a value for the key after this has executed
	 * but before the record can be inserted.
	 * 
	 * @param key the key
	 * @param putIfNotPresent the put if not present
	 * 
	 * @return the value associated with the provided key pre method
	 */
	public V putIfAbsent(K key, Function<? super K, ? extends V> putIfNotPresent) ;
	
	/**
	 * Equivalent to putIfAbsent(key, putIfNotPresent), except that instead of returning
	 * the value previously associated with the key, returns the value associated with the
	 * key as the method is exiting; i.e. if a new value is associated with the key as a
	 * result of this method, this new value will be returned, otherwise the existing value
	 * will be
	 * 
	 * @param key the key
	 * @param putIfNotPresent the put if not present
	 * 
	 * @return the value associated with the provided key post method
	 */
	public V ensureAndGet(K key, Factory<? extends V> putIfNotPresent) ;
	
	/**
	 * Equivalent to putIfAbsent(key, putIfNotPresent.create(key)), except that <br />
	 * 
	 * <ol>
	 * <li><code>putIfNotPresent.create()</code> is only executed if there is no
	 * key associated with the value. In concurrent maps this is not a
	 * guarantee, but a best effort, as it is possible for another thread to set
	 * a value for the key after this has executed but before the record can be
	 * inserted</li>
	 * <li>instead of returning the value previously associated with the key,
	 * returns the value associated with the key as the method is exiting; i.e.
	 * if a new value is associated with the key as a result of this method,
	 * this new value will be returned, otherwise the existing value will be
	 * <p></li>
	 * </ol>
	 * 
	 * @param key the key
	 * @param putIfNotPresent put if not present
	 * @return the value associated with the key post method
	 */
	public V ensureAndGet(K key, Function<? super K, ? extends V> putIfNotPresent) ;
	
	/**
	 * A convenience method, equivalent to first(key)
	 * 
	 * @param key the key
	 * 
	 * @return the value associated with the key, or null if none
	 */
	public V get(K key) ;

	/**
	 * A convenience method, equivalent to both <code>totalCount()</code> and <code>uniqueKeyCount()</code>
	 * 
	 * @return the int
	 */
	public int size() ;
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#values(java.lang.Object)
	 */
	@Override public UnitarySet<V> values(K key) ;

	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#copy()
	 */
	@Override public Map<K, V> copy() ;
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#entries()
	 */
	@Override public Set<Entry<K, V>> entries() ;
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#keys()
	 */
	@Override public Set<K> keys() ;
	
}
