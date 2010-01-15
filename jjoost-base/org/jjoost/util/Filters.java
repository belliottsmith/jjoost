package org.jjoost.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator ;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.jjoost.util.filters.* ;
import org.jjoost.collections.AnySet ;
import org.jjoost.collections.Set ;
import org.jjoost.collections.iters.ClosableIterator ;
import org.jjoost.collections.iters.FilteredClosableIterator ;
import org.jjoost.collections.iters.FilteredIterable ;
import org.jjoost.collections.iters.FilteredIterator ;

/**
 * A class providing methods acting on filters, and default filter implementations 
 * 
 * @author b.elliottsmith
 *
 */
public class Filters {

	/**
	 * Return a filter that accepts everything (i.e. returns true for all input)
	 * @return a filter that accepts everything (i.e. returns true for all input)
	 */
	public static <E> AcceptAll<E> acceptAll() {
    	return AcceptAll.get();
    }
    
	/**
	 * Return a filter that accepts nothing (i.e. returns false for all input)
	 * @return a filter that accepts nothing (i.e. returns false for all input)
	 */
	public static <E> AcceptNone<E> acceptNone() {
		return AcceptNone.get();
	}
	
	/**
	 * Returns the negation of the supplied filter
	 * 
	 * @param filter filter to negate
	 * @return negation of the supplied filter
	 */
    public static <E> Filter<E> not(Filter<E> filter) {
        return FilterNot.get(filter) ;
    }

    /**
	 * Returns the negation of the supplied partial order filter
	 * 
	 * @param filter filter to negate
	 * @return negation of the supplied partial order filter
     */
    public static <E> FilterPartialOrder<E> not(FilterPartialOrder<E> filter) {
        return PartialOrderNot.get(filter) ;
    }

    /**
	 * Returns the negation of the supplied filter implementing both <code>Filter</code> and <code>PartialOrder</code>
	 * 
	 * @param filter
	 *            filter to negate
	 * @return negation of the supplied filter implementing both <code>Filter</code> and <code>PartialOrder</code>
	 */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> not(F filter) {
    	return BothFilterNot.get(filter) ;
    }
    
    /**
     * Returns the conjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param a filter to apply first
     * @param b filter to apply second
     * @return conjunction of a and b
     */
    public static <E> Filter<E> and(Filter<? super E> a, Filter<? super E> b) {
    	return FilterAnd.get(a, b) ;
    }
    
    /**
     * Returns the conjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param filters filters to apply
     * @return conjunction of provided filters
     */
    public static <E> Filter<E> and(Filter<? super E> ... filters) {
        return FilterMultiAnd.get(filters) ;
    }
    
    /**
     * Returns the conjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param filters filters to apply
     * @return conjunction of provided filters
     */
    public static <E> Filter<E> and(Iterable<Filter<? super E>> filters) {
        return FilterMultiAnd.get(filters) ;
    }

    /**
     * Returns the conjunction of the supplied partial order filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param a filter to apply first
     * @param b filter to apply second
     * @return conjunction of a and b
     */
    public static <E> FilterPartialOrder<E> and(FilterPartialOrder<E> a, FilterPartialOrder<E> b) {
        return PartialOrderAnd.get(a, b) ;
    }
    
    /**
     * Returns the conjunction of the supplied partial order filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param filters filters to apply
     * @return conjunction of provided filters
     */
    public static <E> FilterPartialOrder<E> and(FilterPartialOrder<E> ... filters) {
    	return PartialOrderMultiAnd.get(filters) ;
    }
    
    /**
     * Returns the conjunction of the supplied partial order filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param filters filters to apply
     * @return conjunction of provided filters
     */
    public static <E> FilterPartialOrder<E> and(Iterable<? extends FilterPartialOrder<E>> filters) {
        return PartialOrderMultiAnd.get(filters) ;
    }

