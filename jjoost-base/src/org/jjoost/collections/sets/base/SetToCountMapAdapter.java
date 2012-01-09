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
import java.util.List;
import java.util.Map.Entry;

import org.jjoost.collections.AnyReadSet;
import org.jjoost.collections.AnySet;
import org.jjoost.collections.ReadMap;
import org.jjoost.collections.ReadSet;
import org.jjoost.collections.UnitaryReadSet;
import org.jjoost.collections.maps.ImmutableMapEntry;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SetToCountMapAdapter<V> implements ReadMap<V, Integer> {

	private static final long serialVersionUID = 7089328142843569432L;
	
	final AnyReadSet<V> set;
	public SetToCountMapAdapter(AnyReadSet<V> set) {
		this.set = set;
	}

	@Override
	public Integer get(V key) {
		return set.count(key);
	}

	@Override
	public int size() {
		return set.uniqueCount();
	}

	@Override
	public AnySet<Integer> values() {
		return null;
	}

	@Override
	public Integer apply(V v) {
		return get(v);
	}

	@Override
	public boolean contains(V key, Integer val) {
		final int count = set.count(key);
		if (count == 0)
			return val == null || val == 0;
		return val != null && val == count;
	}

	@Override
	public boolean contains(V key) {
		return set.contains(key);
	}

	@Override
	public int count(V key, Integer val) {
		if (contains(key, val))
			return 1;
		return 0;
	}

	@Override
	public int count(V key) {
		if (set.contains(key))
			return 1;
		return 0;
	}

	@Override
	public Iterable<Entry<V, Integer>> entries(V key) {
		return Arrays.asList((Entry<V, Integer>) new ImmutableMapEntry<V, Integer>(key, set.count(key)));
	}

	@Override
	public Integer first(V key) { 
		return get(key);
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public List<Integer> list(V key) {
		return Arrays.asList(get(key));
	}

	@Override
	public boolean permitsDuplicateKeys() {
		return false;
	}

	@Override
	public int totalCount() {
		return set.uniqueCount();
	}

	@Override
	public int uniqueKeyCount() {
		return set.uniqueCount();
	}

	@Override
	public ReadSet<V> keys() {
		return set.unique();
	}

	@Override
	public UnitaryReadSet<Integer> values(final V key) {
		return new ImmutableUnitarySet<Integer>() {
			private static final long serialVersionUID = -8038795621421728832L;
			protected Integer value() {
				return set.count(key);
			}
			@Override
			public Equality<? super Integer> equality() {
				return Equalities.object();
			}
		};
	}

	@Override
	public ReadMap<V, Integer> copy() {
		return set.copy().asMap();
	}

	@Override
	public ReadSet<Entry<V, Integer>> entries() {
		throw new NotImplementedException();
	}

}
