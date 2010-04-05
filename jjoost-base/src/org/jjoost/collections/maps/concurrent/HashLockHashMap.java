package org.jjoost.collections.maps.concurrent;

import org.jjoost.collections.base.HashLockHashStore;
import org.jjoost.collections.base.LockFreeHashStore ;
import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.AbstractConcurrentHashStore.Counting;
import org.jjoost.collections.maps.base.HashMap ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class HashLockHashMap<K, V> extends HashMap<K, V, ScalarNode<K, V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public HashLockHashMap() {
		this(16, 0.75f) ;
	}
	public HashLockHashMap(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object(), Equalities.object()) ;
	}
	public HashLockHashMap(Equality<? super K> keyEquality) {
		this(LockFreeHashStore.defaultRehasher(), keyEquality) ;
	}	
	public HashLockHashMap(Rehasher rehasher, Equality<? super K> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality, Equalities.object()) ;
	}	
	public HashLockHashMap(Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) { 
		this(16, 0.75f, rehasher, keyEquality, valEquality) ;
	}

	public HashLockHashMap( 
			int minimumInitialCapacity, float loadFactor,
			Rehasher rehasher, Equality<? super K> keyEquality, Equality<? super V> valEquality) 
	{
		super(rehasher, new ScalarNode.KeyEquality<K, V>(keyEquality), new ScalarNode.NodeEquality<K, V>(keyEquality, valEquality),
				ScalarNode.<K, V>factory(), 
			new HashLockHashStore<ScalarNode<K, V>>(minimumInitialCapacity, loadFactor, Counting.PRECISE, Counting.OFF)) ;
	}
	
}
