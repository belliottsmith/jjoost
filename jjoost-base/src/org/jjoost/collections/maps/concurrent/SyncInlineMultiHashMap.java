package org.jjoost.collections.maps.concurrent;

import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.base.SynchronizedHashStore;
import org.jjoost.collections.maps.base.InlineMultiHashMap;
import org.jjoost.collections.maps.serial.SerialInlineMultiHashMap;
import org.jjoost.collections.maps.serial.SerialInlineMultiHashMap.Node;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SyncInlineMultiHashMap<K, V> extends InlineMultiHashMap<K, V, SerialInlineMultiHashMap.Node<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SyncInlineMultiHashMap() {
		this(16, 0.75f) ;
	}	
	public SyncInlineMultiHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	public SyncInlineMultiHashMap(Equality<? super K> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality) ;
	}	
	public SyncInlineMultiHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object()) ;
	}	
	public SyncInlineMultiHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality) ;
	}

	public SyncInlineMultiHashMap( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(rehasher, new SerialInlineMultiHashMap.KeyEquality<K, V>(keyEquality), new SerialInlineMultiHashMap.EntryEquality<K, V>(keyEquality, valEquality),
			SerialInlineMultiHashMap.<K, V>factory(), 
			new SynchronizedHashStore<Node<K, V>>(new SerialHashStore<Node<K, V>>(minimumInitialCapacity, loadFactor))) ;
	}

}
