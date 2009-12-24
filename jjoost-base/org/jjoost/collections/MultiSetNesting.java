package org.jjoost.collections;

public class MultiSetNesting<V> {

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
