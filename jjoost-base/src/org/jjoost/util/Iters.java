package org.jjoost.util;

import java.lang.reflect.Array ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Enumeration ;
import java.util.Iterator ;
import java.util.List ;

import org.jjoost.collections.iters.ArrayIterator ;
import org.jjoost.collections.iters.ClosableIterator ;
import org.jjoost.collections.iters.ConcatIterable ;
import org.jjoost.collections.iters.ConcatIterator ;
import org.jjoost.collections.iters.DestructiveIterator;
import org.jjoost.collections.iters.DropIterable ;
import org.jjoost.collections.iters.EmptyIterable ;
import org.jjoost.collections.iters.EmptyIterator;
import org.jjoost.collections.iters.EnumerationIterator ;
import org.jjoost.collections.iters.HeadClosableIterator ;
import org.jjoost.collections.iters.HeadIterable ;
import org.jjoost.collections.iters.HeadIterator ;
import org.jjoost.collections.iters.OnceIterable ;

/**
 * A class declaring useful methods for working with <code>Iterator</code> and <code>Iterable</code> objects
 * 
 * @author b.elliottsmith
 */
public class Iters {

	/**
	 * Return an <code>Iterator</code> which contains no values
	 * 
	 * @return an <code>Iterator</code> which contains no values
	 */
	public static <E> Iterator<E> emptyIterator() {
		return EmptyIterator.<E>get() ;
	}
	
	/**
	 * Return an <code>Iterator</code> which contains no values
	 * 
	 * @return an <code>Iterator</code> which contains no values
	 */
	public static <E> Iterable<E> emptyIterable() {
		return EmptyIterable.<E>get() ;
	}
	
	/**
	 * convert the supplied array into an Iterator
	 * 
	 * @param array
	 *            the array to wrap
	 * @return an <code>Iterator</code> representing the provided array
	 */
	public static <E> Iterator<E> iterator(final E[] array) {
		return new ArrayIterator<E>(array) ;
	}
	
	/**
	 * convert the supplied array into an <code>Iterator</code>
	 * 
	 * @param array
	 *            the array to wrap
	 * @param count
	 *            the number of elements to use from the array, beginning at index 0
	 * @return an <code>Iterator</code> representing the first <code>count</code> elements of the <code>array</code>
	 */
	public static <E> Iterator<E> iterator(final E[] array, int count) {
		return new ArrayIterator<E>(array, 0, count) ;
	}
	
	/**
	 * convert the supplied array into an <code>Iterator</code>
	 * 
	 * @param array
	 *            array to wrap
	 * @param lb
	 *            the first index to use from the array
	 * @param ub
	 *            the last index to use from the array
	 * @return an <code>Iterator</code> representing the elements of the provided array from index <code>lb</code> (incl) to <code>ub</code>
	 *         (excl)
	 */
	public static <E> Iterator<E> iterator(final E[] array, int lb, int ub) {
		return new ArrayIterator<E>(array, lb, ub) ;
	}
	
	/**
	 * convert the supplied <code>Enumeration</code> into an <code>Iterator</code>
	 * 
	 * @param enumeration
	 *            an <code>Enumeration</code>
	 * @return an <code>Iterator</code> wrapping the provided Enumeration
	 */
    public static <E> Iterator<E> iterator(final Enumeration<E> enumeration) {
    	return new EnumerationIterator<E>(enumeration) ;
    }
    
    /**
	 * count the items in the provided <code>Iterable</code> by creating an <code>Iterator</code> from it and consuming every element
	 * 
	 * @param iter
	 *            An <code>Iterable</code>
	 * @return the number of elements in the provided <code>Iterable</code>
	 */
    public static int count(final Iterable<?> iter) {
    	return count(iter.iterator()) ;
    }
    
    /**
	 * counts the items in the provided <code>Iterator</code> by fully exhausting it
	 * 
	 * @param iter
	 *            an <code>Iterator</code>
	 * @return the number of elements in the provided <code>Iterator</code>
	 */
    public static int count(final Iterator<?> iter) {
    	int count = 0 ;
    	while (iter.hasNext()) {
    		iter.next() ;
    		count++ ;
    	}
    	return count ;
    }
    
