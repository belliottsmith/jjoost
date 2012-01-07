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

import java.io.Serializable;

public abstract class HashNode<N extends HashNode<N>> implements Serializable {
	
	private static final long serialVersionUID = 2035712133283347382L;
	public final int hash;
	public HashNode(int hash) {
		this.hash = hash;
	}
	public abstract N copy();
	public final int hash() { return hash ; }
	
	public static boolean insertBefore(int reverseHash, HashNode<?> node) {
		final int reverseNodeHash = Integer.reverse(node.hash);
		return (reverseHash < reverseNodeHash) ^ ((reverseNodeHash > 0) != (reverseHash > 0));
	}

	public static boolean insertBefore(HashNode<?> a, HashNode<?> b) {
		final int ra = Integer.reverse(a.hash);
		final int rb = Integer.reverse(b.hash);
		return (ra < rb) ^ ((ra > 0) != (rb > 0));
	}
	
}

