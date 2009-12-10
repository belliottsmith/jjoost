package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;
import org.jjoost.util.Function ;

public class MappedFilter<X, Y> implements Filter<X> {

	private static final long serialVersionUID = -8782803136948476218L ;

	private final Filter<Y> filter ;
	private final Function<X, Y> mapped ;

	public MappedFilter(Function<X, Y> mapping, Filter<Y> filter) {
		this.filter = filter ;
		this.mapped = mapping ;
	}

	public boolean accept(X test) {
		return filter.accept(mapped.apply(test)) ;
	}

	public static <X, Y> MappedFilter<X, Y> get(Function<X, Y> mapping, Filter<Y> filter) {
		return new MappedFilter<X, Y>(mapping, filter) ;
	}

}
