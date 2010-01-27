package org.jjoost.collections.sets.base;

import org.jjoost.collections.AnySet ;

public abstract class AbstractSet<V> implements AnySet<V> {
	
	private static final long serialVersionUID = -2269362435477906614L;

	@SuppressWarnings("unchecked")
	public boolean equals(Object that) {
		return this == that || (that instanceof AnySet && equals((AnySet<V>) that)) ;
	}
	
	public boolean equals(AnySet<V> that) {
		if (that.totalCount() != this.totalCount())
			return false ;
		if (that.permitsDuplicates() != this.permitsDuplicates())
			return false ;
		// retain some type safety of equals(Object) by confirming equalities are "equal" before comparing sets
		if (!that.equality().equals(this.equality()))
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
