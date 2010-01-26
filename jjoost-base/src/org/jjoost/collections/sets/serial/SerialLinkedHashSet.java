package org.jjoost.collections.sets.serial;

import org.jjoost.collections.base.SerialHashStore ;
import org.jjoost.collections.base.SerialLinkedHashStore ;
import org.jjoost.collections.sets.base.AbstractHashSet ;
import org.jjoost.collections.sets.base.HashSet ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class SerialLinkedHashSet<V> extends HashSet<V, AbstractHashSet.SerialLinkedHashSetNode<V>>{

	private static final long serialVersionUID = 1051610520557989640L;

	public SerialLinkedHashSet() {
		this(16, 0.75f) ;
	}
	public SerialLinkedHashSet(int minimumInitialCapacity, float loadFactor) {
		this(minimumInitialCapacity, loadFactor, SerialHashStore.defaultRehasher(), Equalities.object()) ;
	}
	
	public SerialLinkedHashSet(Equality<? super V> keyEquality) {
		this(SerialHashStore.defaultRehasher(), keyEquality) ;
	}
	
	public SerialLinkedHashSet(Rehasher rehasher, Equality<? super V> keyEquality) { 
		this(16, 0.75f, rehasher, keyEquality) ;
	}
	
	public SerialLinkedHashSet( 
			int minimumInitialCapacity, float loadFactor, 
			Rehasher rehasher, Equality<? super V> keyEquality) 
	{
		super(rehasher, keyEquality, 
			AbstractHashSet.<V>serialLinkedNodeFactory(), 
			new SerialLinkedHashStore<SerialLinkedHashSetNode<V>>(minimumInitialCapacity, loadFactor)) ;
	}

}
