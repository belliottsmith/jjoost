package org.jjoost.util;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException ;

/**
 * A simple Factory interface that defines a create() method for instantiating some object
 * 
 * @author b.elliottsmith
 * @param <E>
 */
public interface Factory<E> extends Serializable {

	/**
	 * a thread safe method to create a new object of type E
	 * 
	 * @return a new object of type E
	 */
	E create() ;
	
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
	
	public static final class CastFactory<E> implements Factory<E> {
		private static final long serialVersionUID = 8830755652916045329L ;
		private final Factory<?> wrapped ;
		public CastFactory(Factory<?> wrapped) throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
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
