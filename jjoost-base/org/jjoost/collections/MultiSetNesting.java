package org.jjoost.collections;

/**
 * This class encapsulates the concept of nesting for objects that implement <code>MultiSet</code>.
 * Three kinds of nesting are supported: <code>INLINE</code>, <code>COUNTING</code> and <code>NESTED</code>. 
 * <code>INLINE</code> indicates that the map should be structurally the same as if it did not support duplicates,
 * only that duplicate records are handled correctly. i.e. an <code>INLINE MultiSet</code> that
 * contains no duplicate keys should look structurally identical to a regular <code>Set</code>.
 * A <code>COUNTING MutliSet</code>, however, should store an integer against each unique key, storing each
 * key just once. A <code>NESTED</code> set should store each key just once, and store a list of each duplicate
 * against that key.
 */
public class MultiSetNesting<V> {

	/**
	 * An enumeration of the nesting types for MultiSet
	 * 
	 * @author b.elliottsmith
	 */
	public static enum Type {		
		/**
		 * Store duplicate values merely as an integer against the first such value encountered
		 * This is the most efficient means of storing duplicate values.
		 */
		COUNTING, 
		/**
		 * Store duplicate values "inline" - i.e. in the same structure as we would store unique values
		 * Typically this will be more efficient than <code>NESTED</code> for very small numbers of duplicates
		 */
		INLINE,
		/**
		 * Store duplicate values in a nested set or list. 
		 * Typically this will be more efficient than <code>INLINE</code> for moderate to large numbers of duplicates (e.g. above 3 on average)
		 */
		NESTED ;
	}
	
	/**
	 * Return the MultiSetNesting.Type type of this MultiSetNesting
	 * @return the MultiSetNesting.Type type of this MultiSetNesting
	 */
	public Type type() { return type ; }
	private final Type type ;
	MultiSetNesting(Type type) {
		super();
		this.type = type;
	}
	
	@SuppressWarnings("unchecked")
	private static final MultiSetNesting INLINE = new MultiSetNesting(Type.INLINE) ;
	
	@SuppressWarnings("unchecked")
	private static final MultiSetNesting COUNTING = new MultiSetNesting(Type.COUNTING) ;
	
	@SuppressWarnings("unchecked")
	private static final MultiSetNesting NESTED = new MultiSetNesting(Type.NESTED) ;
	
	/**
	 * Return a MultiSetNesting whose type is INLINE
	 * @return a MultiSetNesting whose type is INLINE
	 */
	@SuppressWarnings("unchecked")
	public static <V> MultiSetNesting<V> inline() {
		return INLINE ;
	}

	/**
	 * Return a MultiSetNesting whose type is COUNTING
	 * @return a MultiSetNesting whose type is COUNTING
	 */
	@SuppressWarnings("unchecked")
	public static <V> MultiSetNesting<V> counting() {
		return COUNTING ;
	}
	
	/**
	 * Return a MultiSetNesting whose type is NESTED
	 * @return a MultiSetNesting whose type is NESTED
	 */
	@SuppressWarnings("unchecked")
	public static <V> MultiSetNesting<V> nested() {
		return NESTED ;
	}
	
}
