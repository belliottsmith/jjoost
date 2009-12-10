package org.jjoost.collections;

import org.jjoost.util.Factory;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehashers;

public class MultiMapNesting<V> {

	public static final class Type {
		public static final int INLINE = 1 ;
		public static final int NESTED = 2 ;
	}
	
	public int type() { return type ; }
	public Factory<ScalarSet<V>> factory() { return factory ; }
	private final int type ;
	private final Factory<ScalarSet<V>> factory ;
	protected MultiMapNesting(Factory<ScalarSet<V>> factory, int type) {
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
	
	public static <V> MultiMapNesting<V> nested(Factory<ScalarSet<V>> factory) {
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
