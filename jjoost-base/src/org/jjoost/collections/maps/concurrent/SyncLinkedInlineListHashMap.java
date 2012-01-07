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

package org.jjoost.collections.maps.concurrent;

import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.base.SerialLinkedHashStore;
import org.jjoost.collections.base.SynchronizedHashStore;
import org.jjoost.collections.maps.base.InlineListHashMap;
import org.jjoost.collections.maps.serial.SerialLinkedInlineListHashMap;
import org.jjoost.collections.maps.serial.SerialLinkedInlineListHashMap.Node;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SyncLinkedInlineListHashMap<K, V> extends InlineListHashMap<K, V, SerialLinkedInlineListHashMap.Node<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SyncLinkedInlineListHashMap() {
		this(16, 0.75f);
	}
	public SyncLinkedInlineListHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object());
	}
	public SyncLinkedInlineListHashMap(Equality<? super K> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality);
	}	
	public SyncLinkedInlineListHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object());
	}	
	public SyncLinkedInlineListHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality);
	}
	
	public SyncLinkedInlineListHashMap( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(rehasher, new SerialLinkedInlineListHashMap.KeyEquality<K, V>(keyEquality), new SerialLinkedInlineListHashMap.EntryEquality<K, V>(keyEquality, valEquality),
			SerialLinkedInlineListHashMap.<K, V>factory(), 
			new SynchronizedHashStore<Node<K, V>>(new SerialLinkedHashStore<Node<K, V>>(minimumInitialCapacity, loadFactor)));
	}

}
