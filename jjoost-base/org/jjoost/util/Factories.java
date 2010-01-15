package org.jjoost.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * A class providing simple default factories and methods that act upon them
 * 
 * @author b.elliottsmith
 */
public class Factories {

	/**
	 * Returns a new <code>SimpleObjectFactory</code> which constructs objects of type <code>clazz</code> with its no-args constructor
	 * @param clazz
	 *            the type of object to create
	 * @return a new <code>SimpleObjectFactory</code> which constructs objects of type <code>clazz</code> with its no-args constructor
		 * @throws SecurityException if clazz.getConstructor throws a SecurityException
		 * @throws NoSuchMethodException if a matching constructor is not found
		 * @throws InstantiationException if an object cannot be constructed using the found constructor
		 * @throws IllegalAccessException if the constructor's access is restricted
		 * @throws InvocationTargetException if the constructor throws an exception
	 */
	public static <E> Factory<E> forClass(Class<E> clazz) throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		return new SimpleObjectFactory<E>(clazz) ;
	}
	
	/**
	 * Creates a factory which delegates to the provided factory and (unsafely) casts its result to the type <code>F</code>
	 * @param <E> type of provided factory
	 * @param <F> type to unsafely cast to
	 * @param factory factory to delegate to
	 * @return </p>
	 */
	public static <E, F> Factory<F> cast(Factory<E> factory) {
		return new CastFactory<F>(factory) ;
	}
	
	/**
	 * A simple factory that creates objects of the type of the class provided, providing it has an empty constructor
	 * 
	 * @author b.elliottsmith
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
		/**
		 * Construct a new SimpleObjectFactory for the no-args constructor of the provided type
		 * @param clazz the type to construct
		 * @throws SecurityException if clazz.getConstructor throws a SecurityException
		 * @throws NoSuchMethodException if a matching constructor is not found
		 * @throws InstantiationException if an object cannot be constructed using the found constructor
		 * @throws IllegalAccessException if the constructor's access is restricted
		 * @throws InvocationTargetException if the constructor throws an exception
		 */
		public SimpleObjectFactory(Class<E> clazz) throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
			this(clazz, new Class[0], new Object[0]) ;
		}
		/**
		 * Construct a new SimpleObjectFactory for the constructor that matches the types of provided arguments (arguments must be non-null)
		 * @param clazz the type to construct
		 * @param args the arguments to use for the constructor
		 * @throws SecurityException if clazz.getConstructor throws a SecurityException
		 * @throws NoSuchMethodException if a matching constructor is not found
		 * @throws InstantiationException if an object cannot be constructed using the found constructor
		 * @throws IllegalAccessException if the constructor's access is restricted
		 * @throws InvocationTargetException if the constructor throws an exception
		 */
		public SimpleObjectFactory(Class<E> clazz, Object ... args) throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
			this(clazz, getClasses(args), args) ;
		}
		/**
		 * Construct a new SimpleObjectFactory for the constructor that matches the types provided
		 * @param clazz the type to construct
		 * @param classargs the types to use to find the constructor
		 * @param args the arguments to use for the constructor
		 * @throws SecurityException if clazz.getConstructor throws a SecurityException
		 * @throws NoSuchMethodException if a matching constructor is not found
		 * @throws InstantiationException if an object cannot be constructed using the found constructor
		 * @throws IllegalAccessException if the constructor's access is restricted
		 * @throws InvocationTargetException if the constructor throws an exception
		 */
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
	 * (unsafely) casts the result to the type parameter provided.
	 * 
	 * @author b.elliottsmith
	 */
	public static final class CastFactory<E> implements Factory<E> {
		private static final long serialVersionUID = 8830755652916045329L ;
		private final Factory<?> wrapped ;
		/**
		 * Create a new cast factory 
		 * @param wrapped the factory to delegate creation to
		 */
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
