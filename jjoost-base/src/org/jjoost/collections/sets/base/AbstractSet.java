package org.jjoost.collections.sets.base;

import org.jjoost.collections.AnySet ;

public abstract class AbstractSet<V> implements AnySet<V> {
	
	public boolean equals(Object that) {
		return this == that || (that instanceof AnySet && equals((AnySet<V>) that)) ;
	}
	
	public boolean equals(AnySet<V> that) {
		if (that.totalCount() != this.totalCount())
			return false ;
		if (that.permitsDuplicates() != this.permitsDuplicates())
			return false ;
		if (permitsDuplicates()) {
			for (V v : that) {
				if (this.count(v) != that.count(v))
					return false ;
			}
		} else {
			for (V v : that)
				if (!contains(v))
					return false ;
		}
		return true ;
	}

}