    /**
	 * Returns the conjunction of the supplied filters implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; filters
	 * are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
	 * 
	 * @param a
	 *            filter to apply first
	 * @param b
	 *            filter to apply second
	 * @return conjunction of a and b
	 */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> and(F a, F b) {
    	return BothFilterAnd.get(a, b) ;
    }
    
    /**
	 * Returns the conjunction of the supplied filters implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; filters
	 * are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param filters filters to apply
     * @return conjunction of provided filters
     */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> and(F ... filters) {
        return BothFilterMultiAnd.get(filters) ;
    }
    
    /**
	 * Returns the conjunction of the supplied filters implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; filters
	 * are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param filters filters to apply
     * @return conjunction of provided filters
     */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> and(Iterable<? extends F> filters) {
        return BothFilterMultiAnd.get(filters) ;
    }

    /**
     * Returns the disjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param a filter to apply first
     * @param b filter to apply second
     * @return disjunction of a and b
     */
    public static <E> Filter<E> or(Filter<? super E> a, Filter<? super E> b) {
    	return FilterOr.get(a, b) ;
    }
    
    /**
     * Returns the disjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param filters filters to apply
     * @return disjunction of provided filters
     */
    public static <E> Filter<E> or(Filter<? super E> ... filters) {
        return FilterMultiOr.get(filters) ;
    }
    
    /**
     * Returns the disjunction of the supplied filters; filters are evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
     * 
     * @param filters filters to apply
     * @return disjunction of provided filters
     */
    public static <E> Filter<E> or(Iterable<? extends Filter<? super E>> filters) {
        return FilterMultiOr.get(filters) ;
    }

    /**
	 * Returns the disjunction of the supplied partial order filters; filters are evaluated in the order they are provided (left-to-right)
	 * and are evaluated iff previous filters passed
	 * 
	 * @param a
	 *            filter to apply first
	 * @param b
	 *            filter to apply second
	 * @return disjunction of a and b
     */
    public static <E> FilterPartialOrder<E> or(FilterPartialOrder<E> a, FilterPartialOrder<E> b) {
    	return PartialOrderOr.get(a, b) ;
    }
    
    /**
	 * Returns the disjunction of the supplied partial order filters; filters are evaluated in the order they are provided (left-to-right)
	 * and are evaluated iff previous filters passed
	 * 
     * @param filters filters to apply
     * @return disjunction of provided filters
	 */
    public static <E> FilterPartialOrder<E> or(FilterPartialOrder<E> ... filters) {
        return PartialOrderMultiOr.get(filters) ;
    }
    
    /**
	 * Returns the disjunction of the supplied partial order filters; filters are evaluated in the order they are provided (left-to-right)
	 * and are evaluated iff previous filters passed
	 * 
     * @param filters filters to apply
     * @return disjunction of provided filters
     */
    public static <E> FilterPartialOrder<E> or(Iterable<? extends FilterPartialOrder<E>> filters) {
        return PartialOrderMultiOr.get(filters) ;
    }


    /**
	 * Returns the disjunction of the supplied filters that implement <code>Filter</code> and <code>FilterPartialOrder</code>; filters are
	 * evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
	 * 
	 * @param a
	 *            filter to apply first
	 * @param b
	 *            filter to apply second
	 * @return disjunction of a and b
	 */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> or(F a, F b) {
    	return BothFilterOr.get(a, b) ;
    }
    
    /**
	 * Returns the disjunction of the supplied filters that implement <code>Filter</code> and <code>FilterPartialOrder</code>; filters are
	 * evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
	 * 
     * @param filters filters to apply
     * @return disjunction of provided filters
     */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> or(F ... filters) {
        return BothFilterMultiOr.get(filters) ;
    }
    
    /**
	 * Returns the disjunction of the supplied filters that implement <code>Filter</code> and <code>FilterPartialOrder</code>; filters are
	 * evaluated in the order they are provided (left-to-right) and are evaluated iff previous filters passed
	 * 
     * @param filters filters to apply
     * @return disjunction of provided filters
     */
    public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilter<E> or(Iterable<? extends F> filters) {
        return BothFilterMultiOr.get(filters) ;
    }

