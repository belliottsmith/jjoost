package org.jjoost.collections.sets.serial;

import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.sets.base.AbstractHashSet ;
import org.jjoost.collections.sets.base.InlineMultiHashSet ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class SerialInlineMultiHashSet<V> extends InlineMultiHashSet<V, AbstractHashSet.SerialHashSetNode<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialInlineMultiHashSet() {
		this(16, 0.75f) ;
	}
	public SerialInlineMultiHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashStore.defaultRehasher(), Equalities.object()) ;
	}
	
	public SerialInlineMultiHashSet( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super V> keyHasher, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(keyHasher, rehasher, keyEquality, 
			AbstractHashSet.<V>serialNodeFactory(), 
			new SerialHashStore<SerialHashSetNode<V>>(minimumInitialCapacity, loadFactor)) ;
	}

}
