package org.jjoost.collections.sets.wrappers;

import java.util.Collection ;
import java.util.Iterator ;

import org.jjoost.collections.Set ;
import org.jjoost.util.Iters ;

public class AdapterToJDKSet<V> implements java.util.Set<V> {

	private final Class<V> clazz ;
	private final Set<V> set ;
	
	public AdapterToJDKSet(Class<V> clazz, Set<V> set) {
		super() ;
		this.clazz = clazz ;
		this.set = set ;
	}

	@Override
	public boolean add(V v) {
		if (v == null) {
			if (set.contains(null))
				return false ; 
			set.put(null) ;
			return true ;
		} else {
			final V e = set.put(v) ;
			return e == null ;
		}
	}

	@Override
	public boolean addAll(Collection<? extends V> c) {
		boolean r = false ;
		for (V v : c) {
			r |= add(v) ;
		}
		return r ;
	}

	@Override
	public void clear() {
		set.clear() ;
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(clazz.cast(o)) ;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		boolean r = true ;
		Iterator<?> iter = c.iterator() ;
		while (r && iter.hasNext())
			r = contains(iter.next()) ;
		return r ;
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty() ;
	}

	@Override
	public Iterator<V> iterator() {
		return set.iterator() ;
	}

	@Override
	public boolean remove(Object o) {
		return set.remove(clazz.cast(o)) != 0 ;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean r = false ;
		for (Object o : c)
			r |= remove(o) ;
		return r ;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean r = false ;
		Iterator<V> vs = iterator() ;
		while (vs.hasNext()) {
			if (c.contains(vs.next())) {
				vs.remove() ;
				r = true ;
			}
		}
		return r ;
	}

	@Override
	public int size() {
		return set.size() ;
	}

	@Override
	public Object[] toArray() {
		return Iters.toArray(this) ;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return Iters.toArray(this, a) ;
	}
	

}
