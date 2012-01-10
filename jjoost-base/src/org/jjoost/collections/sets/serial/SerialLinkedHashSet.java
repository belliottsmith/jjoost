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
import org.jjoost.collections.base.SerialLinkedHashStore;
import org.jjoost.collections.sets.base.AbstractHashSet;
import org.jjoost.collections.sets.base.HashSet;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SerialLinkedHashSet<V> extends HashSet<V, AbstractHashSet.SerialLinkedHashSetNode<V>, SerialLinkedHashStore<AbstractHashSet.SerialLinkedHashSetNode<V>>> {

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialLinkedHashSet() {
		this(16, 0.75f);
	}
	public SerialLinkedHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object());
	}
	
	public SerialLinkedHashSet(Equality<? super V> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality);
	}
	
	public SerialLinkedHashSet(Rehasher rehasher, Equality<? super V> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality);
	}
	
	public SerialLinkedHashSet( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(rehasher, keyEquality, 
			AbstractHashSet.<V>serialLinkedNodeFactory(), 
			new SerialLinkedHashStore<SerialLinkedHashSetNode<V>>(minimumInitialCapacity, loadFactor));
	}
	
	public V removeOldest() {
		final SerialLinkedHashSetNode<V> n = ((SerialLinkedHashStore<SerialLinkedHashSetNode<V>>) store).oldest();
		if (n != null) {
			store.removeNode(valProj(), valEq, n);
			return n.getValue();
		}
		return null;
	}
	
}
