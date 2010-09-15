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

package org.jjoost.collections.sets.base;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.ReadMap;
import org.jjoost.collections.Set;
import org.jjoost.collections.UnitarySet;
import org.jjoost.collections.lists.UniformList;
import org.jjoost.collections.maps.ImmutableMapEntry;

public abstract class AnySetToCountsAdapter<V> implements ReadMap<V, Integer> {
	
	private static final long serialVersionUID = 5610603388897525494L;

	protected abstract MultiSet<V> set() ;
	public abstract Iterator<Entry<V, Integer>> clearAndReturn() ;
	protected abstract Iterator<Entry<V, Integer>> entryIterator() ;
	protected abstract Entry<V, Integer> getEntry() ;
	
	@Override
	public Set<Entry<V, Integer>> entries() {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public Set<V> keys() {
		return set().unique() ;
	}

	@Override
	public ReadMap<V, Integer> copy() {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public Integer get(V key) {
		return set().count(key) ;
	}

	@Override
	public int size() {
		return set().uniqueCount() ;
	}

	@Override
	public UnitarySet<Integer> values(V key) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public AnySet<Integer> values() {
		return null ;
	}

	@Override
	public Integer apply(V v) {
		return get(v) ;
	}

	@Override
	public boolean contains(V key, Integer val) {
		final int count = set().count(key) ;
		if (count == 0)
			return val == null || val == 0 ;
		return val != null && val == count ;
	}

	@Override
	public boolean contains(V key) {
		return set().contains(key) ;
	}

	@Override
	public int count(V key, Integer val) {
		if (contains(key, val))
			return 1 ;
		return 0 ;
	}

	@Override
	public int count(V key) {
		if (set().contains(key))
			return 1 ;
		return 0 ;
	}

	@Override
	public Iterable<Entry<V, Integer>> entries(V key) {
		return new UniformList<Entry<V, Integer>>(new ImmutableMapEntry<V, Integer>(key, set().count(key)), 1) ;
	}

	@Override
	public Integer first(V key) { 
		return get(key) ;
	}

	@Override
	public boolean isEmpty() {
		return set().isEmpty() ;
	}

	@Override
	public List<Integer> list(V key) {
		return Arrays.asList(get(key)) ;
	}

	@Override
	public boolean permitsDuplicateKeys() {
		return false ;
	}

	@Override
	public int totalCount() {
		return set().uniqueCount() ;
	}

	@Override
	public int uniqueKeyCount() {
		return set().uniqueCount() ;
	}
	
}
