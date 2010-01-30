package org.jjoost.collections.maps.concurrent;

import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.base.SerialLinkedHashStore;
import org.jjoost.collections.base.SynchronizedHashStore;
import org.jjoost.collections.maps.base.InlineMultiHashMap;
import org.jjoost.collections.maps.serial.SerialLinkedInlineMultiHashMap;
import org.jjoost.collections.maps.serial.SerialLinkedInlineMultiHashMap.Node;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SyncLinkedInlineMultiHashMap<K, V> extends InlineMultiHashMap<K, V, SerialLinkedInlineMultiHashMap.Node<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SyncLinkedInlineMultiHashMap() {
		this(16, 0.75f) ;
	}	
	public SyncLinkedInlineMultiHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	public SyncLinkedInlineMultiHashMap(Equality<? super K> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality) ;
	}	
	public SyncLinkedInlineMultiHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object()) ;
	}	
	public SyncLinkedInlineMultiHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality) ;
	}
	
	public SyncLinkedInlineMultiHashMap( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(rehasher, new SerialLinkedInlineMultiHashMap.KeyEquality<K, V>(keyEquality), new SerialLinkedInlineMultiHashMap.EntryEquality<K, V>(keyEquality, valEquality),
			SerialLinkedInlineMultiHashMap.<K, V>factory(), 
			new SynchronizedHashStore<Node<K, V>>(new SerialLinkedHashStore<Node<K, V>>(minimumInitialCapacity, loadFactor))) ;
	}

}