    /**
	 * Returns a filter accepting only values equal to the one provided, using default object equality. The filter implements both
	 * <code>Filter</code> and <code>FilterPartialOrder</code>.
	 * 
	 * @param val
	 *            value to accept
	 * @return a filter accepting only values equal to the one provided, using default object equality.
	 */
    public static <E> BothFilter<E> isEqualTo(E val) {
    	return AcceptEqual.get(val) ;
    }
    
    /**
     * Returns a filter accepting only values equal to the one provided, using the provided equality. The filter implements both
	 * <code>Filter</code> and <code>FilterPartialOrder</code>.
     * 
     * @param val value to accept
     * @return a filter accepting only values equal to the one provided, using the provided equality
     */
    public static <E> BothFilter<E> isEqualTo(E val, Equality<? super E> equality) {
    	return AcceptEqual.get(val, equality) ;
    }
    
    /**
	 * Returns a partial order filter that accepts everything less than the provided value as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes
	 * 
	 * @param val
	 *            exclusive upper limit of acceptable values
	 * @return a partial order filter that accepts everything less than the provided value
	 */
    public static <E> FilterPartialOrder<E> isLess(E val) {
        return PartialOrderAcceptLess.get(val) ;
    }

    /**
	 * Returns a partial order filter that accepts everything less than the provided value as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes.
	 * <p>
	 * Returns an object implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
	 * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
	 * methods utilise the provided comparators
	 * 
	 * @param val
	 *            exclusive upper limit of acceptable values
	 * @return a filter that accepts everything less than the provided value
	 */
    public static <E extends Comparable<? super E>> BothFilter<E> isLess(E val) {
    	return AcceptLess.get(val) ;
    }
    
    /**
	 * Returns a partial order filter that accepts everything less than or equal to the provided value, as determined by the
	 * <code>Comparator</code> provided to its methods by utilising classes
	 * 
	 * @param val
	 *            inclusive upper limit of acceptable values
	 * @return a partial order filter that accepts everything less than or equal to the provided value
	 */
    public static <E> FilterPartialOrder<E> isLessEq(E val) {
    	return PartialOrderAcceptLessEqual.get(val) ;
    }
    
    /**
	 * Returns a partial order filter that accepts everything less than or equal to the provided value as determined by the
	 * <code>Comparator</code> provided to its methods by utilising classes.
	 * <p>
	 * Returns an object implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
	 * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
	 * methods utilise the provided comparators
	 * 
	 * @param val
	 *            exclusive upper limit of acceptable values
	 * @return a filter that accepts everything less than or equal to the provided value
	 */
    public static <E extends Comparable<? super E>> BothFilter<E> isLessEq(E val) {
    	return AcceptLessEqual.get(val) ;
    }
    
    /**
	 * Returns a partial order filter that accepts everything greater than the provided value as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes
	 * 
	 * @param val
	 *            exclusive upper limit of acceptable values
	 * @return a partial order filter that accepts everything greater than the provided value
	 */
    public static <E> FilterPartialOrder<E> isGreater(E val) {
    	return PartialOrderAcceptGreater.get(val) ;
    }
    
    /**
	 * Returns a partial order filter that accepts everything greater than the provided value as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes.
	 * <p>
	 * Returns an object implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
	 * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
	 * methods utilise the provided comparators
	 * 
	 * @param val
	 *            exclusive upper limit of acceptable values
	 * @return a filter that accepts everything greater than the provided value
	 */
    public static <E extends Comparable<? super E>> BothFilter<E> isGreater(E val) {
    	return AcceptGreater.get(val) ;
    }
    
