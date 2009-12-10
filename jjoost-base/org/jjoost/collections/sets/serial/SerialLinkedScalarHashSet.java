package org.jjoost.collections.sets.serial;

import org.jjoost.collections.base.SerialHashTable ;
import org.jjoost.collections.base.SerialLinkedHashTable ;
import org.jjoost.collections.sets.base.AbstractHashSet ;
import org.jjoost.collections.sets.base.ScalarHashSet ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class SerialLinkedScalarHashSet<V> extends ScalarHashSet<V, AbstractHashSet.SerialLinkedHashSetNode<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialLinkedScalarHashSet() {
		this(16, 0.75f) ;
	}
	public SerialLinkedScalarHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashTable.defaultRehasher(), Equalities.object()) ;
	}
	
	public SerialLinkedScalarHashSet( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super V> keyHasher, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(keyHasher, rehasher, keyEquality, 
			AbstractHashSet.<V>serialLinkedNodeFactory(), 
			new SerialLinkedHashTable<SerialLinkedHashSetNode<V>>(minimumInitialCapacity, loadFactor)) ;
	}

}
