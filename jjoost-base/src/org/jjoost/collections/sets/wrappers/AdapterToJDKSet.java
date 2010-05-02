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
		return set.add(v) ;
	}

	@Override
	public boolean addAll(Collection<? extends V> c) {
		boolean r = false ;
		for (V v : c) {
			r |= set.add(v) ;
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