    /**
	 * Returns a <code>boolean</code> indicating if the two provided <code>Iterators</code> are "equal", using regular <code>Object</code>
	 * equality (<code>Equalities.object()</code>). The ordering of the elements is important to this method; i.e., it confirms that the
	 * same element occurs in the same position in both iterators.
	 * 
	 * @param a
	 *            an <code>Iterator</code>
	 * @param b
	 *            an <code>Iterator</code>
	 * @return a <code>boolean</code> indicating if the two provided <code>Iterators</code> are "equal", using regular <code>Object</code>
	 *         equality (<code>Equalities.object()</code>)
	 */
    public static boolean equal(Iterator<?> a, Iterator<?> b) {
    	return equal(Equalities.object(), a, b) ;
    }
    /**
	 * Returns a <code>boolean</code> indicating if the two provided <code>Iterators</code> are "equal", using the provided
	 * <code>Equality</code>. The ordering of the elements is important to this method; i.e., it confirms that the same element occurs in
	 * the same position in both iterators.
	 * 
	 * @param eq
	 *            the <code>Equality</code> to use to test element equality
	 * @param a
	 *            an <code>Iterator</code>
	 * @param b
	 *            an <code>Iterator</code>
	 * @return a <code>boolean</code> indicating if the two provided <code>Iterators</code> are "equal", using regular <code>Object</code>
	 *         equality (<code>Equalities.object()</code>)
	 */
    public static <E> boolean equal(Equality<? super E> eq, Iterator<? extends E> a, Iterator<? extends E> b) {
    	while (a.hasNext() && b.hasNext()) {
    		if (!eq.equates(a.next(), b.next()))
    			return false ;
    	}
    	return a.hasNext() == b.hasNext() ;
    }
    
    /**
	 * Returns a <code>boolean</code> indicating if the provided <code>Iterable</code> contains an element equal to the provided object, as
	 * determined by regular <code>Object</code> equality (<code>Equalities.object()</code>). It achieves this by creating an
	 * <code>Iterator</code> from the <code>Iterable</code> and consuming the input until an equal value is found, or the
	 * <code>Iterator</code> is exhausted.
	 * 
	 * @param find
	 *            value to find
	 * @param iter
	 *            values to look through
	 * @return a <code>boolean</code> indicating if the <code>Iterable</code> contains the provided value
	 */
    public static <E> boolean contains(final E find, final Iterable<E> iter) {
    	return contains(Equalities.object(), find, iter.iterator()) ;
    }
    
    /**
	 * Returns a <code>boolean</code> indicating if the provided <code>Iterator</code> contains an element equal to the provided object, as
	 * determined by regular <code>Object</code> equality (<code>Equalities.object()</code>). It achieves this by consuming the input until
	 * an equal value is found, or the <code>Iterator</code> is exhausted.
	 * 
	 * @param find
	 *            value to find
	 * @param iter
	 *            values to look through
	 * @return a <code>boolean</code> indicating if the <code>Iterator</code> contains the provided value
	 */
    public static <E> boolean contains(final E find, final Iterator<E> iter) {
    	return contains(Equalities.object(), find, iter) ;
    }
    
    /**
	 * Returns a <code>boolean</code> indicating if the provided <code>Iterable</code> contains an element equal to the provided object, as
	 * determined by the provided <code>Equality</code>. It achieves this by creating an <code>Iterator</code> from the
	 * <code>Iterable</code> and consuming the input until an equal value is found, or the <code>Iterator</code> is exhausted.
	 * 
	 * @param eq
	 *            the <code>Equality</code> to use
	 * @param find
	 *            value to find
	 * @param iter
	 *            values to look through
	 * @return a <code>boolean</code> indicating if the <code>Iterable</code> contains the provided value
	 */
    public static <E> boolean contains(final Equality<? super E> eq, final E find, final Iterable<E> iter) {
    	return contains(eq, find, iter.iterator()) ;
    }
    
