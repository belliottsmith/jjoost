package org.jjoost.util;

import java.lang.reflect.Array ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Enumeration ;
import java.util.Iterator ;
import java.util.List ;

import org.jjoost.collections.iters.ClosableIterator ;
import org.jjoost.collections.iters.ConcatIterable ;
import org.jjoost.collections.iters.ConcatIterator ;
import org.jjoost.collections.iters.DropIterable ;
import org.jjoost.collections.iters.EnumerationIterator ;
import org.jjoost.collections.iters.HeadClosableIterator ;
import org.jjoost.collections.iters.HeadIterable ;
import org.jjoost.collections.iters.HeadIterator ;
import org.jjoost.collections.iters.OnceIterable ;

public class Iters {

    /**
     * convert the supplied enumeration into an Iterator
     * 
     * @param <E>
     * @param enumeration
     * @return
     */
    public static <E> Iterator<E> iterator(final Enumeration<E> enumeration) {
    	return new EnumerationIterator<E>(enumeration) ;
    }
    
    /**
     * count the items in the provided iterable
     * 
     * @param <E>
     * @param enumeration
     * @return
     */
    public static int count(final Iterable<?> iter) {
    	return count(iter.iterator()) ;
    }
    
    /**
     * count the items in the provided Iterator
     * 
     * @param <E>
     * @param enumeration
     * @return
     */
    public static int count(final Iterator<?> iter) {
    	int count = 0 ;
    	while (iter.hasNext()) {
    		iter.next() ;
    		count++ ;
    	}
    	return count ;
    }
    
    public static boolean equal(Iterator<?> a, Iterator<?> b) {
    	while (a.hasNext() && b.hasNext()) {
    		if (!Objects.equalQuick(a.next(), b.next()))
    			return false ;
    	}
    	return a.hasNext() == b.hasNext() ;
    }
    
    public static <E> boolean contains(final E o, final Iterable<? super E> iter) {
    	return contains(o, iter.iterator()) ;
    }
    public static <E> boolean contains(final E o, final Iterator<? super E> iter) {
    	while (iter.hasNext()) {
    		if (Objects.equalQuick(o, iter.next()))
    			return true ;
    	}
    	return false ;
    }
    
    /**
     * Concatenate the supplied Iterables (lazily)
     * @param <E>
     * @param a
     * @param b
     * @return
     */
    @SuppressWarnings("unchecked")
	public static <E> ConcatIterable<E> concat(Iterable<E> a, Iterable<E> b) {
    	return new ConcatIterable<E>(a, b) ;
    }

    /**
     * Concentenate the supplied Iterables (lazily)
     * 
     * @param <E>
     * @param a
     * @return
     */
	public static <E> ConcatIterable<E> concat(Iterable<E> ... a) {
    	return new ConcatIterable<E>(a) ;
    }
	
	/**
	 * Concentenate the supplied Iterables (lazily)
	 * 
	 * @param <E>
	 * @param a
	 * @return
	 */
	public static <E> ConcatIterable<E> concat(Iterable<? extends Iterable<E>> a) {
		return new ConcatIterable<E>(a) ;
	}
	
	/**
	 * Concentenate the supplied Iterables (lazily)
	 * 
	 * @param <E>
	 * @param a
	 * @return
	 */
	public static <E> ConcatIterable<E> concat(Iterator<? extends Iterable<E>> a) {
		return new ConcatIterable<E>(a) ;
	}
	
	/**
	 * Concentenate the supplied Iterables (lazily)
	 * 
	 * @param <E>
	 * @param a
	 * @return
	 */
	public static <E> ConcatIterator<E> concat(Iterator<? extends Iterator<E>> a) {
		return new ConcatIterator<E>(a) ;
	}
	
