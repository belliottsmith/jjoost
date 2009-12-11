package org.jjoost.collections.sets.serial;

import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.SerialLinkedHashStore ;
import org.jjoost.collections.sets.base.AbstractHashSet ;
import org.jjoost.collections.sets.base.InlineListHashSet ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Hashers;
import org.jjoost.util.Rehasher;

public class SerialLinkedInlineListHashSet<V> extends InlineListHashSet<V, AbstractHashSet.SerialLinkedHashSetNode<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialLinkedInlineListHashSet() {
		this(16, 0.75f) ;
	}
	public SerialLinkedInlineListHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, Hashers.object(), SerialHashStore.defaultRehasher(), Equalities.object()) ;
	}
	
	public SerialLinkedInlineListHashSet( 
			int minimumInitialCapacity, float loadFactor, Hasher<? super V> keyHasher, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(keyHasher, rehasher, keyEquality, 
			AbstractHashSet.<V>serialLinkedNodeFactory(), 
			new SerialLinkedHashStore<SerialLinkedHashSetNode<V>>(minimumInitialCapacity, loadFactor)) ;
	}

}