    /**
	 * Returns a <code>boolean</code> indicating if the provided <code>Iterator</code> contains an element equal to the provided object, as
	 * determined by the provided <code>Equality</code>. It achieves this by consuming the input until an equal value is found, or the
	 * <code>Iterator</code> is exhausted.
	 * 
	 * @param eq
	 *            the <code>Equality</code> to use
	 * @param find
	 *            value to find
	 * @param iter
	 *            values to look through
	 * @return a <code>boolean</code> indicating if the <code>Iterator</code> contains the provided value
	 */
    public static <E> boolean contains(final Equality<? super E> eq, final E find, final Iterator<E> iter) {
    	while (iter.hasNext()) {
    		if (eq.equates(find, iter.next()))
    			return true ;
    	}
    	return false ;
    }

    /**
	 * Returns an <code>int</code> representing the number of occurrences, in the provided <code>Iterable</code>, of elements equal to the
	 * provided object, as determined by regular <code>Object</code> equality (<code>Equalities.object()</code>). It achieves this by
	 * creating an <code>Iterator</code> from the <code>Iterable</code> and consuming the input until an equal value is found, or the
	 * <code>Iterator</code> is exhausted.
	 * 
	 * @param find
	 *            value to find
	 * @param iter
	 *            values to look through
	 * @return an <code>int</code> representing the number of occurrences in the <code>Iterable</code> of the provided value
	 */
    public static <E> int count(final E find, final Iterable<E> iter) {
    	return count(find, iter.iterator()) ;
    }
    
    /**
	 * Returns an <code>int</code> representing the number of occurrences, in the provided <code>Iterator</code>, of elements equal to the
	 * provided object, as determined by regular <code>Object</code> equality (<code>Equalities.object()</code>). It achieves this by
	 * consuming the input until an equal value is found, or the <code>Iterator</code> is exhausted.
	 * 
	 * @param find
	 *            value to find
	 * @param iter
	 *            values to look through
	 * @return an <code>int</code> representing the number of occurrences in the <code>Iterable</code> of the provided value
	 */
    public static <E> int count(final E find, final Iterator<E> iter) {
    	return count(Equalities.object(), find, iter) ;
    }    
    /**
	 * Returns an <code>int</code> representing the number of occurrences, in the provided <code>Iterable</code>, of elements equal to the
	 * provided object, as determined by the provided <code>Equality</code>. It achieves this by creating an <code>Iterator</code> from the
	 * <code>Iterable</code> and consuming the input until an equal value is found, or the <code>Iterator</code> is exhausted.
	 * 
	 * @param eq
	 *            the <code>Equality</code> to use
	 * @param find
	 *            value to find
	 * @param iter
	 *            values to look through
	 * @return an <code>int</code> representing the number of occurrences in the <code>Iterable</code> of the provided value
	 */
    public static <E> int count(final Equality<? super E> eq, final E find, final Iterable<E> iter) {
    	return count(eq, find, iter.iterator()) ;
    }
    /**
	 * Returns an <code>int</code> representing the number of occurrences, in the provided <code>Iterator</code>, of elements equal to the
	 * provided object, as determined by the provided <code>Equality</code>. It achieves this by consuming the input until an equal value is
	 * found, or the <code>Iterator</code> is exhausted.
	 * 
	 * @param eq
	 *            the <code>Equality</code> to use
	 * @param find
	 *            value to find
	 * @param iter
	 *            values to look through
	 * @return an <code>int</code> representing the number of occurrences in the <code>Iterable</code> of the provided value
	 */
    public static <E> int count(final Equality<? super E> eq, final E find, final Iterator<E> iter) {
    	int c = 0 ;
    	while (iter.hasNext()) {
    		if (eq.equates(find, iter.next()))
    			c++ ;
    	}
    	return c ;
    }
    
    /**
	 * Return an <code>Iterator</code> that wraps the provided <code>Iterator</code>, and automatically calls <code>remove()</code> after
	 * every successful call of <code>next()</code>
	 * 
	 * @param iter
	 *            the <code>Iterator</code> to wrap (and hence, destroy)
	 * @return an <code>Iterator</code> that wraps the provided <code>Iterator</code>, and automatically calls <code>remove()</code> after
	 *         every successful call of <code>next()</code>
	 */
    public static <E> Iterator<E> destroyAsConsumed(Iterator<E> iter) {
    	return new DestructiveIterator<E>(iter) ;
    }
    
