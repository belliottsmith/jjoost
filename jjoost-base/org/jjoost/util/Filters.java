package org.jjoost.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator ;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.jjoost.util.filters.* ;
import org.jjoost.collections.ArbitrarySet ;
import org.jjoost.collections.ScalarSet ;
import org.jjoost.collections.iters.ClosableIterator ;
import org.jjoost.collections.iters.FilteredClosableIterator ;
import org.jjoost.collections.iters.FilteredIterable ;
import org.jjoost.collections.iters.FilteredIterator ;

public class Filters {

	public static <E> AcceptAll<E> acceptAll() {
    	return AcceptAll.get();
    }
    
	public static <E> AcceptNone<E> acceptNone() {
		return AcceptNone.get();
	}
	
	/**
	 * Returns the negation of the supplied filter
	 * 
	 * @param <E>
	 * @param filter
	 * @return
	 */
    public static <E> Filter<E> not(Filter<E> filter) {
        return FilterNot.get(filter) ;
    }

    /**
     * Returns the negation of the supplied filter
     * 
     * @param <E>
     * @param filter
     * @return
     */
    public static <E> FilterPartialOrder<E> not(FilterPartialOrder<E> filter) {
        return PartialOrderNot.get(filter) ;
    }

    /**
     * Returns the negation of the supplied filter
     * 
     * @param <E>
     * @param filter
     * @return
     */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> not(F filter) {
    	return BothFilterNot.get(filter) ;
    }
    
    /**
     * Returns the conjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E> Filter<E> and(Filter<? super E> a, Filter<? super E> b) {
    	return FilterAnd.get(a, b) ;
    }
    
    /**
     * Returns the conjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E> Filter<E> and(Filter<? super E> ... filters) {
        return FilterMultiAnd.get(filters) ;
    }
    
    /**
     * Returns the conjunction of the supplied filters; filters are evaluated in ascending order, and are evaluated iff previous filters passed
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E> Filter<E> and(Iterable<Filter<? super E>> filters) {
        return FilterMultiAnd.get(filters) ;
    }

    /**
     * Returns the conjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed 
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E> FilterPartialOrder<E> and(FilterPartialOrder<E> a, FilterPartialOrder<E> b) {
        return PartialOrderAnd.get(a, b) ;
    }
    
    /**
     * Returns the conjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed 
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E> FilterPartialOrder<E> and(FilterPartialOrder<E> ... filters) {
    	return PartialOrderMultiAnd.get(filters) ;
    }
    
    /**
     * Returns the conjunction of the supplied filters; filters are evaluated in ascending order, and are evaluated iff previous filters passed
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E> FilterPartialOrder<E> and(Iterable<? extends FilterPartialOrder<E>> filters) {
        return PartialOrderMultiAnd.get(filters) ;
    }

    /**
     * Returns the conjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> and(F a, F b) {
    	return BothFilterAnd.get(a, b) ;
    }
    
    /**
     * Returns the conjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> and(F ... filters) {
        return BothFilterMultiAnd.get(filters) ;
    }
    
    /**
     * Returns the conjunction of the supplied filters; filters are evaluated in ascending order, and are evaluated iff previous filters passed
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> and(Iterable<? extends F> filters) {
        return BothFilterMultiAnd.get(filters) ;
    }

    /**
     * Returns the disjunction of the supplied filters
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E> Filter<E> or(Filter<? super E> a, Filter<? super E> b) {
    	return FilterOr.get(a, b) ;
    }
    
    /**
     * Returns the disjunction of the supplied filters
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E> Filter<E> or(Filter<? super E> ... filters) {
        return FilterMultiOr.get(filters) ;
    }
    
    /**
     * Returns the disjunction of the supplied filters
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E> Filter<E> or(Iterable<? extends Filter<? super E>> filters) {
        return FilterMultiOr.get(filters) ;
    }

    /**
     * Returns the disjunction of the supplied filters
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E> FilterPartialOrder<E> or(FilterPartialOrder<E> a, FilterPartialOrder<E> b) {
    	return PartialOrderOr.get(a, b) ;
    }
    
    /**
     * Returns the disjunction of the supplied filters
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E> FilterPartialOrder<E> or(FilterPartialOrder<E> ... filters) {
        return PartialOrderMultiOr.get(filters) ;
    }
    
    /**
     * Returns the disjunction of the supplied filters
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E> FilterPartialOrder<E> or(Iterable<? extends FilterPartialOrder<E>> filters) {
        return PartialOrderMultiOr.get(filters) ;
    }


    /**
     * Returns the disjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> or(F a, F b) {
    	return BothFilterOr.get(a, b) ;
    }
    
    /**
     * Returns the disjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> or(F ... filters) {
        return BothFilterMultiOr.get(filters) ;
    }
    
    /**
     * Returns the disjunction of the supplied filters; filters are evaluated in ascending order, and are evaluated iff previous filters passed
     * 
     * @param <E>
     * @param filters
     * @return
     */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> or(Iterable<? extends F> filters) {
        return BothFilterMultiOr.get(filters) ;
    }