    /**
	 * Returns a partial order filter that accepts everything greater than or equal to the provided value, as determined by the
	 * <code>Comparator</code> provided to its methods by utilising classes
	 * 
	 * @param val
	 *            inclusive upper limit of acceptable values
	 * @return a partial order filter that accepts everything greater than or equal to the provided value
	 */
    public static <E> FilterPartialOrder<E> isGreaterEq(E val) {
    	return PartialOrderAcceptGreaterEqual.get(val) ;
    }
    
    /**
	 * Returns a partial order filter that accepts everything greater than or equal to the provided value as determined by the
	 * <code>Comparator</code> provided to its methods by utilising classes.
	 * <p>
	 * Returns an object implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
	 * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
	 * methods utilise the provided comparators
	 * 
	 * @param val
	 *            exclusive upper limit of acceptable values
	 * @return a filter that accepts everything greater than or equal to the provided value
	 */
    public static <E extends Comparable<? super E>> BothFilter<E> isGreaterEq(E val) {
    	return AcceptGreaterEqual.get(val) ;
    }
    
    /**
	 * Returns a partial order filter that accepts everything greater than or equal to the provided lower bound (first argument) and
	 * everything strictly less than the provided upper bound (second argument), as determined by the <code>Comparator</code> provided to
	 * its methods by utilising classes.
	 * 
	 * @param lb
	 *            inclusive lower limit of acceptable values
	 * @param ub
	 *            exclusive upper limit of acceptable values
	 * @return a filter that accepts everything in the range <code>[lb...ub)</code>
	 */
    public static <E> FilterPartialOrder<E> isBetween(E lb, E ub) {
    	return PartialOrderAcceptBetween.get(lb, ub) ;
    }
    
    /**
	 * Returns a filter that accepts everything greater than or equal to the provided lower bound (first argument) and everything strictly
	 * less than the provided upper bound (second argument), as determined by the <code>Comparator</code> provided to its methods by
	 * utilising classes.
	 * <p>
	 * Returns an object implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
	 * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
	 * methods utilise the provided comparators
	 * 
	 * @param lb
	 *            inclusive lower limit of acceptable values
	 * @param ub
	 *            exclusive upper limit of acceptable values
	 * @return a filter that accepts everything in the range <code>[lb...ub)</code>
	 */
    public static <E extends Comparable<? super E>> BothFilter<E> isBetween(E lb, E ub) {
    	return AcceptBetween.get(lb, ub) ;
    }
    
