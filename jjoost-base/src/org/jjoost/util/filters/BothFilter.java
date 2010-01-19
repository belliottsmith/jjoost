package org.jjoost.util.filters;

import org.jjoost.util.Filter ;
import org.jjoost.util.FilterPartialOrder ;

/**
 * A convenience interface for classes implementing both <code>Filter</code> and <code>FilterPartialOrder</code>
 * 
 * @author b.elliottsmith
 */
public interface BothFilter<E> extends Filter<E>, FilterPartialOrder<E> {

}
