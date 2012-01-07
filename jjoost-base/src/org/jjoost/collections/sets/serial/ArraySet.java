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

package org.jjoost.collections.sets.serial;

import java.util.ArrayList;
import java.util.List;

import org.jjoost.collections.Set;
import org.jjoost.collections.sets.base.AbstractArraySet;
import org.jjoost.util.Equality;
import org.jjoost.util.Iters;

public class ArraySet<V> extends AbstractArraySet<V> implements Set<V> {

	private static final long serialVersionUID = 7815149592942049121L;

	public ArraySet(int initialCapacity) {
		super(initialCapacity);
	}
	
	public ArraySet(int initialCapacity, Equality<? super V> valEq) {
		super(initialCapacity, valEq);
	}

	protected ArraySet(V[] vals, int count, Equality<? super V> valEq) {
		super(vals, count, valEq);
	}

	private int indexOf(V v) {
		for (int i = 0 ; i != count ; i++)
			if (valEq.equates(v, vals[i]))
				return i;
		return count;
	}

	@Override
	public Set<V> copy() {
		return new ArraySet<V>(vals.clone(), count, valEq);
	}

	@Override
	public boolean add(V v) {
		final int i = indexOf(v);
		if (i == count) {
			ensureIndex(i);
			vals[i] = v;
			count = i + 1;
			return true;
		} else return false;
	}
	
	@Override
	public V put(V v) {
		final int i = indexOf(v);
		if (i == count) {
			ensureIndex(i);
			vals[i] = v;
			count = i + 1;
			return null;
		} else return vals[i];
	}
	
	@Override
	public V get(V v) {
		final int i = indexOf(v);
		return i == count ? null : vals[i];
	}

	@Override
	public int size() {
		return count;
	}

	@Override
	public int putAll(Iterable<V> vs) {
		final int oldc = count;
		for (V v : vs) {
			final int i = indexOf(v);
			if (i == count) {
				ensureIndex(i);
				vals[i] = v;
				count = i + 1;
			}
		}
		return count - oldc;
	}

	@Override
	public Iterable<V> all(V v) {
		return list(v);
	}

	@Override
	public List<V> list(V v) {
		List<V> r = new ArrayList<V>();
		for (int i = 0 ; i != count ; i++)
			if (valEq.equates(v, vals[i]))
				r.add(v);
		return r;
	}

	@Override
	public boolean permitsDuplicates() {
		return false;
	}

	@Override
	public Set<V> unique() {
		return this;
	}

	@Override
	public int uniqueCount() {
		return Iters.count(unique());
	}
	
}
