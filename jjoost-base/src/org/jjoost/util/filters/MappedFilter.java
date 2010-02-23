package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;
import org.jjoost.util.Function ;

/**
 * A filter that applies the provided function to its input before delegating to the provided filter
 * 
 * @author b.elliottsmith
 */
public class MappedFilter<X, Y> implements Filter<X> {

	private static final long serialVersionUID = -8782803136948476218L ;

	private final Filter<? super Y> filter ;
	private final Function<? super X, ? extends Y> mapped ;

    /**
     * Constructs a filter that applies the provided function to its input before delegating to the provided filter
     * 
     * @param mapping the function to transform the input variables
     * @param filter the delegate filter
     */
	public MappedFilter(Function<? super X, ? extends Y> mapping, Filter<? super Y> filter) {
		this.filter = filter ;
		this.mapped = mapping ;
	}

	public boolean accept(X test) {
		return filter.accept(mapped.apply(test)) ;
	}

    /**
     * Returns a filter that applies the provided function to its input before delegating to the provided filter
     * 
     * @param mapping the function to transform the input variables
     * @param filter the delegate filter
     * @return a filter that applies the provided function to its input before delegating to the provided filter
     */
	public static <X, Y> MappedFilter<X, Y> get(Function<? super X, ? extends Y> mapping, Filter<? super Y> filter) {
		return new MappedFilter<X, Y>(mapping, filter) ;
	}

}
