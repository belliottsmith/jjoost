package org.jjoost.collections;

import org.jjoost.util.Factory;

public class ListMapNesting<V> {

	public static enum Type {
		INLINE , NESTED ;
	}
	
	public Type type() { return type ; }
	public Factory<MultiSet<V>> factory() { return factory ; }
	private final Type type ;
	private final Factory<MultiSet<V>> factory ;
	protected ListMapNesting(Factory<MultiSet<V>> factory, Type type) {
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
	
	public static <V> ListMapNesting<V> nested(Factory<MultiSet<V>> factory) {
		if (factory == null)
			throw new IllegalArgumentException() ;
		return new ListMapNesting<V>(factory, Type.NESTED) ;
	}
	
}
