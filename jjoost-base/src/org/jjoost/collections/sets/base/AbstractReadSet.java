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

import org.jjoost.collections.AnyReadSet;
import org.jjoost.collections.AnySet;
import org.jjoost.collections.ReadMap;
import org.jjoost.util.Iters;

public abstract class AbstractReadSet<V> implements AnyReadSet<V> {
	
	private static final long serialVersionUID = -2269362435477906614L;

	@Override
	public String toString() {
		return "{" + Iters.toString(this, ", ") + "}";
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object that) {
		return this == that || (that instanceof AnySet && equals((AnySet<V>) that));
	}
	
	public boolean equals(AnySet<V> that) {
		if (that.totalCount() != this.totalCount())
			return false;
		if (that.permitsDuplicates() != this.permitsDuplicates())
			return false;
		// retain some type safety of equals(Object) by confirming equalities are "equal" before comparing sets
		if (!that.equality().equals(this.equality()))
			return false;
		if (permitsDuplicates()) {
			for (V v : that.unique()) {
				if (this.count(v) != that.count(v))
					return false;
			}
		} else {
			for (V v : that)
				if (!contains(v))
					return false;
		}
		return true;
	}
	
	@Override
	public final ReadMap<V, Integer> asMap() {
		return new SetToCountMapAdapter<V>(this);
	}

}
