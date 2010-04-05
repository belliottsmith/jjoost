package org.jjoost.collections.maps.concurrent;

import org.jjoost.collections.base.HashLockHashStore;
import org.jjoost.collections.base.LockFreeHashStore ;
import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.AbstractConcurrentHashStore.Counting;
import org.jjoost.collections.maps.base.InlineMultiHashMap ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class HashLockInlineMultiHashMap<K, V> extends InlineMultiHashMap<K, V, InlineMultiNode<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public HashLockInlineMultiHashMap() {
		this(16, 0.75f) ;
	}	
	public HashLockInlineMultiHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	public HashLockInlineMultiHashMap(Equality<? super K> keyEquality) {
		this(LockFreeHashStore.defaultRehasher(), keyEquality) ;
	}	
	public HashLockInlineMultiHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object()) ;
	}	
	public HashLockInlineMultiHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality) ;
	}
	
	public HashLockInlineMultiHashMap( 
			int minimumInitialCapacity, float loadFactor,
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(rehasher, new InlineMultiNode.KeyEquality<K, V>(keyEquality), new InlineMultiNode.NodeEquality<K, V>(keyEquality, valEquality),
			InlineMultiNode.<K, V>factory(), 
			new HashLockHashStore<InlineMultiNode<K, V>>(minimumInitialCapacity, loadFactor, Counting.PRECISE, Counting.PRECISE)) ;
	}

}