    /**
     * Returns a filter accepting everything strictly less than the supplied Comparable object
     * 
     * @param <E>
     * @param val
     * @return
     */
    public static <E> BothFilter<E> isEqualTo(E val) {
    	return AcceptEqual.get(val) ;
    }
    
    /**
     * Returns a filter accepting everything strictly less than the supplied Comparable object
     * 
     * @param <E>
     * @param val
     * @return
     */
    public static <E> FilterPartialOrder<E> isLess(E val) {
        return PartialOrderAcceptLess.get(val) ;
    }

    /**
     * Accepts everything that is strictly less than the provided object
     * 
     * Returns an object implementing both Filter and FilterPartialOrder; the Filter accept() method delegates to the object's compareTo() method, whereas
     * the FilterPartialOrder methods utilise the provided comparators
     * 
     * @param <E>
     * @param val
     * @return
     */
    public static <E extends Comparable<? super E>> BothFilter<E> isLess(E val) {
    	return AcceptLess.get(val) ;
    }
    
    /**
     * Returns a filter accepting everything strictly less than the supplied Comparable object
     * 
     * @param <E>
     * @param val
     * @return
     */
    public static <E> FilterPartialOrder<E> isLessEq(E val) {
    	return PartialOrderAcceptLessEqual.get(val) ;
    }
    
    /**
     * Accepts everything that is strictly less than the provided object
     * 
     * Returns an object implementing both Filter and FilterPartialOrder; the Filter accept() method delegates to the object's compareTo() method, whereas
     * the FilterPartialOrder methods utilise the provided comparators
     * 
     * @param <E>
     * @param val
     * @return
     */
    public static <E extends Comparable<? super E>> BothFilter<E> isLessEq(E val) {
    	return AcceptLessEqual.get(val) ;
    }
    
    /**
     * Returns a filter accepting everything strictly less than the supplied Comparable object
     * 
     * @param <E>
     * @param val
     * @return
     */
    public static <E> FilterPartialOrder<E> isGreater(E val) {
    	return PartialOrderAcceptGreater.get(val) ;
    }
    
    /**
     * Accepts everything that is strictly less than the provided object
     * 
     * Returns an object implementing both Filter and FilterPartialOrder; the Filter accept() method delegates to the object's compareTo() method, whereas
     * the FilterPartialOrder methods utilise the provided comparators
     * 
     * @param <E>
     * @param val
     * @return
     */
    public static <E extends Comparable<? super E>> BothFilter<E> isGreater(E val) {
    	return AcceptGreater.get(val) ;
    }
    
    /**
     * Returns a filter accepting everything strictly less than the supplied Comparable object
     * 
     * @param <E>
     * @param val
     * @return
     */
    public static <E> FilterPartialOrder<E> isGreaterEq(E val) {
    	return PartialOrderAcceptGreaterEqual.get(val) ;
    }
    
    /**
     * Accepts everything that is strictly less than the provided object
     * 
     * Returns an object implementing both Filter and FilterPartialOrder; the Filter accept() method delegates to the object's compareTo() method, whereas
     * the FilterPartialOrder methods utilise the provided comparators
     * 
     * @param <E>
     * @param val
     * @return
     */
    public static <E extends Comparable<? super E>> BothFilter<E> isGreaterEq(E val) {
    	return AcceptGreaterEqual.get(val) ;
    }
    
    public static <E> FilterPartialOrder<E> isBetween(E lb, E ub) {
    	return PartialOrderAcceptBetween.get(lb, ub) ;
    }
    
    public static <E extends Comparable<? super E>> BothFilter<E> isBetween(E lb, E ub) {
    	return AcceptBetween.get(lb, ub) ;
    }
    
    public static <E> FilterPartialOrder<E> forceComparator(FilterPartialOrder<E> filter, Comparator<? super E> cmp) {
    	return forceComparator(filter, cmp, Functions.<E>identity()) ;
    }
    
    public static <S, T> FilterPartialOrder<T> forceComparator(FilterPartialOrder<S> filter, Comparator<? super S> cmp, Function<? super T, ? extends S> f) {
    	return PartialOrderOverride.get(filter, cmp, f) ;
    }
    
    public static final <E> Filter<E> unique() {
    	return AcceptUnique.<E>get() ;
    }
    
    public static final <E> Filter<E> unique(Equality<? super E> eq) {
    	return AcceptUnique.<E>get() ;
    }
    
    public static final <E> Filter<E> unique(ScalarSet<E> set) {
    	return AcceptUnique.<E>get(set) ;
    }
    
