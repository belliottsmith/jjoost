package org.jjoost.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException ;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jjoost.util.tuples.Value;
import org.jjoost.collections.iters.ClosableIterator ;
import org.jjoost.collections.iters.MappedClosableIterator ;
import org.jjoost.collections.iters.MappedIterable ;
import org.jjoost.collections.iters.MappedIterator ;

public class Functions {

	// TODO : replace with concrete class definitions
    @SuppressWarnings("unchecked")
	private static final Function IDENTITY = 
		new Function() {
			private static final long serialVersionUID = 5801405085506068892L ;
			public Object apply(Object v) { return v ; } 
		} ;

	@SuppressWarnings("unchecked")
	public static final Function TO_STRING = 
			new Function<Object, String>() {
			private static final long serialVersionUID = 5801405085506068892L ;
			public String apply(Object v) { return v == null ? "null" : v.toString() ; } 
		} ;
		
	@SuppressWarnings("unchecked")
	public static <E> Function<E, E> identity() {
		return IDENTITY ;
	}
	
	@SuppressWarnings("unchecked")
	public static <E> Function<E, String> toString() {
		return TO_STRING ;
	}
	
	public static <E, F, G> Function<E, G> composition(final Function<E, F> f1, final Function<F, G> f2) {
		return new Function<E, G>() {
			private static final long serialVersionUID = 3725675651261795247L;
			@Override
			public G apply(E v) {
				return f2.apply(f1.apply(v)) ;
			}
		} ;
	}
	
	public static <E, F> Function<E, F> getMethodProjection(Class<E> clazz, String methodName, final Class<F> returnType) throws SecurityException, NoSuchMethodException {
		final Method m = clazz.getMethod(methodName, new Class[0]) ;
		final Object[] args = new Object[0] ;
		return new MethodProjection<E, F>(returnType, m, args) ;
	}
	
