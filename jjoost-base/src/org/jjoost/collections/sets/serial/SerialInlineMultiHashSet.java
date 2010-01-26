package org.jjoost.collections.sets.serial;

import org.jjoost.collections.base.SerialHashStore;
import org.jjoost.collections.sets.base.AbstractHashSet;
import org.jjoost.collections.sets.base.InlineMultiHashSet;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SerialInlineMultiHashSet<V> extends InlineMultiHashSet<V, AbstractHashSet.SerialHashSetNode<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialInlineMultiHashSet() {
		this(16, 0.75f) ;
	}
	public SerialInlineMultiHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object()) ;
	}
	
	public SerialInlineMultiHashSet(Equality<? super V> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality) ;
	}
	
	public SerialInlineMultiHashSet(Rehasher rehasher, Equality<? super V> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality) ;
	}
	
	public SerialInlineMultiHashSet( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(rehasher, keyEquality, 
			AbstractHashSet.<V>serialNodeFactory(), 
			new SerialHashStore<SerialHashSetNode<V>>(minimumInitialCapacity, loadFactor)) ;
	}

}
