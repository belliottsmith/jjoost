package org.jjoost.collections;

import org.jjoost.util.Equalities;
import org.jjoost.util.Factory;
import org.jjoost.util.Rehashers;

/**
 * This class encapsulates the concept of nesting for objects that implement <code>MultiMap</code>.
 * Two kinds of nesting are supported: <code>INLINE</code> and <code>NESTED</code>. <code>INLINE</code>
 * indicates that the map should be structurally the same as if it did not support duplicates,
 * only that duplicate records are handled correctly. i.e. an <code>INLINE MultiMap</code> that
 * contains no duplicate keys should look structurally identical to a regular <code>Map</code>.
 * A <code>NESTED MultiMap</code>, however, should store a <code>Set</code> against every
 * single key. If <code>NESTED</code>, a <code>MultiMapNesting</code> requires a <code>Factory<Set<V>></code>
 * in order to produce each <code>MultiSet</code> we store against each key. 
 */
public class MultiMapNesting<V> {

	/**
	 * An enumeration of the nesting types for MultiMap
	 */
	public static enum Type {
		INLINE, NESTED ;
	}
	
	/**
	 * INLINE or NESTED
	 * @return the type
	 */
	public Type type() { return type ; }
	
	/**
	 * Factory; ignored if type() == INLINE
	 * 
	 * @return null if type() == INLINE, otherwise a factory
	 */
	public Factory<Set<V>> factory() { return factory ; }
	
	private final Type type ;
	private final Factory<Set<V>> factory ;
	
	protected MultiMapNesting(Factory<Set<V>> factory, Type type) {
		super();
		this.factory = factory;
		this.type = type ;
	}


	@SuppressWarnings("unchecked")
	private static final MultiMapNesting INLINE = new MultiMapNesting(null, Type.INLINE) ;
	
	/**
	 * Public method for retrieving the <code>MultiMapNesting</code> that represents <code>INLINE</code> nesting
	 * 
	 * @return the multi map nesting
	 */
	@SuppressWarnings("unchecked")
	public static <V> MultiMapNesting<V> inline() {
		return INLINE ;
	}
	
	/**
	 * Public method for retrieving a <code>MultiMapNesting</code> to represent the required <code>NESTED</code> nesting
	 * 
	 * @param factory the factory	  
	 * @return the multi map nesting
	 */
	public static <V> MultiMapNesting<V> nested(Factory<Set<V>> factory) {
		if (factory == null)
			throw new IllegalArgumentException() ;
		return new MultiMapNesting<V>(factory, Type.NESTED) ;
	}
	
	public static <V> MultiMapNesting<V> nestedHash() {
		return MultiMapNesting.nested(
				SetMaker.<V>hash()
					.initialCapacity(2)
					.equality(Equalities.rehashing(
						Equalities.object(), 
						Rehashers.jdkConcurrentHashmapRehasher())
				).newSetFactory()) ;
	}
	
}
