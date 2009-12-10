package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;
import org.jjoost.util.Function ;

public class PartialOrderOverride<S, T> implements FilterPartialOrder<T> {

	private static final long serialVersionUID = 454908176068653901L ;
	protected final FilterPartialOrder<S> underlying ;
	protected final Comparator<? super S> cmp ;
	protected final Function<? super T, ? extends S> f ;

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
	public boolean accept(T test, Comparator<? super T> _) {
		return underlying.accept(f.apply(test), cmp) ;
	}

	public static <S, T> PartialOrderOverride<S, T> get(FilterPartialOrder<S> filter, Comparator<? super S> cmp, Function<? super T, ? extends S> f) {
		return new PartialOrderOverride<S, T>(filter, cmp, f) ;
	}

}
