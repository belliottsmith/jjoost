package org.jjoost.collections.maps.concurrent;

import org.jjoost.collections.base.HashLockHashStore;
import org.jjoost.collections.base.LockFreeHashStore ;
import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.AbstractConcurrentHashStore.Counting;
import org.jjoost.collections.maps.base.InlineListHashMap ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class HashLockInlineListHashMap<K, V> extends InlineListHashMap<K, V, InlineListNode<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public HashLockInlineListHashMap() {
		this(16, 0.75f) ;
	}
	public HashLockInlineListHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	public HashLockInlineListHashMap(Equality<? super K> keyEquality) {
		this(LockFreeHashStore.defaultRehasher(), keyEquality) ;
	}	
	public HashLockInlineListHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object()) ;
	}	
	public HashLockInlineListHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality) ;
	}

	public HashLockInlineListHashMap(
			int minimumInitialCapacity, float loadFactor,
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) {
		super(rehasher, new InlineListNode.KeyEquality<K, V>(keyEquality), new InlineListNode.NodeEquality<K, V>(keyEquality, valEquality),
			InlineListNode.<K, V>factory(), 
			new HashLockHashStore<InlineListNode<K, V>>(minimumInitialCapacity, loadFactor, Counting.PRECISE, Counting.PRECISE)) ;
	}
	
}
