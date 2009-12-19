package org.jjoost.collections.maps.concurrent;

import java.util.Map.Entry ;

import org.jjoost.collections.base.LockFreeHashStore ;
import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.maps.base.HashMapNodeFactory ;
import org.jjoost.collections.maps.base.ScalarHashMap ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class LockFreeScalarHashMap<K, V> extends ScalarHashMap<K, V, LockFreeScalarHashMap.Node<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public LockFreeScalarHashMap() {
		this(16, 0.75f) ;
	}
	public LockFreeScalarHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	
	public LockFreeScalarHashMap( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super K> keyHasher, 
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(keyHasher, rehasher, new KeyEquality<K, V>(keyEquality), new EntryEquality<K, V>(keyEquality, valEquality),
			LockFreeScalarHashMap.<K, V>factory(), 
			new LockFreeHashStore<Node<K, V>>(minimumInitialCapacity, loadFactor, LockFreeHashStore.Counting.PRECISE, LockFreeHashStore.Counting.OFF)) ;
	}
	
	public static final class Node<K, V> extends LockFreeHashStore.LockFreeHashNode<Node<K, V>> implements Entry<K, V> {
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
	}
	
	@SuppressWarnings("unchecked")
	private static final SerialScalarHashNodeFactory FACTORY = new SerialScalarHashNodeFactory() ;
	@SuppressWarnings("unchecked")
	public static <K, V> SerialScalarHashNodeFactory<K, V> factory() {
		return FACTORY ;
	}
	public static final class SerialScalarHashNodeFactory<K, V> implements HashMapNodeFactory<K, V, Node<K, V>> {
		@Override
		public final Node<K, V> makeNode(final int hash, final K key, final V value) {
			return new Node<K, V>(hash, key, value) ;
		}
	}

	public static final class KeyEquality<K, V> extends ScalarHashMap.KeyEquality<K, V, Node<K, V>> {
		public KeyEquality(Equality<? super K> keyEq) {
			super(keyEq) ;
		}
		@Override
		public boolean prefixMatch(K cmp, Node<K, V> n) {
			return keyEq.equates(cmp, n.key) ;
		}
	}

	public static final class EntryEquality<K, V> extends ScalarHashMap.EntryEquality<K, V, Node<K, V>> {
		private static final long serialVersionUID = -8668943955126687051L ;

		public EntryEquality(Equality<? super K> keyEq, Equality<? super V> valEq) {
			super(keyEq, valEq) ;
		}
		@Override
		public boolean equates(Node<K, V> a, Node<K, V> b) {
			return keyEq.equates(a.key, b.key) && valEq.equates(a.value, b.value) ;
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
