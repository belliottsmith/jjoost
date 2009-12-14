package org.jjoost.collections;

public class MultiSetNesting<V> {

	public static final class Type {
		public static final int INLINE = 1 ;
		public static final int COUNTING = 2 ;
		public static final int NESTED = 3 ;
	}
	
	public int type() { return type ; }
	private final int type ;
	public MultiSetNesting(int type) {
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