    /**
	 * Lazily concatenates the two provided <code>Iterable</code> objects.
	 * 
	 * @param a
	 *            an <code>Iterable</code>
	 * @param b
	 *            an <code>Iterable</code>
	 * @return the concatenation of b to a (i.e. a followed by b)
	 */
    @SuppressWarnings("unchecked")
	public static <E> ConcatIterable<E> concat(Iterable<E> a, Iterable<E> b) {
    	return new ConcatIterable<E>(a, b) ;
    }

    /**
	 * Lazily concatenates the provided <code>Iterable</code> objects.
	 * 
	 * @param a
	 *            an var-args array of <code>Iterable</code> objects
	 * @return the concatenation of all provided <code>Iterable</code> objects, returned in the order they appear in the array
	 */
	public static <E> ConcatIterable<E> concat(Iterable<E> ... a) {
    	return new ConcatIterable<E>(a) ;
    }
	
    /**
	 * Lazily concatenates the provided <code>Iterable</code> objects.
	 * 
	 * @param a
	 *            an <code>Iterable</code> of <code>Iterable</code> objects
	 * @return the concatenation of all provided <code>Iterable</code> objects, returned in the order they appear in <code>the</code>
	 *         Iterable
	 */
	public static <E> ConcatIterable<E> concat(Iterable<? extends Iterable<E>> a) {
		return new ConcatIterable<E>(a) ;
	}
	
    /**
	 * Lazily concatenates the provided <code>Iterator</code> objects.
	 * 
	 * @param a
	 *            an <code>Iterator</code> of <code>Iterator</code> objects
	 * @return the concatenation of all provided <code>Iterator</code> objects, returned in the order they appear in the
	 *         <code>Iterator</code>
	 */
	public static <E> ConcatIterator<E> concat(Iterator<? extends Iterator<E>> a) {
		return new ConcatIterator<E>(a) ;
	}
	
	/**
	 * Create a one-shot <code>Iterable</code> from the supplied <code>Iterator</code>
	 * 
	 * @param iter
	 *            an <code>Iterator</code>
	 * @return an <code>Iterable</code> which returns the provided <code>Iterator</code> for every call to <code>iterator()</code>
	 */
	public static <E> Iterable<E> onceIterable(Iterator<E> iter) {
		return new OnceIterable<E>(iter) ; 
	}
	
	/**
	 * Returns a new <code>Iterator</code> that returns the first <code>count</code> elements of the supplied <code>Iterator</code> (or all
	 * of them if that is fewer)
	 * 
	 * @param iter
	 *            an <code>Iterator</code>
	 * @param count
	 *            the number of elements to return from the <code>Iterator</code>
	 * @return an <code>Iterator</code> returning the first <code>count</code> elements of the supplied <code>Iterator</code>
	 */
	public static <E> HeadIterator<E> head(Iterator<E> iter, int count) {
		return new HeadIterator<E>(iter, count) ;
	}

	/**
	 * Returns a new <code>Iterator</code> that returns the first <code>count</code> elements of the supplied <code>Iterator</code> (or all
	 * of them if that is fewer)
	 * 
	 * @param count
	 *            the number of elements to return from the <code>Iterator</code>
	 * @param iter
	 *            an <code>Iterator</code>
	 * @return an <code>Iterator</code> returning the first <code>count</code> elements of the supplied <code>Iterator</code>
	 */
	public static <E> HeadIterator<E> head(int count, Iterator<E> iter) {
		return new HeadIterator<E>(iter, count) ;
	}
	
	/**
	 * Returns a new <code>Iterator</code> that returns the first <code>count</code> elements of the supplied <code>Iterator</code> (or all
	 * of them if that is fewer)
	 * 
	 * @param iter
	 *            an <code>Iterator</code>
	 * @param count
	 *            the number of elements to return from the <code>Iterator</code>
	 * @return an <code>Iterator</code> returning the first <code>count</code> elements of the supplied <code>Iterator</code>
	 */
    public static <E> HeadClosableIterator<E> head(ClosableIterator<E> iter, int count) {
    	return new HeadClosableIterator<E>(iter, count) ;
    }
    
