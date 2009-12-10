package org.jjoost.collections.sets.serial;

import org.jjoost.collections.base.SerialHashTable ;
import org.jjoost.collections.sets.base.AbstractHashSet ;
import org.jjoost.collections.sets.base.ScalarHashSet ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class SerialScalarHashSet<V> extends ScalarHashSet<V, AbstractHashSet.SerialHashSetNode<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialScalarHashSet() {
		this(16, 0.75f) ;
	}
	public SerialScalarHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashTable.defaultRehasher(), Equalities.object()) ;
	}
	
	public SerialScalarHashSet(Hasher<? super V> keyHasher, Equality<? super V> keyEquality) {
		this(keyHasher, SerialHashTable.defaultRehasher(), keyEquality) ;
	}
	
	public SerialScalarHashSet(Hasher<? super V> keyHasher, Rehasher rehasher, Equality<? super V> keyEquality) { 
		this(16, 0.75f, keyHasher, rehasher, keyEquality) ;
	}
	
	public SerialScalarHashSet( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super V> keyHasher, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(keyHasher, rehasher, keyEquality, 
			AbstractHashSet.<V>serialNodeFactory(), 
			new SerialHashTable<SerialHashSetNode<V>>(minimumInitialCapacity, loadFactor)) ;
	}

}
