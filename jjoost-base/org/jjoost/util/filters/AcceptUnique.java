package org.jjoost.util.filters ;

import org.jjoost.collections.ScalarSet ;
import org.jjoost.collections.sets.serial.SerialScalarHashSet ;
import org.jjoost.util.Equalities ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Filter ;
import org.jjoost.util.Hashers ;
import org.jjoost.util.Rehashers ;

/**
 * Keeps only the first in a sequence of duplicates
 */
public class AcceptUnique<V> implements Filter<V> {

	private static final long serialVersionUID = 4135610622081116945L ;
	private final ScalarSet<V> seen ;

	public AcceptUnique() {
		this(new SerialScalarHashSet<V>(8, 0.75f, Hashers.object(), Rehashers.compose(Rehashers.jdkHashmapRehasher(), Rehashers.flipEveryHalfByte()), Equalities.object())) ;
	}
	public AcceptUnique(ScalarSet<V> set) {
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
		return get(new SerialScalarHashSet<V>(Hashers.object(), eq)) ;
	}
	
	public static <V> AcceptUnique<V> get(ScalarSet<V> set) {
		return new AcceptUnique<V>(set) ;
	}
	
}
