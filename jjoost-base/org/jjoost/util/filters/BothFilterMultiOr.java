package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.Filter ;
import org.jjoost.util.FilterPartialOrder ;
import org.jjoost.util.Iters ;

/**
 * A filter representing the disjunction (i.e. "or") of the supplied filters implementing both <code>Filter</code> and <code>FilterPartialOrder</code>.
 * The filters are evaluated in the order they are provided (left-to-right) and are evaluated if and only if no previous filters passed.
 * 
 * @author b.elliottsmith
 */
public class BothFilterMultiOr<E, F extends Filter<? super E> & FilterPartialOrder<E>> implements BothFilter<E> {

	private static final long serialVersionUID = 7419162471960836459L ;
	private final F[] filters ;

	/**
	 * Construct a new filter representing the disjunction (i.e. "or") of the supplied filters
	 * 
	 * @param filters filters to apply
	 */
	public BothFilterMultiOr(F... filters) {
		this.filters = filters ;
	}

	/**
	 * Construct a new filter representing the disjunction (i.e. "or") of the supplied filters
	 * 
	 * @param filters filters to apply
	 */
	@SuppressWarnings("unchecked")
	public BothFilterMultiOr(Iterable<? extends F> filters) {
		this.filters = (F[]) Iters.toArray(filters, Filter.class) ;
	}

	public boolean accept(E test) {
		boolean r = false ;
		for (int i = 0 ; !r & i != filters.length ; i++)
			r = filters[i].accept(test) ;
		return r ;
	}

	public boolean accept(E test, Comparator<? super E> cmp) {
		boolean r = false ;
		for (int i = 0 ; !r & i != filters.length ; i++)
			r = filters[i].accept(test, cmp) ;
		return r ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		boolean r = false ;
		for (int i = 0 ; !r & i != filters.length ; i++)
			r = filters[i].mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
		return r ;
	}
	

	@Override
	public boolean mayRejectBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		boolean result = true ;
		for (int i = 0 ; result & i != filters.length ; i++)
			result = filters[i].mayRejectBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
		return result ;
	}
	
	public String toString() {
		return "any hold: " + filters ;
	}

    /**
	 * Returns the disjunction (i.e. "or") of the supplied filters
     * 
     * @param filters filters to apply
     * @return disjunction (i.e. "or") of provided filters
     */
	public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilterMultiOr<E, F> get(F ... filters) {
		return new BothFilterMultiOr<E, F>(filters) ;
	}

    /**
	 * Returns the disjunction (i.e. "or") of the supplied filters
     * 
     * @param filters filters to apply
     * @return disjunction (i.e. "or") of provided filters
     */
	public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilterMultiOr<E, F> get(Iterable<? extends F> filters) {
		return new BothFilterMultiOr<E, F>(filters) ;
	}

}