	public static <T> T[] toArray(Iterable<?> iter, T[] a) {
		return toArray(iter.iterator(), a) ;
	}
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Iterator<?> iter, T[] a) {
		T[] b = a ;
		int i = 0 ;
		while (iter.hasNext()) {
			if (i == b.length)
				b = Arrays.copyOf(b, b.length << 1) ;
			b[i] = (T) iter.next() ;
			i++ ;
		}
		if (a == b || i == b.length)
			return b ;
		return Arrays.copyOf(b, i) ;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Iterator<?> iter, Class<T> clazz) {
		T[] b = (T[]) Array.newInstance(clazz, 10) ;
		int i = 0 ;
		while (iter.hasNext()) {
			if (i == b.length)
				b = Arrays.copyOf(b, b.length << 1) ;
			b[i] = clazz.cast(iter.next()) ;
			i++ ;
		}
		if (i == b.length)
			return b ;
		return Arrays.copyOf(b, i) ;
	}

	/**
	 * Create an array of type <code>type</code> from the supplied Iterable
	 * 
	 * @param <E>
	 * @param iter
	 * @param type
	 * @return
	 */
    public static <P, E extends P> P[] toArray(Iterable<E> iter, Class<P> type) {
		return toArray(iter.iterator(), type) ;
    }

    /**
     * Create an array of type <code>type</code> from the supplied Iterator
     * 
     * @param <E>
     * @param iter
     * @param type
     * @return
     */
	public static <P, E extends P> P[] toArray(ClosableIterator<E> iter, Class<P> type) {
		return toArray(iter, type) ;
    }

	/**
	 * Create an array from the supplied Iterable
	 * 
	 * @param <E>
	 * @param iter
	 * @return
	 */
	public static Object[] toArray(Iterable<?> iter) {
		return toArray(iter.iterator()) ;
    }

	/**
	 * Create an array from the supplied Iterator
	 * 
	 * @param <E>
	 * @param iter
	 * @return
	 */
	public static Object[] toArray(Iterator<?> iter) {
		return toArray(iter, Object.class) ;
    }

	/**
	 * Create an array from the supplied Iterator
	 * 
	 * @param <E>
	 * @param iter
	 * @return
	 */
	public static Object[] toArray(ClosableIterator<?> iter) {
		return toArray(iter) ;
	}
	
	/**
	 * Create a list from the supplied Iterable
	 * 
	 * @param <E>
	 * @param iter
	 * @return
	 */
	public static <E> List<E> toList(Iterable<E> iter) {
		return toList(iter.iterator()) ; 
	}

	/**
	 * Create a one-shot iterable from the supplied Iterator
	 * 
	 * @param <E>
	 * @param iter
	 * @return
	 */
	public static <E> Iterable<E> onceIterable(Iterator<E> iter) {
		return new OnceIterable<E>(iter) ; 
	}
	
	/**
	 * Create a one-shot iterable from the supplied Iterator
	 * 
	 * @param <E>
	 * @param iter
	 * @return
	 */
	public static <E> Iterable<E> onceIterable(final Enumeration<E> iter) {
		return onceIterable(iterator(iter)) ;
	}

	/**
	 * Create a list from the supplied Iterator
	 * 
	 * @param <E>
	 * @param iter
	 * @return
	 */
	public static <E> List<E> toList(Iterator<E> iter) {
		List<E> result = new ArrayList<E>() ;
		while(iter.hasNext()) 
			result.add(iter.next()) ;
		return result ;
	}
	
	/**
	 * Create a list from the supplied Iterator
	 * 
	 * @param <E>
	 * @param iter
	 * @return
	 */
	public static <E> List<E> toList(ClosableIterator<E> iter) {
		List<E> result = new ArrayList<E>() ;
		while(iter.hasNext()) result.add(iter.next()) ;
		iter.close() ;
		return result ;
	}
	
	/**
	 * Returns a new iterator which returns the first <code>count</code> elements of the supplied iterator (or all of them if that is fewer)
	 * 
	 * @param <E>
	 * @param iter
	 * @param count
	 * @return
	 */
	public static <E> HeadIterator<E> head(Iterator<E> iter, int count) {
		return new HeadIterator<E>(iter, count) ;
	}

    public static <E> HeadClosableIterator<E> head(ClosableIterator<E> iter, int count) {
    	return new HeadClosableIterator<E>(iter, count) ;
    }
    
	/**
	 * Returns a new iterable which returns the first <code>count</code> elements of the supplied iterator (or all of them if that is fewer)
	 * 
	 * @param <E>
	 * @param iter
	 * @param count
	 * @return
	 */
	public static <E> HeadIterable<E> head(Iterable<E> iter, int count) {
		return new HeadIterable<E>(iter, count) ;
	}

    public static <E> List<E> head(List<E> list, int count) {
    	return list.subList(0, Math.max(0, Math.min(list.size(), count < 0 ? list.size() + count : count))) ;
    }
    
    /**
     * Returns a new iterator which returns the first <code>count</code> elements of the supplied iterator (or all of them if that is fewer)
     * 
     * @param <E>
     * @param iter
     * @param count
     * @return
     */
    public static <E> Iterator<E> drop(Iterator<E> iter, int count) {
    	while (iter.hasNext() && count > 0)
    		iter.next() ;
    	return iter ;
    }
    
    public static <E> ClosableIterator<E> drop(ClosableIterator<E> iter, int count) {
    	while (iter.hasNext() && count > 0)
    		iter.next() ;
    	return iter ;
    }
    
    /**
     * Returns a new iterable which returns the first <code>count</code> elements of the supplied iterator (or all of them if that is fewer)
     * 
     * @param <E>
     * @param iter
     * @param count
     * @return
     */
    public static <E> DropIterable<E> drop(Iterable<E> iter, int count) {
    	return new DropIterable<E>(iter, count) ;
    }
    
    public static <E> List<E> drop(List<E> list, int count) {
    	return list.subList(count, list.size()) ;
    }
    
    public static <E> List<E> tail(List<E> list, int count) {
    	return list.subList(Math.max(count < 0 ? -count : list.size() - count, 0), list.size()) ;
    }
    
    /**
     * convert an iterable into a list of Strings (by calling all element's toString() methods, or null if null)
     * 
     * @param list
     * @return
     */
    public static List<String> toStrings(List<?> list) {
    	return Functions.apply(Functions.<Object>toString(), list) ;
    }
    
    /**
     * convert an iterable into a list of Strings (by calling all element's toString() methods, or null if null)
     * 
     * @param list
     * @return
     */
    public static Iterable<String> toStrings(Iterable<?> list) {
    	return Functions.apply(Functions.<Object>toString(), list) ;
    }

    /**
     * convert an iterator into a list of Strings (by calling all element's toString() methods, or null if null)
     * 
     * @param list
     * @return
     */
    public static Iterator<String> toStrings(Iterator<?> list) {
    	return Functions.apply(Functions.<Object>toString(), list) ;
    }
    
    /**
     * convert an iterator into a list of Strings (by calling all element's toString() methods, or null if null)
     * 
     * @param list
     * @return
     */
    public static List<String> toStrings(ClosableIterator<?> list) {
    	List<String> ret = new ArrayList<String>() ;
    	while (list.hasNext()) ret.add(Objects.toString(list.next())) ;
    	list.close() ;
    	return ret ;
    }
    
    public static String toString(Iterable<?> list) {
    	return "[" + toString(list.iterator(), ", ") + "]" ;
    }
    
    public static String toString(Iterator<?> iter) {
    	return "[" + toString(iter, ", ") + "]" ;
    }
    
    public static String toString(Iterable<?> list, String delimiter) {
    	return toString(list.iterator(), delimiter) ;
    }
    /**
     * convert an iterator into a String with some delimiter
     * 
     * @param list
     * @return
     */
    @SuppressWarnings("unchecked")
	public static String toString(Iterator<?> list, String delimiter) {
    	StringBuffer buffer = new StringBuffer() ;
    	boolean doneFirst = false ;
    	while (list.hasNext()) {
    		if (doneFirst) buffer.append(delimiter) ;
    		final Object next = list.next() ;
    		final String str ;
    		if (next instanceof Iterable) str = toString((Iterable<?>) next, delimiter) ;
    		else if (next instanceof Iterator) str = toString((Iterator<?>) next, delimiter) ;
    		else str = Objects.toString(next) ;
    		buffer.append(str) ;
    		doneFirst = true ;
    	}
    	return buffer.toString() ;
    }
    
}
