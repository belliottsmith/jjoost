package org.jjoost.collections.maps.serial;

import java.util.Map.Entry ;

import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.SerialHashStore.SerialHashNode ;
import org.jjoost.collections.maps.base.HashMapNodeFactory ;
import org.jjoost.collections.maps.base.InlineListHashMap ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class SerialInlineListHashMap<K, V> extends InlineListHashMap<K, V, SerialInlineListHashMap.SerialListHashMapNode<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialInlineListHashMap() {
		this(16, 0.75f) ;
	}
	public SerialInlineListHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	public SerialInlineListHashMap(
			int minimumInitialCapacity, float loadFactor, Hasher<? super K> keyHasher, 
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) {
		super(keyHasher, rehasher, new KeyEquality<K, V>(keyEquality), new EntryEquality<K, V>(keyEquality, valEquality),
			SerialInlineListHashMap.<K, V>serialNodeFactory(), 
			new SerialHashStore<SerialListHashMapNode<K, V>>(minimumInitialCapacity, loadFactor)) ;
	}

	
	public static final class SerialListHashMapNode<K, V> extends SerialHashNode<SerialListHashMapNode<K, V>> implements Entry<K, V> {
		private static final long serialVersionUID = -5766263745864028747L;
		public SerialListHashMapNode(int hash, K key, V value) {
			super(hash);
			this.key = key;
			this.value = value;
		}
		private final K key ;
		private V value ;		
		@Override public K getKey() { return key ; }
		@Override public V getValue() { return value ; }
		@Override public V setValue(V value) { final V r = this.value ; this.value = value ; return r ; }
		@Override public SerialListHashMapNode<K, V> copy() { return new SerialListHashMapNode<K, V>(hash, key, value) ; }
	}
	
	@SuppressWarnings("unchecked")
	private static final SerialListHashNodeFactory LIST_SCALAR_HASH_NODE_FACTORY = new SerialListHashNodeFactory() ;
	@SuppressWarnings("unchecked")
	public static <K, V> SerialListHashNodeFactory<K, V> serialNodeFactory() {
		return LIST_SCALAR_HASH_NODE_FACTORY ;
	}
	public static final class SerialListHashNodeFactory<K, V> implements HashMapNodeFactory<K, V, SerialListHashMapNode<K, V>> {
		@Override
		public final SerialListHashMapNode<K, V> node(final int hash, final K key, final V value) {
			return new SerialListHashMapNode<K, V>(hash, key, value) ;
		}
	}
	
	public static final class KeyEquality<K, V> extends InlineListHashMap.KeyEquality<K, V, SerialListHashMapNode<K, V>> {
		public KeyEquality(Equality<? super K> keyEq) {
			super(keyEq) ;
		}
		@Override
		public boolean prefixMatch(K cmp, SerialListHashMapNode<K, V> n) {
			return keyEq.equates(cmp, n.key) ;
		}
	}

	public static final class EntryEquality<K, V> extends InlineListHashMap.EntryEquality<K, V, SerialListHashMapNode<K, V>> {
		private static final long serialVersionUID = -8668943955126687051L ;

		public EntryEquality(Equality<? super K> keyEq, Equality<? super V> valEq) {
			super(keyEq, valEq) ;
		}
		@Override
		public boolean equates(SerialListHashMapNode<K, V> a, SerialListHashMapNode<K, V> b) {
			return keyEq.equates(a.key, b.key) && valEq.equates(a.value, b.value) ;
		}

		@Override
		public boolean prefixMatch(Entry<K, V> cmp, SerialListHashMapNode<K, V> n) {
			return keyEq.equates(cmp.getKey(), n.key) ;
		}

		@Override
		public boolean suffixMatch(Entry<K, V> cmp, SerialListHashMapNode<K, V> n) {
			return valEq.equates(cmp.getValue(), n.value) ;
		}
	}

}
