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
		
		INLINE, COUNTING, NESTED ;
	}
	
	public Type type() { return type ; }
	private final Type type ;
	public MultiSetNesting(Type type) {
		super();
		this.type = type;
	}
	
	@SuppressWarnings("unchecked")
	private static final MultiSetNesting INLINE = new MultiSetNesting(Type.INLINE) ;
	
	@SuppressWarnings("unchecked")
	private static final MultiSetNesting COUNTING = new MultiSetNesting(Type.COUNTING) ;
	
	@SuppressWarnings("unchecked")
	private static final MultiSetNesting NESTED = new MultiSetNesting(Type.NESTED) ;
	
	@SuppressWarnings("unchecked")
	public static <V> MultiSetNesting<V> inline() {
		return INLINE ;
	}
	
	@SuppressWarnings("unchecked")
	public static <V> MultiSetNesting<V> counting() {
		return COUNTING ;
	}
	
	@SuppressWarnings("unchecked")
	public static <V> MultiSetNesting<V> nested() {
		return NESTED ;
	}
	
}
