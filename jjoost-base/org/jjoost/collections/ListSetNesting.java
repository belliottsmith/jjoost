package org.jjoost.collections;

public class ListSetNesting<V> {

	public static final class Type {
		public static final int INLINE = 1 ;
		public static final int COUNTING = 2 ;
		public static final int NESTED = 3 ;
	}
	
	public int type() { return type ; }
	private final int type ;
	public ListSetNesting(int type) {
		super();
		this.type = type;
	}
	
	@SuppressWarnings("unchecked")
	private static final ListSetNesting INLINE = new ListSetNesting(Type.INLINE) ;
	
	@SuppressWarnings("unchecked")
	private static final ListSetNesting COUNTING = new ListSetNesting(Type.COUNTING) ;
	
	@SuppressWarnings("unchecked")
	private static final ListSetNesting NESTED = new ListSetNesting(Type.NESTED) ;
	
	@SuppressWarnings("unchecked")
	public static <V> ListSetNesting<V> inline() {
		return INLINE ;
	}
	
	@SuppressWarnings("unchecked")
	public static <V> ListSetNesting<V> counting() {
		return COUNTING ;
	}
	
	@SuppressWarnings("unchecked")
	public static <V> ListSetNesting<V> nested() {
		return NESTED ;
	}
	
}