    /**
     * Returns a new <code>Iterator</code> that returns the first <code>count</code> elements of the supplied <code>Iterator</code> (or all
     * of them if that is fewer)
     * 
     * @param count
     *            the number of elements to return from the <code>Iterator</code>
     * @param iter
     *            an <code>Iterator</code>
     * @return an <code>Iterator</code> returning the first <code>count</code> elements of the supplied <code>Iterator</code>
     */
    public static <E> HeadClosableIterator<E> head(int count, ClosableIterator<E> iter) {
    	return new HeadClosableIterator<E>(iter, count) ;
    }
    
	/**
	 * Returns a new <code>Iterable</code> that returns the first <code>count</code> elements of the supplied <code>Iterable</code> (or all
	 * of them if that is fewer)
	 * 
	 * @param iter
	 *            an <code>Iterable</code>
	 * @param count
	 *            the number of elements to return from the <code>Iterable</code>
	 * @return an <code>Iterable</code> returning the first <code>count</code> elements of the supplied <code>Iterable</code>
	 */
	public static <E> HeadIterable<E> head(Iterable<E> iter, int count) {
		return new HeadIterable<E>(iter, count) ;
	}

	/**
	 * Returns a new <code>Iterable</code> that returns the first <code>count</code> elements of the supplied <code>Iterable</code> (or all
	 * of them if that is fewer)
	 * 
	 * @param count
	 *            the number of elements to return from the <code>Iterable</code>
	 * @param iter
	 *            an <code>Iterable</code>
	 * @return an <code>Iterable</code> returning the first <code>count</code> elements of the supplied <code>Iterable</code>
	 */
	public static <E> HeadIterable<E> head(int count, Iterable<E> iter) {
		return new HeadIterable<E>(iter, count) ;
	}
	
	/**
	 * Returns a new <code>List</code> that returns the first <code>count</code> elements of the supplied <code>List</code> (or all
	 * of them if that is fewer)
	 * 
	 * @param list
	 *            an <code>Iterable</code>
	 * @param count
	 *            the number of elements to return from the <code>Iterable</code>
	 * @return a <code>List</code> returning the first <code>count</code> elements of the supplied <code>List</code>
	 */
    public static <E> List<E> head(List<E> list, int count) {
    	return list.subList(0, Math.max(0, Math.min(list.size(), count < 0 ? list.size() + count : count))) ;
    }
    
    /**
	 * Consumes the first <code>count</code> elements of the supplied <code>Iterator</code> (or all of them if there are fewer), and returns
	 * its argument
	 * 
	 * @param iter
	 *            an <code>Iterator</code>
	 * @param count
	 *            the number of elements to ignore from the <code>Iterator</code>
	 * @return an <code>Iterator</code> returning <b>all but</b> the first <code>count</code> elements of the supplied <code>Iterator</code>
	 */
    public static <E> Iterator<E> drop(Iterator<E> iter, int count) {
    	while (iter.hasNext() && count > 0)
    		iter.next() ;
    	return iter ;
    }
    
    /**
	 * Consumes the first <code>count</code> elements of the supplied <code>Iterator</code> (or all of them if there are fewer), and returns
	 * its argument
	 * 
	 * @param count
	 *            the number of elements to ignore from the <code>Iterator</code>
	 * @param iter
	 *            an <code>Iterator</code>
	 * @return an <code>Iterator</code> returning <b>all but</b> the first <code>count</code> elements of the supplied <code>Iterator</code>
	 */
    public static <E> Iterator<E> drop(int count, Iterator<E> iter) {
    	while (iter.hasNext() && count > 0)
    		iter.next() ;
    	return iter ;
    }
    
    /**
	 * Consumes the first <code>count</code> elements of the supplied <code>Iterator</code> (or all of them if there are fewer), and returns
	 * its argument
	 * 
	 * @param iter
	 *            an <code>Iterator</code>
	 * @param count
	 *            the number of elements to ignore from the <code>Iterator</code>
	 * @return an <code>Iterator</code> returning <b>all but</b> the first <code>count</code> elements of the supplied <code>Iterator</code>
	 */
    public static <E> ClosableIterator<E> drop(ClosableIterator<E> iter, int count) {
    	while (iter.hasNext() && count > 0)
    		iter.next() ;
    	return iter ;
    }
    
