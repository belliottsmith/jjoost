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

import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Set;

public class SynchronizedMultiSet<V> extends SynchronizedArbitrarySet<V, MultiSet<V>> implements MultiSet<V> {
	
	private static final long serialVersionUID = -8766973234275059454L;
	
	public SynchronizedMultiSet(MultiSet<V> delegate) {		
		super(delegate);
	}
	
	@Override public synchronized MultiSet<V> copy() {
		return new SynchronizedMultiSet<V>(delegate.copy());
	}

	@Override public synchronized void put(V val, int numberOfTimes) {
		delegate.put(val, numberOfTimes);
	}

	@Override
	public synchronized Set<V> unique() {
		return wrap(delegate.unique());
	}
	
}
