package org.jjoost.collections.sets.concurrent;

import org.jjoost.collections.base.LockFreeLinkedHashStore;
import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.base.LockFreeHashStore.Counting;
import org.jjoost.collections.sets.base.AbstractHashSet;
import org.jjoost.collections.sets.base.ScalarHashSet;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class LockFreeLinkedScalarHashSet<V> extends ScalarHashSet<V, AbstractHashSet.LockFreeLinkedHashSetNode<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public LockFreeLinkedScalarHashSet() {
		this(16, 0.75f) ;
	}
	public LockFreeLinkedScalarHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object()) ;
	}
	
	public LockFreeLinkedScalarHashSet( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(rehasher, keyEquality, 
			AbstractHashSet.<V>lockFreeLinkedNodeFactory(), 
			new LockFreeLinkedHashStore<LockFreeLinkedHashSetNode<V>>(minimumInitialCapacity, loadFactor, Counting.PRECISE, Counting.OFF)) ;
	}

}
