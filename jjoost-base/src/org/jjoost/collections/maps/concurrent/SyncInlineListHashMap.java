package org.jjoost.collections.maps.concurrent;

import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.SynchronizedHashStore;
import org.jjoost.collections.maps.base.InlineListHashMap ;
import org.jjoost.collections.maps.serial.SerialInlineListHashMap;
import org.jjoost.collections.maps.serial.SerialInlineListHashMap.Node;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SyncInlineListHashMap<K, V> extends InlineListHashMap<K, V, SerialInlineListHashMap.Node<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SyncInlineListHashMap() {
		this(16, 0.75f) ;
	}
	public SyncInlineListHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	public SyncInlineListHashMap(Equality<? super K> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality) ;
	}
	public SyncInlineListHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object()) ;
	}
	public SyncInlineListHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality) ;
	}

	public SyncInlineListHashMap(
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) {
		super(rehasher, new SerialInlineListHashMap.KeyEquality<K, V>(keyEquality), new SerialInlineListHashMap.EntryEquality<K, V>(keyEquality, valEquality),
			SerialInlineListHashMap.<K, V>factory(), 
			new SynchronizedHashStore<Node<K, V>>(new SerialHashStore<Node<K, V>>(minimumInitialCapacity, loadFactor))) ;
	}

}
