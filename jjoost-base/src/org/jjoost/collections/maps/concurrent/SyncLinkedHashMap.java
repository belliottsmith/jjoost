package org.jjoost.collections.maps.concurrent;

import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.base.SerialLinkedHashStore;
import org.jjoost.collections.base.SynchronizedHashStore;
import org.jjoost.collections.maps.base.HashMap;
import org.jjoost.collections.maps.serial.SerialLinkedHashMap;
import org.jjoost.collections.maps.serial.SerialLinkedHashMap.Node;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SyncLinkedHashMap<K, V> extends HashMap<K, V, SerialLinkedHashMap.Node<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SyncLinkedHashMap() {
		this(16, 0.75f) ;
	}	
	public SyncLinkedHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}	
	public SyncLinkedHashMap(Equality<? super K> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality) ;
	}	
	public SyncLinkedHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object()) ;
	}	
	public SyncLinkedHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality) ;
	}
	
	public SyncLinkedHashMap( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(rehasher, new SerialLinkedHashMap.KeyEquality<K, V>(keyEquality), new SerialLinkedHashMap.EntryEquality<K, V>(keyEquality, valEquality),
			SerialLinkedHashMap.<K, V>factory(), 
			new SynchronizedHashStore<Node<K, V>>(new SerialLinkedHashStore<Node<K, V>>(minimumInitialCapacity, loadFactor))) ;
	}

}
