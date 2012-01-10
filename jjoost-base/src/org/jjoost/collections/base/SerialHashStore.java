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

package org.jjoost.collections.base;

import org.jjoost.util.Function;

public final class SerialHashStore<N extends AbstractSerialHashStore.SerialHashNode<N>> extends AbstractSerialHashStore<N, SerialHashStore<N>> {

	private static final long serialVersionUID = 5818748848600569496L;
	
	public SerialHashStore(float loadFactor, N[] table, int totalNodeCount,
			int uniquePrefixCount) {
		super(loadFactor, table, totalNodeCount, uniquePrefixCount);
	}


	public SerialHashStore(int size, float loadFactor) {
		super(size, loadFactor);
	}


	@Override
	public <NCmp> SerialHashStore<N> copy(Function<? super N, ? extends NCmp> nodeEqualityProj,
		HashNodeEquality<? super NCmp, ? super N> nodeEquality) {
		final N[] table = this.table.clone();
		for (int i = 0 ; i != table.length ; i++) {
			N orig = table[i];
			if (orig != null) {
				N copy = orig.copy();
				table[i] = copy;
				orig = orig.next;
				while (orig != null) {
					copy.next = copy = orig.copy();
					orig = orig.next;
				}
			}
		}
		return new SerialHashStore<N>(loadFactor, table, totalNodeCount, uniquePrefixCount);
	}

}
