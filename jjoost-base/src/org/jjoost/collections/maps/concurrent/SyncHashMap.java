package org.jjoost.collections.maps.concurrent;

import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.base.SynchronizedHashStore;
import org.jjoost.collections.maps.base.HashMap;
import org.jjoost.collections.maps.serial.SerialHashMap;
import org.jjoost.collections.maps.serial.SerialHashMap.Node;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SyncHashMap<K, V> extends HashMap<K, V, SerialHashMap.Node<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SyncHashMap() {
		this(16, 0.75f) ;
	}
	public SyncHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}	
	public SyncHashMap(Equality<? super K> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality) ;
	}	
	public SyncHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object()) ;
	}	
	public SyncHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality) ;
	}
	
	public SyncHashMap( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(rehasher, new SerialHashMap.KeyEquality<K, V>(keyEquality), new SerialHashMap.EntryEquality<K, V>(keyEquality, valEquality),
			SerialHashMap.<K, V>serialNodeFactory(), 
			new SynchronizedHashStore<Node<K, V>>(new SerialHashStore<Node<K, V>>(minimumInitialCapacity, loadFactor))) ;
	}
	
}
