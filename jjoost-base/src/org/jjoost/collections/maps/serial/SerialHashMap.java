package org.jjoost.collections.maps.serial;

import java.util.Map.Entry;

import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.base.SerialHashStore.SerialHashNode;
import org.jjoost.collections.maps.base.HashMapNodeFactory;
import org.jjoost.collections.maps.base.HashMap;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SerialHashMap<K, V> extends HashMap<K, V, SerialHashMap.SerialScalarHashMapNode<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialHashMap() {
		this(16, 0.75f) ;
	}
	public SerialHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}	
	public SerialHashMap(Equality<? super K> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality) ;
	}	
	public SerialHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object()) ;
	}	
	public SerialHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality) ;
	}
	
	public SerialHashMap( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(rehasher, new KeyEquality<K, V>(keyEquality), new EntryEquality<K, V>(keyEquality, valEquality),
			SerialHashMap.<K, V>serialNodeFactory(), 
			new SerialHashStore<SerialScalarHashMapNode<K, V>>(minimumInitialCapacity, loadFactor)) ;
	}
	
	protected static final class SerialScalarHashMapNode<K, V> extends SerialHashNode<SerialScalarHashMapNode<K, V>> implements Entry<K, V> {
		private static final long serialVersionUID = -5766263745864028747L;
		public SerialScalarHashMapNode(int hash, K key, V value) {
			super(hash);
			this.key = key;
			this.value = value;
		}
		protected final K key ;
		protected V value ;		
		@Override public final K getKey() { return key ; }
		@Override public final V getValue() { return value ; }
		@Override public final V setValue(V value) { final V r = this.value ; this.value = value ; return r ; }
		@Override public final SerialScalarHashMapNode<K, V> copy() { return new SerialScalarHashMapNode<K, V>(hash, key, value) ; }
	}
	
	@SuppressWarnings("unchecked")
	private static final SerialScalarHashNodeFactory SERIAL_SCALAR_HASH_NODE_FACTORY = new SerialScalarHashNodeFactory() ;
	@SuppressWarnings("unchecked")
	public static <K, V> SerialScalarHashNodeFactory<K, V> serialNodeFactory() {
		return SERIAL_SCALAR_HASH_NODE_FACTORY ;
	}
	protected static final class SerialScalarHashNodeFactory<K, V> implements HashMapNodeFactory<K, V, SerialScalarHashMapNode<K, V>> {
		@Override
		public final SerialScalarHashMapNode<K, V> makeNode(final int hash, final K key, final V value) {
			return new SerialScalarHashMapNode<K, V>(hash, key, value) ;
		}
	}

	protected static final class KeyEquality<K, V> extends HashMap.KeyEquality<K, V, SerialScalarHashMapNode<K, V>> {
		public KeyEquality(Equality<? super K> keyEq) {
			super(keyEq) ;
		}
		@Override
		public boolean prefixMatch(K cmp, SerialScalarHashMapNode<K, V> n) {
			return keyEq.equates(cmp, n.key) ;
		}
	}

	protected static final class EntryEquality<K, V> extends HashMap.NodeEquality<K, V, SerialScalarHashMapNode<K, V>> {
		private static final long serialVersionUID = -8668943955126687051L ;

		public EntryEquality(Equality<? super K> keyEq, Equality<? super V> valEq) {
			super(keyEq, valEq) ;
		}
		@Override
		public boolean prefixMatch(Entry<K, V> cmp, SerialScalarHashMapNode<K, V> n) {
			return keyEq.equates(cmp.getKey(), n.key) ;
		}
		@Override
		public boolean suffixMatch(Entry<K, V> cmp, SerialScalarHashMapNode<K, V> n) {
			return valEq.equates(cmp.getValue(), n.value) ;
		}
	}
	
}
