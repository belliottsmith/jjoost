package org.jjoost.collections.lists ;

import java.io.Serializable;
import java.util.* ;

/**
 * A cheap (memory-wise) way of representing a list with precisely one distinct kind of element in it (which may be null).
 * 
 * @author b.elliottsmith
 */
public class UniformList<E> implements List<E>, Serializable {

	private static final long serialVersionUID = 5099043265610517517L ;
	
	private final E element ;
	private int size ;
	
	public UniformList(E element, int size) {
		this.element = element ;
		this.size = size ;
	}

	public int size() {
		return size ;
	}

	public boolean isEmpty() {
		return size != 0 ;
	}

	public boolean contains(Object o) {
		return size > 0 && (element == o || element != null && o != null && element.equals(o)) ;
	}

	public String toString() {
		return "[" + element + "]^" + size ;
	}

	public Iterator<E> iterator() {
		return new Iterator<E>() {

			private int i = 0 ;

			public boolean hasNext() {
				return i < size ;
			}

			public E next() {
				if (i >= size)
					throw new NoSuchElementException() ;
				i++ ;
				return element ;
			}

			public void remove() {
				size-- ;
			}
		} ;
	}

	@SuppressWarnings("unchecked")
	public Object[] toArray() {
		E[] ret = (E[]) new Object[size] ;
		Arrays.fill(ret, element) ;
		return ret ;
	}

	public <T> T[] toArray(T[] a) {
		Arrays.fill(a, 0, size, element) ;
		return a ;
	}

	public boolean add(E e) {		
		if (element == e) {
			size++ ;
			return true ;
		}
		return false ;
	}

	public void add() {
		size++ ;
	}

	public boolean remove(Object o) {
		if (element.equals(o) && size > 0) {
			size-- ;
			return true ;
		}
		return false ;
	}

	public boolean containsAll(Collection<?> c) {
		for (Object o : c)
			if (!contains(o))
				return false ;
		return true ;
	}

	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException() ;
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException() ;
	}

	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException() ;
	}

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException() ;
	}

	public void clear() {
		size = 0 ;
	}

	public E get(int index) {
		return element ;
	}

	public E set(int index, E element) {
		if (index < size && this.element == element)
			return element ;
		return null ;
	}

	public void add(int index, E element) {
		if (index < size && this.element == element) {
			size++ ;
		}
		throw new IllegalArgumentException("Can only add " + element + " to this list") ;
	}

	public E remove(int index) {
		if (index < size) {
			size-- ;
			return element ;
		}
		throw new IndexOutOfBoundsException() ;
	}

	public int indexOf(Object o) {
		if (element.equals(0))
			return 0 ;
		return -1 ;
	}

	public int lastIndexOf(Object o) {
		if (element.equals(0))
			return size - 1 ;
		return -1 ;
	}

	public ListIterator<E> listIterator() {
		throw new UnsupportedOperationException() ;
	}

	public ListIterator<E> listIterator(int index) {
		throw new UnsupportedOperationException() ;
	}

	public List<E> subList(int fromIndex, int toIndex) {
		return new UniformList<E>(element, toIndex - fromIndex) ;
	}

}
