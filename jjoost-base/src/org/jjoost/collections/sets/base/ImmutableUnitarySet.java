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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jjoost.collections.UnitaryReadSet;
import org.jjoost.collections.lists.UniformList;

public abstract class ImmutableUnitarySet<V> extends AbstractSet<V> implements UnitaryReadSet<V> {

	private static final long serialVersionUID = 924641628585159754L;

	protected abstract V value() ;

	@Override
	public V get() {
		return value() ;
	}

	@Override
	public UnitaryReadSet<V> unique() {
		return this ;
	}

	@Override
	public Iterator<V> iterator() {
		return new UniformList<V>(value(), 1).iterator() ;
	}

	@Override
	public Iterable<V> all(V find) {
		if (equality().equates(find, value()))
			return new UniformList<V>(value(), 1) ;
		return Collections.emptyList() ;
	}

	@Override
	public boolean contains(V find) {
		return equality().equates(find, value()) ;
	}

	@Override
	public int count(V find) {
		if (equality().equates(find, value()))
			return 1 ;
		return 0 ;
	}

	@Override
	public V first(V find) {
		if (equality().equates(find, value()))
			return value() ;
		return null ;
	}

	@Override
	public boolean isEmpty() {
		return false ;
	}

	@Override
	public List<V> list(V find) {
		if (equality().equates(find, value()))
			return new UniformList<V>(value(), 1) ;
		return Collections.emptyList() ;
	}

	@Override
	public boolean permitsDuplicates() {
		return false ;
	}

	@Override
	public int totalCount() {
		return 1 ;
	}

	@Override
	public int uniqueCount() {
		return 1 ;
	}

	@Override
	public Boolean apply(V v) {
		return contains(v) ? Boolean.TRUE : Boolean.FALSE ;
	}

}
