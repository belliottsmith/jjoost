package org.jjoost.collections.sets.base;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.jjoost.collections.AnySet ;
import org.jjoost.collections.iters.ArrayIterator ;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Equality ;

public abstract class AbstractArraySet<V> implements AnySet<V> {

	private static final long serialVersionUID = 6236060917384423908L ;
	protected final Equality<? super V> valEq ;
	protected V[] vals ;
	protected int count ;
	
	protected void ensureIndex(int i) {
		while (vals.length <= i) {
			vals = Arrays.copyOf(vals, vals.length << 1) ;
		}
	}
	
	public AbstractArraySet(int initialCapacity) {
		this(initialCapacity, Equalities.object()) ;
	}
	@SuppressWarnings("unchecked")
	public AbstractArraySet(int initialCapacity, Equality<? super V> valEq) {
		vals = (V[]) new Object[initialCapacity] ;
		this.valEq = valEq ;
	}

	protected AbstractArraySet(V[] vals, int count, Equality<? super V> valEq) {
		this.vals = vals ;
		this.count = count ;
		this.valEq = valEq ;
	}
	
	@Override
	public int clear() {
		final int c = count ;
		count = 0 ;
		return c ;
	}

	@Override
	public Iterator<V> clearAndReturn() {
		final int c = count ;
		count = 0 ;
		return new ArrayIterator<V>(vals.clone(), 0, c) ;
	}

	@Override
	public V putIfAbsent(V v) {
		for (int i = 0 ; i != count ; i++)
			if (valEq.equates(v, vals[i]))
				return vals[i] ;
		ensureIndex(count) ;
		vals[count] = v ;
		count += 1 ;
		return null ;
	}

	@Override
	public int remove(V v, int atMost) {
		if (atMost < 0)
			throw new IllegalArgumentException("Cannot delete fewer than zero elements") ;
		int del = 0 ;
		for (int i = 0 ; (del != atMost) & (i != count) ; i++) {
			if (valEq.equates(v, vals[i])) {
				del += 1 ;
			} else if (del != 0) {
				vals[i - del] = vals[i] ;
			}
		}
		count -= del ;
		return del ;
	}

	@Override
	public Iterable<V> removeAndReturn(V v, int atMost) {
		if (atMost < 0)
			throw new IllegalArgumentException("Cannot delete fewer than zero elements") ;
		List<V> deleted = new ArrayList<V>() ;
		int del = 0 ;
		for (int i = 0 ; (del != atMost) & (i != count) ; i++) {
			if (valEq.equates(v, vals[i])) {
				del += 1 ;
				deleted.add(v) ;
			} else if (del != 0) {
				vals[i - del] = vals[i] ;
			}
		}
		count -= del ;
		return deleted ;
	}

	@Override
	public V removeAndReturnFirst(V v, int atMost) {
		V deleted = null ;
		int del = 0 ;
		for (int i = 0 ; (del != atMost) & (i != count) ; i++) {
			if (valEq.equates(v, vals[i])) {
				if (del == 0)
					deleted = vals[i] ;
				del += 1 ;
			} else if (del != 0) {
				vals[i - del] = vals[i] ;
			}
		}
		count -= del ;
		return deleted ;
	}
	
	@Override
	public int remove(V value) {
		return remove(value, Integer.MAX_VALUE) ;
	}

	@Override
	public Iterable<V> removeAndReturn(V value) {
		return removeAndReturn(value, Integer.MAX_VALUE) ;
	}

	@Override
	public V removeAndReturnFirst(V value) {
		return removeAndReturnFirst(value, Integer.MAX_VALUE) ;
	}

	@Override
	public void shrink() {
		vals = Arrays.copyOf(vals, count < 1 ? 1 : count) ;
	}

	@Override
	public boolean contains(V v) {
		for (int i = 0 ; i != count ; i++)
			if (valEq.equates(v, vals[i]))
				return true ;
		return false ;
	}

	@Override
	public int count(V v) {
		int c = 0 ;
		for (int i = 0 ; i != count ; i++)
			if (valEq.equates(v, vals[i]))
				c++ ;
		return c ;
	}

	@Override
	public V first(V v) {
		for (int i = 0 ; i != count ; i++)
			if (valEq.equates(v, vals[i]))
				return vals[i] ;
		return null ;
	}

	@Override
	public boolean isEmpty() {
		return count == 0 ;
	}

	@Override
	public int totalCount() {
		return count ;
	}

	@Override
	public Iterator<V> iterator() {
		return new ArrayIterator<V>(vals, 0, count) ;
	}

	@Override
	public Boolean apply(V v) {
		return contains(v) ;
	}

	@Override
	public Equality<? super V> equality() {
		return valEq ;
	}

}