    /**
	 * Returns a <code>FilterPartialOrder</code> which delegates to the provided filter, but ignores the comparator provided to it, always
	 * using the one provided here instead. That is, this forces the comparator used by the provided filter to always be the one provided
	 * here.
	 * 
	 * @param filter
	 *            the partial order filter to wrap
	 * @param cmp
	 *            the comparator to pass to its accepts() methods, overriding the one provided by utilising classes
	 * @return a <code>FilterPartialOrder</code> which always uses the provided comparator
	 */
    public static <E> FilterPartialOrder<E> forceComparator(FilterPartialOrder<E> filter, Comparator<? super E> cmp) {
    	return forceComparator(filter, cmp, Functions.<E>identity()) ;
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
    public static <S, T> FilterPartialOrder<T> forceComparator(FilterPartialOrder<S> filter, Comparator<? super S> cmp, Function<? super T, ? extends S> f) {
    	return PartialOrderOverride.get(filter, cmp, f) ;
    }
    
    /**
	 * Returns a <code>Filter</code> which returns <code>true</code> iff it has never seen the value being tested before, using regular
	 * object equality. It maintains a set of all visited values and therefore can be expensive with respect to memory utilisation.
	 * 
	 * @return a filter accepting only unique values
	 */
    public static final <E> Filter<E> unique() {
    	return AcceptUnique.<E>get() ;
    }
    
    /**
	 * Returns a <code>Filter</code> which returns <code>true</code> iff it has never seen the value being tested before, using the provided
	 * equality. It maintains a set of all visited values and therefore can be expensive with respect to memory utilisation.
	 * 
	 * @param eq the equality determining uniqueness
	 * @return a filter accepting only unique values
	 */
    public static final <E> Filter<E> unique(Equality<? super E> eq) {
    	return AcceptUnique.<E>get(eq) ;
    }
    
    /**
	 * Returns a <code>Filter</code> which returns <code>true</code> iff it has never seen the value being tested before, using the provided
	 * set to maintain all visited values, and hence the set's definition of equality. This can be expensive with respect to memory utilisation.
	 * 
	 * @param set the set to store visited values in
	 * @return a filter accepting only unique values
	 */
    public static final <E> Filter<E> unique(Set<E> set) {
    	return AcceptUnique.<E>get(set) ;
    }
    
    /**
     * Returns a partial order filter which will accept a value only if it has never previously seen a value greater than or equal to the value being tested;
     * in an ordered set this results in unique values being efficiently obtained if applied in an ascending order visit of some kind; it is a one shot filter, however,
     * given the state stored.
     * 
     * @return a filter accepting a unique sequence if applied in ascending order
     */
    public static final <E> FilterPartialOrder<E> uniqueAsc() {
		return AcceptUniqueAscendingSequence.<E>get() ;
	}
	
    /**
     * Returns a partial order filter which will accept a value only if it has never previously seen a value less than or equal to the value being tested;
     * in an ordered set this results in unique values being efficiently obtained if applied in an descending order visit of some kind; it is a one shot filter, however,
     * given the state stored.
     * 
     * @return a filter accepting a unique sequence if applied in descending order
     */
	public static final <E> FilterPartialOrder<E> uniqueDesc() {
		return AcceptUniqueDescendingSequence.<E>get() ;
	}
	
    /**
     * Returns a filter accepting everything that is not null     * 
     * @return a filter accepting everything that is not null
     */    
	public static <E> Filter<E> notNull() {
    	return new AcceptIfNotNull<E>() ;
    }

    /**
     * Returns a filter accepting only values that are null
     * @return a filter accepting only values that are null
     */
	public static <E> Filter<E> isNull() {
    	return new AcceptIfNull<E>() ;
    }

    /**
     * Returns a filter that applies the provided function to its input before delegating to the provided filter
     * 
     * @param mapping the function to transform the input variables
     * @param filter the delegate filter
     * @return a filter that applies the provided function to its input before delegating to the provided filter
     */
    public static <X, Y> MappedFilter<X, Y> mapped(Function<X, Y> mapping, Filter<Y> filter) {
        return new MappedFilter<X, Y>(mapping, filter) ;
    }
    
    /**
     * Returns a filter accepting strings that match the supplied pattern
     * 
     * @param pattern the pattern to filter by
     * @return a filter accepting strings that match the supplied pattern
     */
    public static Filter<String> matches(Pattern pattern) {
    	return new FilterPattern(pattern) ;
    }
    
    /**
     * Returns a filter accepting strings that match the supplied pattern
     * 
     * @param pattern the pattern to filter by
     * @return a filter accepting strings that match the supplied pattern
     */
    public static Filter<String> matches(String pattern) {
    	return matches(Pattern.compile(pattern)) ;
    }

    /**
     * Returns a filter accepting values that are members of the provided set
     * @param set set of values to accept
     * @return a filter accepting values that are members of the provided set
     */
    public static <E> Filter<E> isMemberOf(AnySet<E> set) {
    	return new AcceptIfMember<E>(set) ;
    }

    /**
	 * Returns a filter accepting values that occur in the provided <code>Iterable</code>.
	 * A set is constructed containing these values, so this can be expensive with respect
	 * to memory utilisation.
	 * 
	 * @param set
	 *            <code>Iterable</code> of values to accept
	 * @return a filter accepting values that occur in the provided <code>Iterable</code>
	 */
    public static <E> Filter<E> isMemberOf(Iterable<E> set) {
    	return new AcceptIfMember<E>(set) ;
    }

	/**
	 * Returns a new <code>ArrayList</code> representing the items from the provided Collection that when passed to the provided filter's
	 * <code>accept()</code> method, returned <code>true</code>. Equivalent to <code>apply(filter, coll)</code>.
	 * 
	 * @param coll
	 *            the collection to be filtered
	 * @param filter
	 *            the filter to apply
	 * @return an eagerly filtered copy of the provided collection
	 */
    public static <E> List<E> apply(Collection<E> coll, Filter<? super E> filter) {
        return apply(filter, coll) ;
    }

	/**
	 * Returns an <code>Iterable</code> whose <code>iterator()</code> method will yield a lazily filtered <code>Iterator</code> wrapping the
	 * one returned by the Iterable provided to this method, filtered by the provided filter. Equivalent to <code>apply(filter, iter)</code>.
	 * 
	 * @param iter
	 *            <code>Iterable</code> to filter
	 * @param filter
	 *            the filter to apply
	 * @return a filtering <code>Iterable</code>
	 */
    public static <E> FilteredIterable<E> apply(Iterable<E> iter, Filter<? super E> filter) {
        return apply(filter, iter) ;
    }
    
    /**
	 * Returns an <code>Iterator</code> wrapping the one provided and lazily filtering its contents by the provided filter. Equivalent to
	 * <code>apply(filter, iter)</code>.
	 * 
	 * @param iter
	 *            the <code>Iterator</code> to filter
	 * @param filter
	 *            the filter to apply
	 * @return a filtered <code>Iterator</code>
	 */
    public static <E> FilteredIterator<E> apply(Iterator<E> iter, Filter<? super E> filter) {
        return apply(filter, iter) ;
    }
    
    /**
	 * Returns a <code>ClosableIterator</code> wrapping the one provided and lazily filtering its contents by the provided filter
	 * 
	 * @param iter
	 *            the <code>ClosableIterator</code> to filter
	 * @param filter
	 *            the filter to apply
	 * @return a filtered <code>ClosableIterator</code>
	 */
    public static <E> FilteredClosableIterator<E> apply(ClosableIterator<E> iter, Filter<? super E> filter) {
        return apply(filter, iter) ;
    }

	/**
	 * Returns a new <code>ArrayList</code> representing the items from the provided Collection that when passed to the provided filter's
	 * <code>accept()</code> method, returned <code>true</code>. Equivalent to <code>apply(coll, filter)</code>.
	 * 
	 * @param coll
	 *            the collection to be filtered
	 * @param filter
	 *            the filter to apply
	 * @return an eagerly filtered copy of the provided collection
	 */
    public static <E> List<E> apply(Filter<? super E> filter, Collection<E> coll) {
    	List<E> ret = new ArrayList<E>(coll.size()) ;
    	for (E e : coll) if (filter.accept(e)) ret.add(e) ;
    	return ret ;
    }
    
	/**
	 * Returns an <code>Iterable</code> whose <code>iterator()</code> method will yield a lazily filtered <code>Iterator</code> wrapping the
	 * one returned by the Iterable provided to this method, filtered by the provided filter. Equivalent to <code>apply(iter, filter)</code>.
	 * 
	 * @param iter
	 *            <code>Iterable</code> to filter
	 * @param filter
	 *            the filter to apply
	 * @return a filtering <code>Iterable</code>
	 */
    public static <E> FilteredIterable<E> apply(Filter<? super E> filter, Iterable<E> iter) {
    	return new FilteredIterable<E>(iter, filter) ;
    }
    
    /**
	 * Returns an <code>Iterator</code> wrapping the one provided and lazily filtering its contents by the provided filter. Equivalent to
	 * <code>apply(filter, iter)</code>.
	 * 
	 * @param iter
	 *            the <code>Iterator</code> to filter
	 * @param filter
	 *            the filter to apply
	 * @return a filtered <code>Iterator</code>
	 */
    public static <E> FilteredIterator<E> apply(Filter<? super E> filter, Iterator<E> iter) {
    	return new FilteredIterator<E>(iter, filter) ;
    }
    
    /**
	 * Returns an <code>ClosableIterator</code> wrapping the one provided and lazily filtering its contents by the provided filter. Equivalent to
	 * <code>apply(filter, iter)</code>.
	 * 
	 * @param iter
	 *            the <code>ClosableIterator</code> to filter
	 * @param filter
	 *            the filter to apply
	 * @return a filtered <code>ClosableIterator</code>
	 */
    public static <E> FilteredClosableIterator<E> apply(Filter<? super E> filter, ClosableIterator<E> iter) {
    	return new FilteredClosableIterator<E>(iter, filter) ;
    }
    
	/**
	 * Visits the elements in the provided <code>Iterator</code>, executing <code>remove()</code> on any that <b>match</b> the provided
	 * filter (i.e. where the <code>accept()</code> method returns <code>true</code>). Once <code>removeAtMost</code> matches have been
	 * encountered and removed, or the end of the <code>Iterator</code> is reached, the method returns the number of items removed.
	 * 
	 * @param removeMatches
	 *            filter to apply as removals
	 * @param removeAtMost
	 *            maximum number of removals to perform
	 * @param iter
	 *            iterator to remove from
	 * @return the number of matching values found and removed
	 */
	public static <V> int remove(Filter<? super V> removeMatches, int removeAtMost, Iterator<V> iter) {
		if (removeAtMost < 0)
			throw new IllegalArgumentException("Cannot remove fewer than zero elements") ;
		int c = 0 ;
		while (c != removeAtMost & iter.hasNext()) {
			if (removeMatches.accept(iter.next())) {
				iter.remove() ;
				c += 1 ;
			}
		}
		return c ;
	}

	/**
	 * Visits the elements in the provided <code>Iterator</code>, executing <code>remove()</code> on any that <b>match</b> the provided
	 * filter (i.e. where the <code>accept()</code> method returns <code>true</code>). Once <code>removeAtMost</code> matches have been
	 * encountered and removed, or the end of the <code>Iterator</code> is reached, the method returns the first value it encountered
	 * and removed.
	 * 
	 * @param removeMatches
	 *            filter to apply as removals
	 * @param removeAtMost
	 *            maximum number of removals to perform
	 * @param iter
	 *            iterator to remove from
	 * @return the first matching value found and removed
	 */
	public static <V> V removeAndReturnFirst(Filter<? super V> removeMatches, int removeAtMost, Iterator<V> iter) {
		if (removeAtMost < 0)
			throw new IllegalArgumentException("Cannot remove fewer than zero elements") ;
		int c = 0 ;
		V r = null ;
		while (c != removeAtMost & iter.hasNext()) {
			final V next = iter.next() ;
			if (removeMatches.accept(next)) {
				iter.remove() ;
				if (c == 0)
					r = next ; 
				c += 1 ;
			}
		}
		return r ;
	}
	
	/**
	 * Returns a new <code>Iterator</code> which visits the first <code>removeAtMost</code> items from the provided <code>Iterator</code>
	 * matching the provided <code>Filter</code>, removing them from the provided <code>Filter</code> <i>as they are visited</i>.
	 * 
	 * @param removeMatches
	 *            filter to apply as removals
	 * @param removeAtMost
	 *            maximum number of removals to perform
	 * @param iter
	 *            iterator to remove from
	 * @return the values matching the provided filter, which are removed as they are visited
	 */
	public static <V> Iterator<V> removeAndReturn(Filter<? super V> removeMatches, int removeAtMost, Iterator<V> iter) {
		if (removeAtMost < 0)
			throw new IllegalArgumentException("Cannot remove fewer than zero elements") ;
		return Iters.head(removeAtMost, Iters.destroyAsConsumed(apply(removeMatches, iter))) ;
	}
	
}