    /**
     * Consumes the first <code>count</code> elements of the supplied <code>Iterator</code> (or all of them if there are fewer), and returns
     * its argument
     * 
     * @param iter
     *            an <code>Iterator</code>
     * @param count
     *            the number of elements to ignore from the <code>Iterator</code>
     * @return an <code>Iterator</code> returning <b>all but</b> the first <code>count</code> elements of the supplied <code>Iterator</code>
     */
    public static <E> ClosableIterator<E> drop(int count, ClosableIterator<E> iter) {
    	while (iter.hasNext() && count > 0)
    		iter.next() ;
    	return iter ;
    }
    
	/**
	 * Returns a new <code>Iterable</code> that returns <b>all but</b> the first <code>count</code> elements of the supplied
	 * <code>Iterable</code> (or none of them if there are fewer). The removal of these elements is done for each <code>Iterator</code>
	 * constructed by the <code>Iterable</code>
	 * 
	 * @param iter
	 *            an <code>Iterable</code>
	 * @param count
	 *            the number of elements to ignore from the <code>Iterable</code>
	 * @return an <code>Iterable</code> returning <b>all but</b> the first <code>count</code> elements of the supplied <code>Iterable</code>
	 */
    public static <E> DropIterable<E> drop(Iterable<E> iter, int count) {
    	return new DropIterable<E>(iter, count) ;
    }
    
    /**
     * Returns a new <code>Iterable</code> that returns <b>all but</b> the first <code>count</code> elements of the supplied
     * <code>Iterable</code> (or none of them if there are fewer). The removal of these elements is done for each <code>Iterator</code>
     * constructed by the <code>Iterable</code>
     * 
     * @param count
     *            the number of elements to ignore from the <code>Iterable</code>
     * @param iter
     *            an <code>Iterable</code>
     * @return an <code>Iterable</code> returning <b>all but</b> the first <code>count</code> elements of the supplied <code>Iterable</code>
     */
    public static <E> DropIterable<E> drop(int count, Iterable<E> iter) {
    	return new DropIterable<E>(iter, count) ;
    }
    
    /**
     * Returns a <code>List</code> that represents <b>all but</b> the first <code>count</code> elements of the supplied
     * <code>List</code> (or none of them if there are fewer).
     * 
     * @param count
     *            the number of elements to ignore from the <code>List</code>
     * @param list
     *            an <code>List</code>
     * @return an <code>List</code> returning <b>all but</b> the first <code>count</code> elements of the supplied <code>List</code>
     */
    public static <E> List<E> drop(List<E> list, int count) {
    	return list.subList(count, list.size()) ;
    }
    
    /**
     * Returns a <code>List</code> that represents the last <code>count</code> elements of the supplied
     * <code>List</code> (or all of them if there are fewer).
     * 
     * @param count
     *            the number of elements to take from the end of the <code>List</code>
     * @param list
     *            an <code>List</code>
     * @return an <code>List</code> returning <b>all but</b> the first <code>count</code> elements of the supplied <code>List</code>
     */
    public static <E> List<E> tail(List<E> list, int count) {
    	return list.subList(Math.max(count < 0 ? -count : list.size() - count, 0), list.size()) ;
    }
    
    /**
	 * Copy the contents of the provided <code>Iterable</code> into the provided array; if the array is not large enough a new one will be
	 * initialised and returned. If the array was not large enough the new array will be the exact size of the number of elements copied.
	 * 
	 * @param iter
	 *            the <code>Iterable</code> to copy records from
	 * @param a
	 *            the array to copy records to
	 * @return the final array
	 */
	public static <T> T[] toArray(Iterable<?> iter, T[] a) {
		return toArray(iter.iterator(), a) ;
	}
	
    /**
	 * Copy the contents of the provided <code>Iterator</code> into the provided array; if the array is not large enough a new one will be
	 * initialised and returned. If the array was not large enough the new array will be the exact size of the number of elements copied.
	 * The <code>Iterator</code> will be fully exhausted to construct the array.
	 * 
	 * @param iter
	 *            the <code>Iterator</code> to copy records from
	 * @param a
	 *            the array to copy records to
	 * @return the final array
	 */
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

