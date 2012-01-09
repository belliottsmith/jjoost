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

import org.jjoost.collections.AnyReadSet;
import org.jjoost.util.Iters;

public abstract class ImmutableEmptySet<V> extends AbstractReadSet<V> implements AnyReadSet<V> {

	private static final long serialVersionUID = 103439040925077249L;

	@Override
	public abstract AnyReadSet<V> copy();

	@Override
	public Iterator<V> iterator() {
		return Iters.emptyIterator();
	}

	@Override
	public Iterable<V> all(V find) {
		return Iters.emptyIterable();
	}

	@Override
	public boolean contains(V find) {
		return false;
	}

	@Override
	public int count(V find) {
		return 0;
	}

	@Override
	public V first(V find) {
		return null;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public List<V> list(V find) {
		return Collections.emptyList();
	}

	@Override
	public int totalCount() {
		return 0;
	}

	@Override
	public int uniqueCount() {
		return 0;
	}

	@Override
	public Boolean apply(V v) {
		return Boolean.FALSE;
	}

}
