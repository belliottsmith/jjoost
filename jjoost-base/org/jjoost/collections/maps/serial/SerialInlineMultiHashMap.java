package org.jjoost.collections.maps.serial;

import java.util.Map.Entry ;

import org.jjoost.collections.MultiMap ;
import org.jjoost.collections.base.SerialHashTable ;
import org.jjoost.collections.base.SerialHashTable.SerialHashNode ;
import org.jjoost.collections.maps.base.HashMapNodeFactory ;
import org.jjoost.collections.maps.base.InlineMultiHashMap ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class SerialInlineMultiHashMap<K, V> extends InlineMultiHashMap<K, V, SerialInlineMultiHashMap.SerialMultiHashMapNode<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialInlineMultiHashMap() {
		this(16, 0.75f) ;
	}
	
	public SerialInlineMultiHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashTable.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	
	public SerialInlineMultiHashMap( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super K> keyHasher, 
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(keyHasher, rehasher, new KeyEquality<K, V>(keyEquality), new EntryEquality<K, V>(keyEquality, valEquality),
			SerialInlineMultiHashMap.<K, V>serialNodeFactory(), 
			new SerialHashTable<SerialMultiHashMapNode<K, V>>(minimumInitialCapacity, loadFactor)) ;
	}

	public static final class SerialMultiHashMapNode<K, V> extends SerialHashNode<SerialMultiHashMapNode<K, V>> implements Entry<K, V> {
		private static final long serialVersionUID = -5766263745864028747L;
		public SerialMultiHashMapNode(int hash, K key, V value) {
			super(hash);
			this.key = key;
			this.value = value;
		}
		private final K key ;
		private V value ;		
		@Override public K getKey() { return key ; }
		@Override public V getValue() { return value ; }
		@Override public V setValue(V value) { throw new UnsupportedOperationException() ; }
		@Override public SerialMultiHashMapNode<K, V> copy() { return new SerialMultiHashMapNode<K, V>(hash, key, value) ; }
	}
	
	@SuppressWarnings("unchecked")
	private static final SerialMultiHashNodeFactory SERIAL_MULTI_HASH_NODE_FACTORY = new SerialMultiHashNodeFactory() ;
	@SuppressWarnings("unchecked")
	public static <K, V> SerialMultiHashNodeFactory<K, V> serialNodeFactory() {
		return SERIAL_MULTI_HASH_NODE_FACTORY ;
	}
	public static final class SerialMultiHashNodeFactory<K, V> implements HashMapNodeFactory<K, V, SerialMultiHashMapNode<K, V>> {
		@Override
		public final SerialMultiHashMapNode<K, V> node(final int hash, final K key, final V value) {
			return new SerialMultiHashMapNode<K, V>(hash, key, value) ;
		}
	}
	
	public static void main(String[] args) {
		MultiMap<Integer, Integer> map = new SerialInlineMultiHashMap<Integer, Integer>() ;
		for (int i = 0 ; i != 100 ; i++) {
			map.put(i, i) ;
			map.put(i, i+1) ;
			map.put(i, i) ;
		}
		for (Integer i : map.values()) {
			System.out.println(i) ;
		}
	}

	public static final class KeyEquality<K, V> extends InlineMultiHashMap.KeyEquality<K, V, SerialMultiHashMapNode<K, V>> {
		public KeyEquality(Equality<? super K> keyEq) {
			super(keyEq) ;
		}
		@Override
		public boolean prefixMatch(K cmp, SerialMultiHashMapNode<K, V> n) {
			return keyEq.equates(cmp, n.key) ;
		}
	}

	public static final class EntryEquality<K, V> extends InlineMultiHashMap.EntryEquality<K, V, SerialMultiHashMapNode<K, V>> {
		private static final long serialVersionUID = -8668943955126687051L ;

		public EntryEquality(Equality<? super K> keyEq, Equality<? super V> valEq) {
			super(keyEq, valEq) ;
		}
		@Override
		public boolean equates(SerialMultiHashMapNode<K, V> a, SerialMultiHashMapNode<K, V> b) {
			return keyEq.equates(a.key, b.key) && valEq.equates(a.value, b.value) ;
		}

		@Override
		public boolean prefixMatch(Entry<K, V> cmp, SerialMultiHashMapNode<K, V> n) {
			return keyEq.equates(cmp.getKey(), n.key) ;
		}

		@Override
		public boolean suffixMatch(Entry<K, V> cmp, SerialMultiHashMapNode<K, V> n) {
			return valEq.equates(cmp.getValue(), n.value) ;
		}
	}
	
}
