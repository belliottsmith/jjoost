package org.jjoost.collections;

/**
 * This set represents the rather dubiously concept of a set consistent
 * of precisely one item. This set is particularly of note for the map
 * method <code>values(key)</code> on regular (scalar) maps.
 * 
 * @author b.elliottsmith
 *
 * @param <V>
 */
public interface UnitarySet<V> extends AnySet<V> {

	/**
	 * Insert the parameter into the set, removing and returning the currently value
	 * that exists in the set, if any
	 * 
	 * @param value to insert
	 * @return set's previous value, if any
	 */
	@Override public V put(V val) ;
	
	/**
	 * Returns the set's only value, if any
	 * 
	 * @return the set's value, or null if empty
	 */
	public V get() ;

	/**
	 * Returns a copy of the set. This method may or may not return a set of the same class as the one it was
	 * called on, however will return a <code>UnitarySet</code>
	 */
	@Override public UnitarySet<V> copy() ;
	
}
