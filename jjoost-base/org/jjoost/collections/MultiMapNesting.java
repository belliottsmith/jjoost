package org.jjoost.collections;

import org.jjoost.util.Factory;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehashers;

public class MultiMapNesting<V> {

	public static enum Type {
		INLINE, NESTED ;
	}
	
	public Type type() { return type ; }
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
	
	@SuppressWarnings("unchecked")
	public static <V> MultiMapNesting<V> inline() {
		return INLINE ;
	}
	
	public static <V> MultiMapNesting<V> nested(Factory<Set<V>> factory) {
		if (factory == null)
			throw new IllegalArgumentException() ;
		return new MultiMapNesting<V>(factory, Type.NESTED) ;
	}
	
	public static <V> MultiMapNesting<V> nestedHash() {
		return MultiMapNesting.nested(
				SetMaker.<V>hash()
					.initialCapacity(2)
					.hasher(Hashers.rehashing(
						Hashers.object(), 
						Rehashers.jdkConcurrentHashmapRehasher())
				).newScalarSetFactory()) ;
	}
	
}