    /**
	 * Copy the contents of the provided <code>Iterable</code> into an array of the provided type. The resulting array's size will be the
	 * exact number of elements retrieved from the <code>Iterable</code>.
	 * 
	 * @param iter
	 *            the <code>Iterable</code> to copy records from
	 * @param clazz
	 *            the type of array to copy the records to
	 * @return the final array
	 */
    public static <T> T[] toArray(Iterable<?> iter, Class<T> clazz) {
		return toArray(iter.iterator(), clazz) ;
    }

    /**
	 * Copy the contents of the provided <code>Iterator</code> into an array of the provided type. The resulting array's size will be the
	 * exact number of elements retrieved from the <code>Iterator</code>. The <code>Iterator</code> will be fully exhausted to construct the
	 * array.
	 * 
	 * @param iter
	 *            the <code>Iterator</code> to copy records from
	 * @param clazz
	 *            the type of array to copy the records to
	 * @return the final array
	 */
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
	 * Create an <code>Object[]</code> from the supplied <code>Iterable</code>
	 * 
	 * @param iter
	 *            an <code>Iterable</code>
	 * @return an <code>Object[] </code>of the contents of the supplied <code>Iterator</code>
	 */
	public static Object[] toArray(Iterable<?> iter) {
		return toArray(iter.iterator()) ;
    }

	/**
	 * Create an <code>Object[]</code> from the supplied <code>Iterator</code>. The <code>Iterator</code> is fully exhausted to construct
	 * the array.
	 * 
	 * @param iter
	 *            an <code>Iterator</code>
	 * @return an <code>Object[] </code>of the contents of the supplied <code>Iterator</code>
	 */
	public static Object[] toArray(Iterator<?> iter) {
		return toArray(iter, Object.class) ;
    }

	/**
	 * Create an <code>Arraylist</code> from the supplied <code>Iterable</code>
	 * 
	 * @param iter
	 *            an <code>Iterable</code>
	 * @return an <code>Arraylist</code> of the contents of the supplied <code>Iterable</code>
	 */
	public static <E> List<E> toList(Iterable<E> iter) {
		return toList(iter.iterator()) ; 
	}

	/**
	 * Create an <code>Arraylist</code> from the supplied <code>Iterator</code>. The <code>Iterator</code> is fully exhausted to construct
	 * the list.
	 * 
	 * @param iter
	 *            an <code>Iterator</code>
	 * @return an <code>Arraylist</code> of the contents of the supplied <code>Iterator</code>
	 */
	public static <E> List<E> toList(Iterator<E> iter) {
		List<E> result = new ArrayList<E>() ;
		toList(iter, result) ;
		return result ;
	}
	
	/**
	 * Exhaust the provided <code>Iterator</code>, copying its contents into the provided <code>List</code>
	 * 
	 * @param iter
	 *            source
	 * @param list
	 *            target
	 */
	public static <E> void toList(Iterator<E> iter, List<E> list) {
		while(iter.hasNext()) 
			list.add(iter.next()) ;
	}
	
    /**
	 * Eagerly convert the provided <code>List</code> into a <code>List</code> of type <code>String</code>, by calling the
	 * <code>toString()</code> method on each item. If <code>replaceNulls</code> is <code>true</code> then <code>null</code> values will be
	 * replaced by the <code>String</code> <code>"null"</code>, otherwise they will remain as <code>null</code>.
	 * 
	 * @param list
	 *            the iterator to convert to strings
	 * @param replaceNulls
	 *            replace <code>null</code> with <code>"null"</code> if <code>true</code>
	 * @return a <code>List</code> of strings constructed from the provided <code>List</code>
	 */
    public static List<String> toStrings(List<?> list, boolean replaceNulls) {
    	return Functions.apply(Functions.<Object>toString(replaceNulls), list) ;
    }
    
