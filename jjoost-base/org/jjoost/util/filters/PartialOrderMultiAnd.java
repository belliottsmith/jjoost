package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;
import org.jjoost.util.Iters ;

public class PartialOrderMultiAnd<P> implements FilterPartialOrder<P> {

	private static final long serialVersionUID = 454908176068653901L ;
	protected final FilterPartialOrder<P>[] operands ;

	public PartialOrderMultiAnd(FilterPartialOrder<P>... operands) {
		this.operands = operands ;
	}

	@SuppressWarnings("unchecked")
	public PartialOrderMultiAnd(Iterable<? extends FilterPartialOrder<P>> operands) {
		this.operands = Iters.toArray(operands, FilterPartialOrder.class) ;
	}

	@Override
	public boolean mayAcceptBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) {
		boolean result = true ;
		for (int i = 0 ; result & i != operands.length ; i++)
			result = operands[i].mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
		return result ;
	}

	@Override
	public boolean accept(P test, Comparator<? super P> cmp) {
		boolean result = true ;
		for (int i = 0 ; result & i != operands.length ; i++)
			result = operands[i].accept(test, cmp) ;
		return result ;
	}

	public static <P> PartialOrderMultiAnd<P> get(FilterPartialOrder<P>... operands) {
		return new PartialOrderMultiAnd<P>(operands) ;
	}

	public static <P> PartialOrderMultiAnd<P> get(Iterable<? extends FilterPartialOrder<P>> operands) {
		return new PartialOrderMultiAnd<P>(operands) ;
	}

}