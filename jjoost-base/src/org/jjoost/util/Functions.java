/**
 * Copyright (c) 2010 Benedict Elliott Smith
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jjoost.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jjoost.util.tuples.Value;
import org.jjoost.collections.iters.ClosableIterator;
import org.jjoost.collections.iters.MappedClosableIterator;
import org.jjoost.collections.iters.MappedIterable;
import org.jjoost.collections.iters.MappedIterator;

/**
 * A class defining simple functions, and methods for working with functions
 * 
 * @author b.elliottsmith
 *
 */
public class Functions {

	private static final Function<Object, Object> IDENTITY = new Identity<Object>();
	private static final class Identity<E> implements Function<E, E> {
		private static final long serialVersionUID = 5801405085506068892L;
		public E apply(E v) { return v ; }
	}
	
	private static final Function<Object, String> TO_STRING = new ToString<Object>();
	private static final class ToString<E> implements Function<E, String> {
		private static final long serialVersionUID = 5801405085506068892L;
		public String apply(E v) { return v == null ? null : v.toString() ; }
	}
		
	private static final Function<Object, String> TO_STRING_REPLACENULLS = new ToStringReplaceNulls<Object>();
	private static final class ToStringReplaceNulls<E> implements Function<E, String> {
		private static final long serialVersionUID = 5801405085506068892L;
		public String apply(E v) { return v == null ? "null" : v.toString() ; }
	}
	
	private static final class ReplaceNulls<E> implements Function<E, E> {
		private static final long serialVersionUID = 5801405085506068892L;
		private final E replace;
		public ReplaceNulls(E val) {
			this.replace = val;
		}
		public E apply(E v) { return v == null ? replace : v ; }
	}
	
	/**
	 * Returns the identity function, i.e. that returns its input as output
	 * @param <E> input type
	 * @return the identity function
	 */
	@SuppressWarnings("unchecked")
	public static <E> Function<E, E> identity() {
		return (Function<E, E>) IDENTITY;
	}
	
	/**
	 * Returns a function that converts its input to a <code>String</code>; if <code>replaceNulls</code> is <code>true</code> then
	 * <code>null</code> values are replaced by <code>"null"</code>, otherwise they are left as <code>null</code>
	 * 
	 * @param replaceNulls
	 *            if <code>true</code>, replace nulls with <code>"null"</code>
	 * @return function that converts its input to a <code>String</code>
	 */
	@SuppressWarnings("unchecked")
	public static <E> Function<E, String> toString(boolean replaceNulls) {
		return (Function<E, String>) (replaceNulls ? TO_STRING_REPLACENULLS : TO_STRING);
	}
	
	/**
	 * Returns a function that for all non-null inputs acts like the identity function (i.e. yields input as output),
	 * however replaces all null values it is applied to with the provided value
	 * 
	 * @param val value to replace nulls with
	 * @return a function replacing nulls with the provided value
	 */
	public static <E> Function<E, E> replaceNullWith(E val) {
		return new ReplaceNulls<E>(val);
	}
	
	/**
	 * Returns a function that is the composition of the two provided functions; the function provided as the second argument is applied
	 * first, and the function provided as the first argument is then applied to the result of this, i.e. returns
	 * <code>f2.apply(f1.apply(v))</code>
	 * 
	 * @param f2
	 *            function applied second
	 * @param f1
	 *            function applied first
	 * @return composition of f1 and f2
	 */
	public static <E, F, G> Function<E, G> composition(final Function<? super F, ? extends G> f2, final Function<? super E, ? extends F> f1) {
		return new Function<E, G>() {
			private static final long serialVersionUID = 3725675651261795247L;
			@Override
			public G apply(E v) {
				return f2.apply(f1.apply(v));
			}
		};
	}
	
	/**
	 * Return a Function which executes the named no-arg method on objects of the provided type
	 * 
	 * @param clazz function input type
	 * @param methodName name of the method to call
	 * @param returnType the return type of the method
	 * @return a Function which executes the named no-arg method on objects of the provided type
	 * @throws SecurityException if a SecurityManager prevents access to the method 
	 * @throws NoSuchMethodException if no no-args method of the provided name can be found in the provided class' hierarchy
	 */
	public static <E, F> Function<E, F> getMethodProjection(Class<E> clazz, String methodName, final Class<F> returnType) throws SecurityException, NoSuchMethodException {
		final Method m = clazz.getMethod(methodName, new Class[0]);
		final Object[] args = new Object[0];
		return new MethodProjection<E, F>(returnType, m, args);
	}
	
