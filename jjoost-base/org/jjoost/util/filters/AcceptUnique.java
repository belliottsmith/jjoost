package org.jjoost.util.filters ;

import org.jjoost.collections.Set ;
import org.jjoost.collections.sets.serial.SerialHashSet ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Filter ;

/**
 * Keeps only the first in a sequence of duplicates
 */
public class AcceptUnique<V> implements Filter<V> {

	private static final long serialVersionUID = 4135610622081116945L ;
	private final Set<V> seen ;

	public AcceptUnique() {
		this(new SerialHashSet<V>(8, 0.75f)) ;
	}
	public AcceptUnique(Set<V> set) {
		this.seen = set ;
	}

	public boolean accept(V next) {
		final boolean r ;
		if (next != null) {
			r = seen.put(next) == null ;
		} else {
			r = seen.contains(null) ;
			if (!r)
				seen.put(null) ;
		}
		return r ;
	}
	
	public String toString() {
		return "is first occurence of" ;
	}

	public static <V> AcceptUnique<V> get() {
		return new AcceptUnique<V>() ;
	}
	
	public static <V> AcceptUnique<V> get(Equality<? super V> eq) {
		return get(new SerialHashSet<V>(eq)) ;
	}
	
	public static <V> AcceptUnique<V> get(Set<V> set) {
		return new AcceptUnique<V>(set) ;
	}
	
}
