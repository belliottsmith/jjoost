package org.jjoost.collections.sets.concurrent;

import org.jjoost.collections.base.LockFreeLinkedHashStore ;
import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.LockFreeHashStore.Counting ;
import org.jjoost.collections.sets.base.AbstractHashSet ;
import org.jjoost.collections.sets.base.InlineMultiHashSet ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class LockFreeLinkedInlineMultiHashSet<V> extends InlineMultiHashSet<V, AbstractHashSet.LockFreeLinkedHashSetNode<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public LockFreeLinkedInlineMultiHashSet() {
		this(16, 0.75f) ;
	}
	public LockFreeLinkedInlineMultiHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashStore.defaultRehasher(), Equalities.object()) ;
	}
	
	public LockFreeLinkedInlineMultiHashSet( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super V> keyHasher, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(keyHasher, rehasher, keyEquality, 
			AbstractHashSet.<V>lockFreeLinkedNodeFactory(), 
			new LockFreeLinkedHashStore<LockFreeLinkedHashSetNode<V>>(minimumInitialCapacity, loadFactor, Counting.PRECISE, Counting.PRECISE)) ;
	}

}
