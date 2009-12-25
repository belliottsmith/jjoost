package org.jjoost.collections;

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

/**
 * This is the common super interface for all Jjoost maps.
 * <p>
 * The methods declared here make no assumptions about the number of occurences
 * of a given key or key->value pair, and declare no actions that may modify the
 * map, so is the most general form of map. No concrete class should implement
 * this interface directly.
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
public interface AnyReadMap<K, V> extends Serializable {

	/**
	 * Returns true iff a key occurs in the map which is equal to the parameter,
	 * as determined by the map's definition of equality.
	 * 
	 * @param key find
	 * 
	 * @return true, if present
	 */
	public boolean contains(K key) ;	
	
	/**
	 * Returns true iff a (key,value) pair occurs in the map that is equal to those
	 * provided, as determined by the map's definition(s) of equality.
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return true, if present
	 */
	public boolean contains(K key, V val) ;
	
	/**
	 * Returns the number of occurrences of keys present in the map that are equal to the 
	 * one provided, as determined by the map's definition of equality.
	 * 
	 * @param key the key
	 * 
	 * @return number of occurrences
	 */
	public int count(K key) ;	
	
	/**
	 * Returns the number of occurrences of (key, value) pairs present in the map that are 
	 * equal to the one provided, as determined by the map's definition(s) of equality.
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return number of occurrences
	 */
	public int count(K key, V val) ;
	
	/**
	 * Returns the value associated with the first key that is equal to the one provided,
	 * as determined by the map's definition of equality. Returns <code>null</code> if no matching key
	 * is found.
	 * 
	 * @param key the key
	 * 
	 * @return the value associated with the first matching key
	 */
	public V first(K key) ;	
	
	/**
	 * Returns a set representing the values associated with the provided key in
	 * this map. This set should always reflect changes to the map, and changes
	 * to the set should be reflected in the map also. Otherwise, this set should
	 * behave exactly as a regular set does.
	 * <p>
	 * Note that in a regular (scalar) map the set returned will be a
	 * <code>UnitarySet</code>, which contains at most one value.
	 * <code>put()</code> operations on such a set will override any existing
	 * value regardless of if it is equal to the one already present.
	 * 
	 * @return the set of values mapped to by provided key
	 */
	public AnyReadSet<V> values(K key) ;
	
	/**
	 * Returns a <code>List</code> of all values in the map which are mapped to by 
	 * the provided key, as determined by the map's definition of equality. This list
	 * should be constructed "eagerly" and should be a consistent snapshot of 
	 * the values valid at some point between the method being called and it returning.
	 * 
	 * @param key the key
	 * 
	 * @return the list< v>
	 */
	public List<V> list(K key) ;
	
	/**
	 * Returns a set representing all the keys in the domain of this map. In a
	 * <code>MultiMap</code> or <code>ListMap</code> this will be a
	 * <code>MultiSet</code>. This set should always reflect changes to the map,
	 * and changes to the set should be reflected in the map also. Otherwise,
	 * this set should behave exactly as a regular set does. The key equality used
	 * by this map can be obtained from this set.
	 * <p>
	 * Note that the <code>put()</code> methods on this set will always fail,
	 * because no value can be provided to update the map with.
	 * 
	 * @return the key set< k>
	 */
	public AnyReadSet<K> keys() ;
	
	/**
	 * Returns a set representing all the key->value pairs in this map. In a
	 * <code>ListMap</code> this will be a <code>MultiSet</code>. This set
	 * should always reflect changes to the map, and changes to the set should
	 * be reflected in the map also. Otherwise, this set should behave exactly
	 * as a regular set does.
	 * 
	 * @return the entry set< k>
	 */
	public AnyReadSet<? extends Entry<K, V>> entries() ;	
	
	/**
	 * Returns a set representing the range of the map. Operations on this
	 * set will typically be expensive (O(n) where n is the size of the map).
	 * The value equality used by this map can be obtained from this set.
	 * 
	 * @return the range of the map
	 */
	public AnyReadSet<V> values() ;	
	
	/**
	 * Returns an <code>Iterable</code> over all key->value maplets in the map
	 * whose key is equal to the one provided, as determined by the map's
	 * definition of equality.
	 * 
	 * @param key find
	 * 
	 * @return maplets with the provided key
	 */
	public Iterable<Entry<K, V>> entries(K key) ;
	
	/**
	 * Return an integer representing the total number of maplets (i.e. counting
	 * all occurrences of each key) in the map
	 * 
	 * @return the total number of maplets
	 */
	public int totalCount() ;
	
	/**
	 * Return an integer representing the number of unique keys in the domain of
	 * the map
	 * 
	 * @return number of unique keys
	 */
	public int uniqueKeyCount() ;
	
	/**
	 * Returns true iff the map is empty
	 * 
	 * @return true, if empty
	 */
	public boolean isEmpty() ;
	
	/**
	 * Returns true if a key can map to more than one value
	 * 
	 * @return true, if keys can map to more than one value
	 */
	public boolean permitsDuplicateKeys() ;

}
