package org.jjoost.collections.sets.concurrent;

import org.jjoost.collections.base.LockFreeHashStore ;
import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.LockFreeHashStore.Counting ;
import org.jjoost.collections.sets.base.AbstractHashSet ;
import org.jjoost.collections.sets.base.ScalarHashSet ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class LockFreeScalarHashSet<V> extends ScalarHashSet<V, AbstractHashSet.LockFreeHashSetNode<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public LockFreeScalarHashSet() {
		this(16, 0.75f) ;
	}
	public LockFreeScalarHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashStore.defaultRehasher(), Equalities.object()) ;
	}
	
	public LockFreeScalarHashSet(Hasher<? super V> keyHasher, Equality<? super V> keyEquality) {
		this(keyHasher, SerialHashStore.defaultRehasher(), keyEquality) ;
	}
	
	public LockFreeScalarHashSet(Hasher<? super V> keyHasher, Rehasher rehasher, Equality<? super V> keyEquality) { 
		this(16, 0.75f, keyHasher, rehasher, keyEquality) ;
	}
	
	public LockFreeScalarHashSet( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super V> keyHasher, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(keyHasher, rehasher, keyEquality, 
			AbstractHashSet.<V>lockFreeNodeFactory(), 
			new LockFreeHashStore<LockFreeHashSetNode<V>>(minimumInitialCapacity, loadFactor, Counting.PRECISE, Counting.OFF)) ;
	}

}
