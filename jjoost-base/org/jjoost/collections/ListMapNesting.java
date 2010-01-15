package org.jjoost.collections;

import org.jjoost.util.Factory;

/**
 * This class encapsulates the concept of nesting for objects that implement <code>ListMap</code>.
 * Two kinds of nesting are supported: <code>INLINE</code> and <code>NESTED</code>. <code>INLINE</code>
 * indicates that the map should be structurally the same as if it did not support duplicates,
 * only that duplicate records are handled correctly. i.e. an <code>INLINE ListMap</code> that
 * contains no duplicate keys should look structurally identical to a regular <code>Map</code>.
 * A <code>NESTED ListMap</code>, however, should store a <code>MultiSet</code> against every
 * single key. If <code>NESTED</code>, a <code>ListMapNesting</code> requires a <code>Factory<MultiSet<V>></code>
 * in order to produce each <code>MultiSet</code> we store against each key. 
 */
public class ListMapNesting<V> {

	/**
	 * An enumeration of the nesting types for ListMap
	 */
	public static enum Type {
		/**
		 * Store the values for duplicates keys simply as extra entries using the same mechanism as an equivalent Map would use to
		 * store its values. Typically this will be more efficient than <code>NESTED</code> for small numbers of duplicate keys.
		 */
		INLINE,
		/**
		 * Store the values for duplicates keys in a nested <code>MultiSet</code>. Typically this will be more efficient than
		 * <code>INLINE</code> for moderate to large average numbers of duplicate keys. 
		 */
		NESTED ;
	}
	
	/**
	 * INLINE or NESTED	 * 
	 * @return the type
	 */
	public Type type() { return type ; }
	
	/**
	 * Factory; ignored if type() == INLINE
	 * 
	 * @return null if type() == INLINE, otherwise a factory
	 */
	public Factory<MultiSet<V>> factory() { return factory ; }
	
	private final Type type ;
	private final Factory<MultiSet<V>> factory ;
	
	/**
	 * Instantiates a new list map nesting.
	 * 
	 * @param factory the factory
	 * @param type the type
	 */
	protected ListMapNesting(Factory<MultiSet<V>> factory, Type type) {
		super();
		this.factory = factory ;
		this.type = type ;
	}


	/** The Constant INLINE */
	@SuppressWarnings("unchecked")
	private static final ListMapNesting INLINE = new ListMapNesting(null, Type.INLINE) ;
	
	/**
	 * Public method for retrieving the <code>ListMapNesting</code> that represents <code>INLINE</code> nesting
	 * 
	 * @return the list map nesting
	 */
	@SuppressWarnings("unchecked")
	public static <V> ListMapNesting<V> inline() {
		return INLINE ;
	}
	
	/**
	 * Public method for retrieving a <code>ListMapNesting</code> to represent the required <code>NESTED</code> nesting
	 * 
	 * @param factory the factory	 * 
	 * @return the list map nesting
	 */
	public static <V> ListMapNesting<V> nested(Factory<MultiSet<V>> factory) {
		if (factory == null)
			throw new IllegalArgumentException() ;
		return new ListMapNesting<V>(factory, Type.NESTED) ;
	}
	
}
