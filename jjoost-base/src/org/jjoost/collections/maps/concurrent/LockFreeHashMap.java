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

import java.util.Map.Entry ;

import org.jjoost.collections.base.LockFreeHashStore ;
import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.maps.base.HashMapNodeFactory ;
import org.jjoost.collections.maps.base.HashMap ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class LockFreeHashMap<K, V> extends HashMap<K, V, LockFreeHashMap.Node<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public LockFreeHashMap() {
		this(16, 0.75f) ;
	}
	public LockFreeHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	public LockFreeHashMap(Equality<? super K> keyEquality) {
		this(LockFreeHashStore.defaultRehasher(), keyEquality) ;
	}	
	public LockFreeHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object()) ;
	}	
	public LockFreeHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality) ;
	}

	public LockFreeHashMap( 
			int minimumInitialCapacity, float loadFactor,
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(rehasher, new KeyEquality<K, V>(keyEquality), new NodeEquality<K, V>(keyEquality, valEquality),
			LockFreeHashMap.<K, V>factory(), 
			new LockFreeHashStore<Node<K, V>>(minimumInitialCapacity, loadFactor, LockFreeHashStore.Counting.PRECISE, LockFreeHashStore.Counting.OFF)) ;
	}
	
	protected static final class Node<K, V> extends LockFreeHashStore.LockFreeHashNode<Node<K, V>> implements Entry<K, V> {
		private static final long serialVersionUID = -5766263745864028747L;
		public Node(int hash, K key, V value) {
			super(hash);
			this.key = key;
			this.value = value;
		}
		protected final K key ;
		protected V value ;		
		@Override public final K getKey() { return key ; }
		@Override public final V getValue() { return value ; }
		@Override public final V setValue(V value) { final V r = this.value ; this.value = value ; return r ; }
		@Override public final Node<K, V> copy() { return new Node<K, V>(hash, key, value) ; }
		@Override public String toString() { return "{" + key + " -> " + value + "}" ; }
	}
	
	@SuppressWarnings("unchecked")
	private static final SerialScalarHashNodeFactory FACTORY = new SerialScalarHashNodeFactory() ;
	@SuppressWarnings("unchecked")
	public static <K, V> SerialScalarHashNodeFactory<K, V> factory() {
		return FACTORY ;
	}
	protected static final class SerialScalarHashNodeFactory<K, V> implements HashMapNodeFactory<K, V, Node<K, V>> {
		@Override
		public final Node<K, V> makeNode(final int hash, final K key, final V value) {
			return new Node<K, V>(hash, key, value) ;
		}
	}

	protected static final class KeyEquality<K, V> extends HashMap.KeyEquality<K, V, Node<K, V>> {
		public KeyEquality(Equality<? super K> keyEq) {
			super(keyEq) ;
		}
		@Override
		public boolean prefixMatch(K cmp, Node<K, V> n) {
			return keyEq.equates(cmp, n.key) ;
		}
	}

	protected static final class NodeEquality<K, V> extends HashMap.NodeEquality<K, V, Node<K, V>> {
		private static final long serialVersionUID = -8668943955126687051L ;

		public NodeEquality(Equality<? super K> keyEq, Equality<? super V> valEq) {
			super(keyEq, valEq) ;
		}
		@Override
		public boolean prefixMatch(Entry<K, V> cmp, Node<K, V> n) {
			return keyEq.equates(cmp.getKey(), n.key) ;
		}

		@Override
		public boolean suffixMatch(Entry<K, V> cmp, Node<K, V> n) {
			return valEq.equates(cmp.getValue(), n.value) ;
		}
	}
	
}
