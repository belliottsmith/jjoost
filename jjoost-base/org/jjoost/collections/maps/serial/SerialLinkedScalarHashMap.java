package org.jjoost.collections.maps.serial;

import java.util.Map.Entry ;

import org.jjoost.collections.ScalarMap ;
import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.SerialLinkedHashStore ;
import org.jjoost.collections.base.SerialLinkedHashStore.SerialLinkedHashNode ;
import org.jjoost.collections.maps.base.HashMapNodeFactory ;
import org.jjoost.collections.maps.base.ScalarHashMap ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class SerialLinkedScalarHashMap<K, V> extends ScalarHashMap<K, V, SerialLinkedScalarHashMap.Node<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialLinkedScalarHashMap() {
		this(16, 0.75f) ;
	}
	
	public SerialLinkedScalarHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	
	public SerialLinkedScalarHashMap( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super K> keyHasher, 
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(keyHasher, rehasher, new KeyEquality<K, V>(keyEquality), new EntryEquality<K, V>(keyEquality, valEquality),
			SerialLinkedScalarHashMap.<K, V>factory(), 
			new SerialLinkedHashStore<Node<K, V>>(minimumInitialCapacity, loadFactor)) ;
	}

	public static final class Node<K, V> extends SerialLinkedHashNode<Node<K, V>> implements Entry<K, V> {
		private static final long serialVersionUID = -5766263745864028747L;
		public Node(int hash, K key, V value) {
			super(hash);
			this.key = key;
			this.value = value;
		}
		private final K key ;
		private V value ;		
		@Override public K getKey() { return key ; }
		@Override public V getValue() { return value ; }
		@Override public V setValue(V value) { final V r = this.value ; this.value = value ; return r ; }
		@Override public Node<K, V> copy() { return new Node<K, V>(hash, key, value) ; }
	}
	
	@SuppressWarnings("unchecked")
	private static final NodeFactory FACTORY = new NodeFactory() ;
	@SuppressWarnings("unchecked")
	public static <K, V> NodeFactory<K, V> factory() {
		return FACTORY ;
	}
	public static final class NodeFactory<K, V> implements HashMapNodeFactory<K, V, Node<K, V>> {
		@Override
		public final Node<K, V> makeNode(final int hash, final K key, final V value) {
			return new Node<K, V>(hash, key, value) ;
		}
	}

	public static void main(String[] args) {
		ScalarMap<Integer, Integer> map = new SerialLinkedScalarHashMap<Integer, Integer>() ;
		for (int i = 0 ; i != 100 ; i++) {
			map.put(i, i) ;
			map.put(i, i + 1) ;
			map.put(i, i) ;
		}
		for (Integer i : map.values()) {
			System.out.println(i) ;
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
