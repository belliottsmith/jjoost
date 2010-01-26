package org.jjoost.collections.sets.concurrent;

import org.jjoost.collections.base.LockFreeHashStore;
import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.base.LockFreeHashStore.Counting;
import org.jjoost.collections.sets.base.AbstractHashSet;
import org.jjoost.collections.sets.base.InlineMultiHashSet;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class LockFreeInlineMultiHashSet<V> extends InlineMultiHashSet<V, AbstractHashSet.LockFreeHashSetNode<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public LockFreeInlineMultiHashSet() {
		this(16, 0.75f) ;
	}
	public LockFreeInlineMultiHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object()) ;
	}
	
	public LockFreeInlineMultiHashSet(Equality<? super V> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality) ;
	}
	
	public LockFreeInlineMultiHashSet(Rehasher rehasher, Equality<? super V> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality) ;
	}
	
	public LockFreeInlineMultiHashSet( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(rehasher, keyEquality, 
			AbstractHashSet.<V>lockFreeNodeFactory(), 
			new LockFreeHashStore<LockFreeHashSetNode<V>>(minimumInitialCapacity, loadFactor, Counting.PRECISE, Counting.PRECISE)) ;
	}

}
