package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;
import org.jjoost.util.Iters ;

/**
 * A filter representing the conjunction (i.e. "and") of the supplied filters.
 * The filters are evaluated in the order they are provided (left-to-right) and are evaluated if and only if no previous filters passed
 * 
 * @author b.elliottsmith
 */
public class FilterMultiOr<E> implements Filter<E> {

	private static final long serialVersionUID = 6311808530912921895L ;
	private final Filter<? super E>[] filters ;

    /**
     * Constructs a new filter representing the conjunction (i.e. "and") of the supplied filters
     * 
     * @param filters filters to apply
     */
	public FilterMultiOr(Filter<? super E>... filters) {
		this.filters = filters ;
	}

    /**
     * Constructs a new filter representing the conjunction (i.e. "and") of the supplied filters
     * 
     * @param filters filters to apply
     */
	@SuppressWarnings("unchecked")
	public FilterMultiOr(Iterable<? extends Filter<? super E>> filters) {
		this.filters = Iters.toArray(filters, Filter.class) ;
	}

	public boolean accept(E test) {
		boolean r = false ;
		for (int i = 0 ; !r & i != filters.length ; i++)
			r = filters[i].accept(test) ;
		return r ;
	}

	public String toString() {
		return "any hold: " + filters.toString() ;
	}

    /**
     * Returns the conjunction (i.e. "and") of the supplied filters
     * 
     * @param filters filters to apply
     * @return conjunction (i.e. "and") of provided filters
     */
	public static <E> FilterMultiOr<E> get(Filter<? super E>... filters) {
		return new FilterMultiOr<E>(filters) ;
	}

    /**
     * Returns the conjunction (i.e. "and") of the supplied filters
     * 
     * @param filters filters to apply
     * @return conjunction (i.e. "and") of provided filters
     */
	public static <E> FilterMultiOr<E> get(Iterable<? extends Filter<? super E>> filters) {
		return new FilterMultiOr<E>(filters) ;
	}

}
