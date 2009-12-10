package org.jjoost.collections;

import org.jjoost.util.Factory;

public class ListMapNesting<V> {

	public static final class Type {
		public static final int INLINE = 1 ;
		public static final int NESTED = 2 ;
	}
	
	public int type() { return type ; }
	public Factory<ListSet<V>> factory() { return factory ; }
	private final int type ;
	private final Factory<ListSet<V>> factory ;
	protected ListMapNesting(Factory<ListSet<V>> factory, int type) {
		super();
		this.factory = factory ;
		this.type = type ;
	}


	@SuppressWarnings("unchecked")
	private static final ListMapNesting INLINE = new ListMapNesting(null, Type.INLINE) ;
	
	@SuppressWarnings("unchecked")
	public static <V> ListMapNesting<V> inline() {
		return INLINE ;
	}
	
	public static <V> ListMapNesting<V> nested(Factory<ListSet<V>> factory) {
		if (factory == null)
			throw new IllegalArgumentException() ;
		return new ListMapNesting<V>(factory, Type.NESTED) ;
	}
	
//	public static <V> ListMapNesting<V> nestedList() {
//		return ListMapNesting.nested(
//				SetMaker.<V>hash()
//					.initialCapacity(2)
//					.hasher(Hashers.rehashing(
//						Hashers.object(), 
//						Rehashers.jdkConcurrentHashmapRehasher())
//				).newListSetFactory(ListSetNesting.inline())) ;
//	}
	
}