    /**
	 * Lazily convert the provided <code>Iterable</code> into an <code>Iterable</code> of type <code>String</code>, by calling the
	 * <code>toString()</code> method on each item. If <code>replaceNulls</code> is <code>true</code> then <code>null</code> values will be
	 * replaced by the <code>String</code> <code>"null"</code>, otherwise they will remain as <code>null</code>.
	 * 
	 * @param iter
	 *            the <code>Iterable</code> to convert to strings
	 * @param replaceNulls
	 *            replace <code>null</code> with <code>"null"</code> if <code>true</code>
	 * @return an <code>Iterable</code> lazily converting the values of the one provided to strings
	 */
    public static Iterable<String> toStrings(Iterable<?> iter, boolean replaceNulls) {
    	return Functions.apply(Functions.<Object>toString(replaceNulls), iter) ;
    }

    /**
	 * Lazily convert the provided <code>Iterator</code> into an <code>Iterator</code> of type <code>String</code>, by calling the
	 * <code>toString()</code> method on each item. If <code>replaceNulls</code> is <code>true</code> then <code>null</code> values will be
	 * replaced by the <code>String</code> <code>"null"</code>, otherwise they will remain as <code>null</code>.
	 * 
	 * @param iter
	 *            the iterator to convert to strings
	 * @param replaceNulls 
	 *            replace <code>null</code> with <code>"null"</code> if <code>true</code>
	 * @return an <code>Iterator</code> lazily converting the values of the one provided to strings
	 */
    public static Iterator<String> toStrings(Iterator<?> iter, boolean replaceNulls) {
    	return Functions.apply(Functions.<Object>toString(replaceNulls), iter) ;
    }
    
    /**
	 * Lazily convert the provided <code>Iterator</code> into an <code>Iterator</code> of type <code>String</code>, by calling the
	 * <code>toString()</code> method on each item. If <code>replaceNulls</code> is <code>true</code> then <code>null</code> values will be
	 * replaced by the <code>String</code> <code>"null"</code>, otherwise they will remain as <code>null</code>.
	 * 
	 * @param iter
	 *            the iterator to convert to strings
	 * @param replaceNulls 
	 *            replace <code>null</code> with <code>"null"</code> if <code>true</code>
	 * @return an <code>Iterator</code> lazily converting the values of the one provided to strings
	 */
    public static ClosableIterator<String> toStrings(ClosableIterator<?> iter, boolean replaceNulls) {
    	return Functions.apply(Functions.<Object>toString(replaceNulls), iter) ;
    }
    
    /**
	 * Returns a string representation of the argument of the form produced by java.util collections
	 * 
	 * @param iter
	 *            an <code>Iterable</code>
	 * @return a string representation of the argument
	 */
    public static String toString(Iterable<?> iter) {
    	return "[" + toString(iter.iterator(), ", ") + "]" ;
    }
    
    /**
	 * Returns a string representation of the argument of the form produced by java.util collections. The <code>Iterator</code> is fully
	 * exhausted in the construction of this string.
	 * 
	 * @param iter
	 *            an <code>Iterator</code>
	 * @return a string representation of the argument
	 */
    public static String toString(Iterator<?> iter) {
    	return "[" + toString(iter, ", ") + "]" ;
    }
    
    /**
	 * Concatenates the elements of the provided <code>Iterable</code> using the provided delimiter
	 * 
	 * @param iter
	 *            an <code>Iterable</code>
	 * @param delimiter
	 *            the delimiter
	 * @return a string representation of the argument
	 */
    public static String toString(Iterable<?> iter, String delimiter) {
    	return toString(iter.iterator(), delimiter) ;
    }
    /**
	 * Concatenates the elements of the provided <code>Iterator</code> using the provided delimiter. The <code>Iterator</code> is fully
	 * exhausted by the construction of this string.
	 * 
	 * @param iter
	 *            an <code>Iterator</code>
	 * @param delimiter
	 *            the delimiter
	 * @return a string representation of the argument
	 */
    @SuppressWarnings("unchecked")
	public static String toString(Iterator<?> iter, String delimiter) {
    	StringBuffer buffer = new StringBuffer() ;
    	boolean doneFirst = false ;
    	while (iter.hasNext()) {
    		if (doneFirst) buffer.append(delimiter) ;
    		final Object next = iter.next() ;
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
