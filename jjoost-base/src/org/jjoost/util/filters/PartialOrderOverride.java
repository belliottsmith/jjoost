package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;
import org.jjoost.util.Function ;

/**
 * Constructs a <code>FilterPartialOrder</code> which delegates to the provided filter, but ignores the comparator provided to it, always
 * using the one provided here instead. That is, this forces the comparator used by the provided filter to always be the one provided
 * here. It also transforms the type of the FilterPartialOrder by accepting a method from a new type to the type accepted by the provided
 * filter, thereby providing a filter over the domain type of the function. The function should be stable with regards to the comparator,
 * i.e. <code>a < b <==> f.f(a) < f.f(b)</code>
 */
public class PartialOrderOverride<S, T> implements FilterPartialOrder<T> {

	private static final long serialVersionUID = 454908176068653901L ;
	final FilterPartialOrder<S> underlying ;
	final Comparator<? super S> cmp ;
	final Function<? super T, ? extends S> f ;

    /**
	 * Constructs a <code>FilterPartialOrder</code> which delegates to the provided filter, but ignores the comparator provided to it, always
	 * using the one provided here instead. That is, this forces the comparator used by the provided filter to always be the one provided
	 * here. It also transforms the type of the FilterPartialOrder by accepting a method from a new type to the type accepted by the provided
	 * filter, thereby providing a filter over the domain type of the function. The function should be stable with regards to the comparator,
	 * i.e. <code>a < b <==> f.f(a) < f.f(b)</code>
	 * 
	 * @param underlying
	 *            the partial order filter to wrap
	 * @param cmp
	 *            the comparator to pass to its accepts() methods, overriding the one provided by utilising classes
	 * @param f
	 *            a function whose range is the input type of the provided filter, and whose domain will be the input type of the filter returned
	 */
	public PartialOrderOverride(FilterPartialOrder<S> underlying, Comparator<? super S> cmp, Function<? super T, ? extends S> f) {
		this.underlying = underlying ;
		this.cmp = cmp ;
		this.f = f ;
	}

	@Override
	public boolean mayAcceptBetween(T lb, boolean lbInclusive, T ub, boolean ubInclusive, Comparator<? super T> _) {
		return underlying.mayAcceptBetween(f.apply(lb), lbInclusive, f.apply(ub), ubInclusive, cmp) ;
	}

	@Override
	public boolean mayRejectBetween(T lb, boolean lbInclusive, T ub, boolean ubInclusive, Comparator<? super T> _) {
		return underlying.mayRejectBetween(f.apply(lb), lbInclusive, f.apply(ub), ubInclusive, cmp) ;
	}
	
	@Override
	public boolean accept(T test, Comparator<? super T> _) {
		return underlying.accept(f.apply(test), cmp) ;
	}

    /**
	 * Returns a <code>FilterPartialOrder</code> which delegates to the provided filter, but ignores the comparator provided to it, always
	 * using the one provided here instead. That is, this forces the comparator used by the provided filter to always be the one provided
	 * here. It also transforms the type of the FilterPartialOrder by accepting a method from a new type to the type accepted by the provided
	 * filter, thereby providing a filter over the domain type of the function. The function should be stable with regards to the comparator,
	 * i.e. <code>a < b <==> f.f(a) < f.f(b)</code>
	 * 
	 * @param filter
	 *            the partial order filter to wrap
	 * @param cmp
	 *            the comparator to pass to its accepts() methods, overriding the one provided by utilising classes
	 * @param f
	 *            a function whose range is the input type of the provided filter, and whose domain will be the input type of the filter returned
	 * @return a <code>FilterPartialOrder</code> which always uses the provided comparator
	 */
	public static <S, T> PartialOrderOverride<S, T> get(FilterPartialOrder<S> filter, Comparator<? super S> cmp, Function<? super T, ? extends S> f) {
		return new PartialOrderOverride<S, T>(filter, cmp, f) ;
	}

}
