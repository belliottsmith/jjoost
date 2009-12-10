package org.jjoost.collections.sets.serial;

import org.jjoost.collections.base.SerialHashTable ;
import org.jjoost.collections.sets.base.AbstractHashSet ;
import org.jjoost.collections.sets.base.InlineListHashSet ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class SerialInlineListHashSet<V> extends InlineListHashSet<V, AbstractHashSet.SerialHashSetNode<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialInlineListHashSet() {
		this(16, 0.75f) ;
	}
	public SerialInlineListHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashTable.defaultRehasher(), Equalities.object()) ;
	}
	
	public SerialInlineListHashSet( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super V> keyHasher, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(keyHasher, rehasher, keyEquality, 
			AbstractHashSet.<V>serialNodeFactory(), 
			new SerialHashTable<SerialHashSetNode<V>>(minimumInitialCapacity, loadFactor)) ;
	}

}
