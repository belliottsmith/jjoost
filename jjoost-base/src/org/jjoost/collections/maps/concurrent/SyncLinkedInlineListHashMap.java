package org.jjoost.collections.maps.concurrent;

import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.base.SerialLinkedHashStore;
import org.jjoost.collections.base.SynchronizedHashStore;
import org.jjoost.collections.maps.base.InlineListHashMap;
import org.jjoost.collections.maps.serial.SerialLinkedInlineListHashMap;
import org.jjoost.collections.maps.serial.SerialLinkedInlineListHashMap.Node;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SyncLinkedInlineListHashMap<K, V> extends InlineListHashMap<K, V, SerialLinkedInlineListHashMap.Node<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SyncLinkedInlineListHashMap() {
		this(16, 0.75f) ;
	}
	public SyncLinkedInlineListHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	public SyncLinkedInlineListHashMap(Equality<? super K> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality) ;
	}	
	public SyncLinkedInlineListHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object()) ;
	}	
	public SyncLinkedInlineListHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality) ;
	}
	
	public SyncLinkedInlineListHashMap( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(rehasher, new SerialLinkedInlineListHashMap.KeyEquality<K, V>(keyEquality), new SerialLinkedInlineListHashMap.EntryEquality<K, V>(keyEquality, valEquality),
			SerialLinkedInlineListHashMap.<K, V>factory(), 
			new SynchronizedHashStore<Node<K, V>>(new SerialLinkedHashStore<Node<K, V>>(minimumInitialCapacity, loadFactor))) ;
	}

}
