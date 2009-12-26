package org.jjoost.collections.sets.serial;

import java.util.ArrayList ;
import java.util.List ;

import org.jjoost.collections.Set ;
import org.jjoost.collections.sets.base.AbstractArraySet ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Iters ;

public class ScalarArraySet<V> extends AbstractArraySet<V> implements Set<V> {

	private static final long serialVersionUID = 7815149592942049121L ;

	public ScalarArraySet(int initialCapacity) {
		super(initialCapacity) ;
	}
	
	public ScalarArraySet(int initialCapacity, Equality<? super V> valEq) {
		super(initialCapacity, valEq) ;
	}

	protected ScalarArraySet(V[] vals, int count, Equality<? super V> valEq) {
		super(vals, count, valEq) ;
	}

	private int indexOf(V v) {
		for (int i = 0 ; i != count ; i++)
			if (valEq.equates(v, vals[i]))
				return i ;
		return count ;
	}

	@Override
	public Set<V> copy() {
		return new ScalarArraySet<V>(vals.clone(), count, valEq) ;
	}

	@Override
	public V put(V v) {
		final int i = indexOf(v) ;
		if (i == count) {
			ensureIndex(i) ;
			vals[i] = v ;
			count = i + 1 ;
			return null ;
		} else return vals[i] ;
	}
	
	@Override
	public V get(V v) {
		final int i = indexOf(v) ;
		return i == count ? null : vals[i] ;
	}

	@Override
	public int size() {
		return count ;
	}

	@Override
	public int putAll(Iterable<V> vs) {
		final int oldc = count ;
		for (V v : vs) {
			final int i = indexOf(v) ;
			if (i == count) {
				ensureIndex(i) ;
				vals[i] = v ;
				count = i + 1 ;
			}
		}
		return count - oldc ;
	}

	@Override
	public Iterable<V> all(V v) {
		return list(v) ;
	}

	@Override
	public List<V> list(V v) {
		List<V> r = new ArrayList<V>() ;
		for (int i = 0 ; i != count ; i++)
			if (valEq.equates(v, vals[i]))
				r.add(v) ;
		return r ;
	}

	@Override
	public boolean permitsDuplicates() {
		return false ;
	}

	@Override
	public Set<V> unique() {
		return this ;
	}

	@Override
	public int uniqueCount() {
		return Iters.count(unique()) ;
	}
	
}
