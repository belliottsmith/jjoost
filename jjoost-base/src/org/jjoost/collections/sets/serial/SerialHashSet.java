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

import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.sets.base.AbstractHashSet;
import org.jjoost.collections.sets.base.HashSet;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SerialHashSet<V> extends HashSet<V, AbstractHashSet.SerialHashSetNode<V>, SerialHashStore<AbstractHashSet.SerialHashSetNode<V>>> {

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialHashSet() {
		this(16, 0.75f);
	}
	public SerialHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object());
	}
	
	public SerialHashSet(Equality<? super V> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality);
	}
	
	public SerialHashSet(Rehasher rehasher, Equality<? super V> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality);
	}
	
	public SerialHashSet( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(rehasher, keyEquality, 
			AbstractHashSet.<V>serialNodeFactory(), 
			new SerialHashStore<SerialHashSetNode<V>>(minimumInitialCapacity, loadFactor));
	}

}