	/**
	 * A <code>Function</code> which accepts a <code>Method</code> and arguments to provide to the method, which will execute the method on
	 * each object it receives as an argument, returning the result.
	 * 
	 * @author b.elliottsmith
	 */
	public static final class MethodProjection<E, F> implements Function<E, F> {
		private static final long serialVersionUID = -2627623099906029633L;
		final Class<F> returnType;
		final Method m;
		final Object[] args;
		/**
		 * Create a new MethodProjection class, which executes the provided method on any input object, using the provided arguments
		 * @param returnType the return type of the provided method, provided for type safety
		 * @param m the method
		 * @param args the arguments to provide to each method call
		 */
		public MethodProjection(Class<F> returnType, Method m, Object[] args) {
			super();
			this.returnType = returnType;
			this.m = m;
			this.args = args;
		}
		public F apply(E v) {
			try {
				return returnType.cast(m.invoke(v, args));
			} catch (IllegalAccessException e) {
				throw new UndeclaredThrowableException(e);
			} catch (InvocationTargetException e) {
				throw new UndeclaredThrowableException(e);
			}
		}
	}
	
	/**
	 * Return a Function which retrieves the named field from each object it is applied to
	 * @param clazz the input type of the function
	 * @param propertyName the name of the property
	 * @param propertyType the type of the property
	 * @return a Function which retrieves the named field from each object it is applied to
	 * @throws SecurityException if a SecurityManager prevents access to the method
	 * @throws NoSuchFieldException if a field of the provided name could not be found in the provided class
	 */
	public static <E, F> Function<E, F> getFieldProjection(Class<E> clazz, String propertyName, final Class<F> propertyType) throws SecurityException, NoSuchFieldException {
		final Field f = clazz.getField(propertyName);
		if (!propertyType.isAssignableFrom(f.getType()))
			throw new IllegalArgumentException("");
		return new FieldProjection<E, F>(propertyType, f);
	}
	
	/**
	 * A <code>Function</code> which accepts a <code>Field</code> (reflection API), which will be retrieved and returned
	 * from every object the function is applied to.
	 * 
	 * @author b.elliottsmith
	 */
	public static final class FieldProjection<E, F> implements Function<E, F> {
		private static final long serialVersionUID = -2627623099906029633L;
		final Class<F> returnType;
		final Field f;
		/**
		 * Create a new <code>FieldProjection</code>
		 * 
		 * @param returnType
		 *            the type stored by the field
		 * @param f
		 *            the <code>Field</code> object
		 */
		public FieldProjection(Class<F> returnType, Field f) {
			super();
			this.returnType = returnType;
			this.f = f;
		}
		public F apply(E v) {
			try {
				return returnType.cast(f.get(v));
			} catch (IllegalAccessException e) {
				throw new UndeclaredThrowableException(e);
			}
		}
	}
	
	/**
	 * Return a function that retrieves the value portion of a <code>Map.Entry</code>
	 * 
	 * @return a function that retrieves the value portion of a <code>Map.Entry</code>
	 */
	@SuppressWarnings("unchecked")
	public static <V, E extends Entry<?, ? extends V>> Function<E, V> getMapEntryValueProjection() {
		return MAP_ENTRY_VALUE_PROJECTION;
	}
	@SuppressWarnings("rawtypes")
	private static final MapEntryValueProjection MAP_ENTRY_VALUE_PROJECTION = new MapEntryValueProjection();
	/**
	 * A function that retrieves the value portion of a <code>Map.Entry</code>
	 * 
	 * @author b.elliottsmith
	 */
	public static final class MapEntryValueProjection<E extends Entry<?, V>, V> implements Function<E, V> {
		private static final long serialVersionUID = -2627623099906029633L;
		public V apply(E v) { return v.getValue() ; }
	}
	
	/**
	 * Return a function that retrieves the key portion of a <code>Map.Entry</code>
	 * 
	 * @return a function that retrieves the key portion of a <code>Map.Entry</code>
	 */
	@SuppressWarnings("unchecked")
	public static <K, E extends Entry<? extends K, ?>> Function<E, K> getMapEntryKeyProjection() {
		return MAP_ENTRY_KEY_PROJECTION;
	}
	@SuppressWarnings("rawtypes")
	private static final MapEntryKeyProjection MAP_ENTRY_KEY_PROJECTION = new MapEntryKeyProjection();
	/**
	 * A function that retrieves the key portion of a <code>Map.Entry</code>
	 * 
	 * @author b.elliottsmith
	 */
	public static final class MapEntryKeyProjection<E extends Entry<K, ?>, K> implements Function<E, K> {
		private static final long serialVersionUID = -2627623099906029633L;
		public K apply(E v) { return v.getKey() ; }
	}
	
	/**
	 * Return a function that retrieves the value from a <code>Value</code> object
	 * 
	 * @return a function that retrieves the value from a <code>Value</code> object
	 */
	@SuppressWarnings("unchecked")
	public static <V> Function<Value<V>, V> getValueContentsProjection() {
		return ABSTRACT_VALUE_PROJECTION;
	}
	@SuppressWarnings("rawtypes")
	private static final AbstractValueProjection ABSTRACT_VALUE_PROJECTION = new AbstractValueProjection();
	/**
	 * A function that retrieves the value from a <code>Value</code>
	 * 
	 * @author b.elliottsmith
	 */
	public static final class AbstractValueProjection<E extends Value<V>, V> implements Function<E, V> {
		private static final long serialVersionUID = -2627623099906029633L;
		public V apply(E v) { return v.getValue() ; }
	}


