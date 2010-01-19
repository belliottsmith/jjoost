package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;
import org.jjoost.util.Iters ;

/**
 * A filter representing the disjunction (i.e. "or") of the supplied partial order filters.
 * The filters are evaluated in the order they are provided (left-to-right) and are evaluated if and only if all previous filters passed
 * 
 * @author b.elliottsmith
 */
public class PartialOrderMultiOr<P> implements FilterPartialOrder<P> {

	private static final long serialVersionUID = 454908176068653901L ;

	/**
	 * The filters from which at least one must all hold for the result to be true 
	 */
	protected final FilterPartialOrder<P>[] filters ;

	/**
	 * Constructs a new filter representing the disjunction (i.e. "or") of the supplied partial order filters
	 * 
	 * @param filters filters to apply
	 */
	public PartialOrderMultiOr(FilterPartialOrder<P>... filters) {
		this.filters = filters ;
	}

	/**
	 * Constructs a new filter representing the disjunction (i.e. "or") of the supplied partial order filters
	 * 
	 * @param filters filters to apply
	 */
	@SuppressWarnings("unchecked")
	public PartialOrderMultiOr(Iterable<? extends FilterPartialOrder<P>> filters) {
		this.filters = Iters.toArray(filters, FilterPartialOrder.class) ;
	}

	@Override
	public boolean mayAcceptBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) {
		boolean result = false ;
		for (int i = 0 ; !result & i != filters.length ; i++)
			result = filters[i].mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
		return result ;
	}

	@Override
	public boolean mayRejectBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) {
		boolean result = true ;
		for (int i = 0 ; result & i != filters.length ; i++)
			result = filters[i].mayRejectBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
		return result ;
	}
	
	@Override
	public boolean accept(P test, Comparator<? super P> cmp) {
		boolean result = false ;
		for (int i = 0 ; !result & i != filters.length ; i++)
			result = filters[i].accept(test, cmp) ;
		return result ;
	}

    /**
     * Returns the disjunction (i.e. "or") of the supplied partial order filters
     * 
     * @param filters filters to apply
     * @return disjunction (i.e. "or") of provided filters
     */
	public static <E> PartialOrderMultiOr<E> get(FilterPartialOrder<E>... filters) {
		return new PartialOrderMultiOr<E>(filters) ;
	}

    /**
     * Returns the disjunction (i.e. "or") of the supplied partial order filters
     * 
     * @param filters filters to apply
     * @return disjunction (i.e. "or") of provided filters
     */
	public static <E> PartialOrderMultiOr<E> get(Iterable<? extends FilterPartialOrder<E>> filters) {
		return new PartialOrderMultiOr<E>(filters) ;
	}

}
