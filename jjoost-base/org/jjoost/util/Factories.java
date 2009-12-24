package org.jjoost.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class Factories {

	public static <E> Factory<E> forClass(Class<E> clazz) throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		return new SimpleObjectFactory<E>(clazz) ;
	}
	
	public static <E, F> Factory<F> cast(Factory<E> factory) {
		return new CastFactory<F>(factory) ;
	}
	
	/**
	 * A simple factory that creates objects of the type of the class provided, providing it has an empty constructor
	 * 
	 * @author b.elliottsmith
	 * @param <E>
	 */
	public static final class SimpleObjectFactory<E> implements Factory<E> {
		private static final long serialVersionUID = 8830755652916045329L ;
		private final Constructor<E> constructor ;
		private final Object[] args ;
		private static final Class<?>[] getClasses(Object[] args) {
			final Class<?>[] classargs = new Class[args.length] ;
			for (int i = 0 ; i != args.length ; i++) {
				classargs[i] = args[i].getClass() ;
			}
			return classargs ;
		}
		public SimpleObjectFactory(Class<E> clazz) throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
			this(clazz, new Class[0], new Object[0]) ;
		}
		public SimpleObjectFactory(Class<E> clazz, Object ... args) throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
			this(clazz, getClasses(args), args) ;
		}
		public SimpleObjectFactory(Class<E> clazz, Class<?>[] classargs, Object[] args) throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
			super() ;
			this.constructor = clazz.getConstructor(classargs) ;
			try {
				this.constructor.setAccessible(true) ;
			} catch (Exception e) {
			}
			constructor.newInstance(args) ; // test to make sure constructor at least nominally works (can't guarantee it will always work)
			this.args = args.clone() ;
		}
		public E create() {
			try {
				return constructor.newInstance(args) ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}

	/**
	 * A simple factory which wraps another factory (ordinarily a SimpleObjectFactory) and 
	 * casts the result to the type parameter provided
	 * 
	 * @author b.elliottsmith
	 *
	 * @param <E>
	 */
	public static final class CastFactory<E> implements Factory<E> {
		private static final long serialVersionUID = 8830755652916045329L ;
		private final Factory<?> wrapped ;
		public CastFactory(Factory<?> wrapped) {
			this.wrapped = wrapped ;
		}
		@SuppressWarnings("unchecked")
		public E create() {
			try {
				return (E) wrapped.create() ;
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e) ;
			}
		}
	}
	
}