	public static final class MethodProjection<E, F> implements Function<E, F> {
		private static final long serialVersionUID = -2627623099906029633L ;
		final Class<F> returnType ;
		final Method m ;
		final Object[] args ;
		public MethodProjection(Class<F> returnType, Method m, Object[] args) {
			super();
			this.returnType = returnType;
			this.m = m;
			this.args = args;
		}
		public F apply(E v) {
			try {
				return returnType.cast(m.invoke(v, args)) ;
			} catch (IllegalAccessException e) {
				throw new UndeclaredThrowableException(e) ;
			} catch (InvocationTargetException e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}
	
	public static <E, F> Function<E, F> getFieldProjection(Class<E> clazz, String propertyName, final Class<F> propertyType) throws SecurityException, NoSuchFieldException {
		final Field f = clazz.getField(propertyName) ;
		if (!propertyType.isAssignableFrom(f.getType()))
			throw new IllegalArgumentException("") ;
		return new FieldProjection<E, F>(propertyType, f) ;
	}
	
	public static final class FieldProjection<E, F> implements Function<E, F> {
		private static final long serialVersionUID = -2627623099906029633L ;
		final Class<F> returnType ;
		final Field f ;
		public FieldProjection(Class<F> returnType, Field f) {
			super();
			this.returnType = returnType;
			this.f = f;
		}
		public F apply(E v) {
			try {
				return returnType.cast(f.get(v)) ;
			} catch (IllegalAccessException e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <V, E extends Entry<?, ? extends V>> Function<E, V> getMapEntryValueProjection() {
		return MAP_ENTRY_VALUE_PROJECTION ;
	}
	@SuppressWarnings("unchecked")
	private static final MapEntryValueProjection MAP_ENTRY_VALUE_PROJECTION = new MapEntryValueProjection() ; 
	public static final class MapEntryValueProjection<E extends Entry<?, V>, V> implements Function<E, V> {
		private static final long serialVersionUID = -2627623099906029633L ;
		public V apply(E v) { return v.getValue() ; }
	}
	
	@SuppressWarnings("unchecked")
	public static <K, E extends Entry<? extends K, ?>> Function<E, K> getMapEntryKeyProjection() {
		return MAP_ENTRY_KEY_PROJECTION ;
	}
	@SuppressWarnings("unchecked")
	private static final MapEntryValueProjection MAP_ENTRY_KEY_PROJECTION = new MapEntryValueProjection() ; 
	public static final class MapEntryKeyProjection<E extends Entry<K, ?>, K> implements Function<E, K> {
		private static final long serialVersionUID = -2627623099906029633L ;
		public K apply(E v) { return v.getKey() ; }
	}
	
	@SuppressWarnings("unchecked")
	public static <V> Function<Value<V>, V> getAbstractValueContentsProjection() {
		return ABSTRACT_VALUE_PROJECTION ;
	}
	@SuppressWarnings("unchecked")
	private static final AbstractValueProjection ABSTRACT_VALUE_PROJECTION = new AbstractValueProjection() ; 
	public static final class AbstractValueProjection<E extends Value<V>, V> implements Function<E, V> {
		private static final long serialVersionUID = -2627623099906029633L ;
		public V apply(E v) { return v.getValue() ; }
	}


    /**
     * performs f.f() on every element of list, and returns it as a new list
     * 
     * @param <E>
     * @param <F>
     * @param list
     * @param f
     * @return
     */
    public static <E, F> List<F> apply(Function<? super E, ? extends F> f, List<? extends E> list) {
    	List<F> ret = new ArrayList<F>(list.size()) ;
    	for (E e : list) ret.add(f.apply(e)) ;
    	return ret ;
    }
    
    /**
     * performs f.f() on every element of list, and returns it as a new list
     * 
     * @param <E>
     * @param <F>
     * @param list
     * @param f
     * @return
     */
    public static <E, F> List<F> apply(List<? extends E> list, Function<? super E, ? extends F> f) {
        return apply(f, list) ;
    }
    
    /**
     * performs f.f() on every element of list, and returns it as a new list
     * 
     * @param <E>
     * @param <F>
     * @param iter
     * @param f
     * @return
     */
    public static <E, F> Iterable<F> apply(Function<? super E, ? extends F> f, Iterable<? extends E> iter) {
    	return new MappedIterable<E, F>(iter, f) ;
    }
    
    /**
     * performs f.f() on every element of list, and returns it as a new list
     * 
     * @param <E>
     * @param <F>
     * @param list
     * @param f
     * @return
     */
    public static <E, F> Iterable<F> apply(Iterable<? extends E> list, Function<? super E, ? extends F> f) {
    	return apply(f, list) ;
    }
    
    /**
     * performs f.f() on every element of list, and returns it as a new list
     * 
     * @param <E>
     * @param <F>
     * @param list
     * @param f
     * @return
     */
    public static <E, F> Iterator<F> apply(Iterator<? extends E> list, Function<? super E, ? extends F> f) {
    	return apply(f, list) ;
    }

    /**
     * performs f.f() on every element of list, and returns it as a new list
     * 
     * @param <E>
     * @param <F>
     * @param iter
     * @param f
     * @return
     */
    public static <E, F> Iterator<F> apply(Function<? super E, ? extends F> f, Iterator<? extends E> iter) {
    	return new MappedIterator<E, F>(iter, f) ;
    }
    
    /**
     * performs f.f() on every element of list, and returns it as a new list
     * 
     * @param <E>
     * @param <F>
     * @param list
     * @param f
     * @return
     */
    public static <E, F> ClosableIterator<F> apply(ClosableIterator<? extends E> list, Function<? super E, ? extends F> f) {
    	return apply(f, list) ;
    }

    /**
     * performs f.f() on every element of list, and returns it as a new list
     * 
     * @param <E>
     * @param <F>
     * @param iter
     * @param f
     * @return
     */
    public static <E, F> ClosableIterator<F> apply(Function<? super E, ? extends F> f, ClosableIterator<? extends E> iter) {
    	return new MappedClosableIterator<E, F>(iter, f) ;
    }
    

}