    /**
	 * Creates a new <code>ArrayList</code> that contains the result of applying the provided function to every element in the provided
	 * list. Equivalent to <code>apply(list, f)</code>.
	 * 
	 * @param list
	 *            list to apply the function to
	 * @param f
	 *            function to apply to the list
	 * @return a new <code>ArrayList</code> that contains the result of applying the provided function to every element in the provided list
	 */
    public static <E, F> List<F> apply(Function<? super E, ? extends F> f, List<? extends E> list) {
    	List<F> ret = new ArrayList<F>(list.size());
    	for (E e : list) ret.add(f.apply(e));
    	return ret;
    }
    
    /**
	 * Creates a new <code>ArrayList</code> that contains the result of applying the provided function to every element in the provided list.
	 * Equivalent to <code>apply(f, list)</code>.
	 * 
	 * @param list
	 *            list to apply the function to
	 * @param f
	 *            function to apply to the list
	 * @return a new <code>ArrayList</code> that contains the result of applying the provided function to every element in the provided list
	 */
    public static <E, F> List<F> apply(List<? extends E> list, Function<? super E, ? extends F> f) {
        return apply(f, list);
    }
    
    /**
	 * Returns a new <code>Iterable</code> which lazily applies the provided function to all iterators constructed from it.
	 * Equivalent to <code>apply(iter, f)</code>.
	 * 
	 * @param iter
	 *            iterable to apply the function to
	 * @param f
	 *            function to apply to the iterable
	 * @return a new <code>Iterable</code> which lazily applies the provided function to all iterators constructed from it
	 */
    public static <E, F> Iterable<F> apply(Function<? super E, ? extends F> f, Iterable<? extends E> iter) {
    	return new MappedIterable<E, F>(iter, f);
    }
    
    /**
	 * Returns a new <code>Iterable</code> which lazily applies the provided function to all iterators constructed from it.
	 * Equivalent to <code>apply(f, iter)</code>.
	 * 
	 * @param iter
	 *            iterable to apply the function to
	 * @param f
	 *            function to apply to the iterable
	 * @return a new <code>Iterable</code> which lazily applies the provided function to all iterators constructed from it
	 */
    public static <E, F> Iterable<F> apply(Iterable<? extends E> iter, Function<? super E, ? extends F> f) {
    	return apply(f, iter);
    }
    
    /**
	 * Returns a new iterator which returns <code>f.f(iter.next())</code> for each call to <code>next()</code> in the resulting iterator.
	 * Equivalent to <code>apply(f, iter)</code>.
	 * 
	 * @param iter
	 *            iterator to apply the function to
	 * @param f
	 *            function to apply to the iterator
	 * @return a new iterator which return <code>f.f(iter.next())</code> for each call to <code>next()</code>
	 */
    public static <E, F> Iterator<F> apply(Iterator<? extends E> iter, Function<? super E, ? extends F> f) {
    	return apply(f, iter);
    }

    /**
	 * Returns a new iterator which returns <code>f.f(iter.next())</code> for each call to <code>next()</code> in the resulting iterator.
	 * Equivalent to <code>apply(iter, f)</code>.
	 * 
	 * @param iter
	 *            iterator to apply the function to
	 * @param f
	 *            function to apply to the iterator
	 * @return a new iterator which return <code>f.f(iter.next())</code> for each call to <code>next()</code>
	 */
    public static <E, F> Iterator<F> apply(Function<? super E, ? extends F> f, Iterator<? extends E> iter) {
    	return new MappedIterator<E, F>(iter, f);
    }
    
    /**
    /**
	 * Returns a new iterator which returns <code>f.f(iter.next())</code> for each call to <code>next()</code> in the resulting iterator.
	 * Equivalent to <code>apply(f, iter)</code>.
	 * 
	 * @param iter
	 *            iterator to apply the function to
	 * @param f
	 *            function to apply to the iterator
	 * @return a new iterator which return <code>f.f(iter.next())</code> for each call to <code>next()</code>
	 */
    public static <E, F> ClosableIterator<F> apply(ClosableIterator<? extends E> iter, Function<? super E, ? extends F> f) {
    	return apply(f, iter);
    }

    /**
	 * Returns a new iterator which returns <code>f.f(iter.next())</code> for each call to <code>next()</code> in the resulting iterator.
	 * Equivalent to <code>apply(iter, f)</code>.
	 * 
	 * @param iter
	 *            iterator to apply the function to
	 * @param f
	 *            function to apply to the iterator
	 * @return a new iterator which return <code>f.f(iter.next())</code> for each call to <code>next()</code>
	 */
    public static <E, F> ClosableIterator<F> apply(Function<? super E, ? extends F> f, ClosableIterator<? extends E> iter) {
    	return new MappedClosableIterator<E, F>(iter, f);
    }
    

}