    public static final <E> FilterPartialOrder<E> uniqueAsc() {
		return AcceptUniqueAscendingSequence.<E>get() ;
	}
	
	public static final <E> FilterPartialOrder<E> uniqueDesc() {
		return AcceptUniqueDescendingSequence.<E>get() ;
	}
	
    /**
     * Returns a filter accepting everything that is not null; the class parameter is used for type safety only
     * 
     * @param <E>
     * @param clazz
     * @return
     */    
	public static <E> Filter<E> notNull(Class<E> clazz) {
    	return new AcceptIfNotNull<E>() ;
    }

    /**
     * Returns a filter accepting only values that are null; the class parameter is used for type safety only
     * 
     * @param <E>
     * @param clazz
     * @return
     */
	public static <E> Filter<E> isNull(Class<E> clazz) {
    	return new AcceptIfNull<E>() ;
    }

    /**
     * Returns a filter that applies a function to its input before delegating to another filter
     * 
     * @param <INPUT, FILTER>
     * @param mapping the function to transform the input variables
     * @param filter the underlying filter
     * @return
     */
    public static <X, Y> MappedFilter<X, Y> mapped(Function<X, Y> mapping, Filter<Y> filter) {
        return new MappedFilter<X, Y>(mapping, filter) ;
    }
    
    /**
     * Returns a filter accepting strings that match the supplied pattern
     * 
     * @param pattern the pattern to filter by
     * @return
     */
    public static Filter<String> matches(Pattern pattern) {
    	return new FilterPattern(pattern) ;
    }
    
    /**
     * Returns a filter accepting strings that match the supplied pattern
     * 
     * @param pattern the pattern to filter by
     * @return
     */
    public static Filter<String> matches(String pattern) {
    	return matches(Pattern.compile(pattern)) ;
    }

    public static <E> Filter<E> isMemberOf(ArbitrarySet<E> set) {
    	return new AcceptIfMember<E>(set) ;
    }

    public static <E> Filter<E> isMemberOf(Iterable<E> set) {
    	return new AcceptIfMember<E>(set) ;
    }

	/**
	 * Filter the supplied collection through the supplied filter, returning the result as a list
	 * 
	 * @param <E>
	 * @param list
	 * @param filter
	 * @return
	 */
    public static <E> List<E> apply(Collection<E> list, Filter<? super E> filter) {
        return apply(filter, list) ;
    }

	/**
	 * lazily filter the supplied iterable through the supplied filter
	 * 
	 * @param <E>
	 * @param list
	 * @param filter
	 * @return
	 */
    public static <E> FilteredIterable<E> apply(Iterable<E> iter, Filter<? super E> filter) {
        return apply(filter, iter) ;
    }
    
    /**
     * lazily filter the supplied iterator through the supplied filter
     * 
     * @param <E>
     * @param iter
     * @param filter
     * @return
     */
    public static <E> FilteredIterator<E> apply(Iterator<E> iter, Filter<? super E> filter) {
        return apply(filter, iter) ;
    }
    
    /**
     * lazily filter the supplied closable iterator through the supplied filter
     * 
     * @param <E>
     * @param iter
     * @param filter
     * @return
     */
    public static <E> FilteredClosableIterator<E> apply(ClosableIterator<E> iter, Filter<? super E> filter) {
        return apply(filter, iter) ;
    }

    /**
     * Filter the supplied collection through the supplied filter, returning the result as a list
     * 
     * @param <E>
     * @param list
     * @param filter
     * @return
     */
    public static <E> List<E> apply(Filter<? super E> filter, Collection<E> list) {
    	List<E> ret = new ArrayList<E>(list.size()) ;
    	for (E e : list) if (filter.accept(e)) ret.add(e) ;
    	return ret ;
    }
    
    /**
     * lazily filter the supplied iterable through the supplied filter
     * 
     * @param <E>
     * @param list
     * @param filter
     * @return
     */
    public static <E> FilteredIterable<E> apply(Filter<? super E> filter, Iterable<E> iter) {
    	return new FilteredIterable<E>(iter, filter) ;
    }
    
    /**
     * lazily filter the supplied iterator through the supplied filter
     * 
     * @param <E>
     * @param iter
     * @param filter
     * @return
     */
    public static <E> FilteredIterator<E> apply(Filter<? super E> filter, Iterator<E> iter) {
    	return new FilteredIterator<E>(iter, filter) ;
    }
    
    /**
     * lazily filter the supplied closable iterator through the supplied filter
     * 
     * @param <E>
     * @param iter
     * @param filter
     * @return
     */
    public static <E> FilteredClosableIterator<E> apply(Filter<? super E> filter, ClosableIterator<E> iter) {
    	return new FilteredClosableIterator<E>(iter, filter) ;
    }
    
}
