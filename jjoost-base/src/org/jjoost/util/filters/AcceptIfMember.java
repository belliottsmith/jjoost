package org.jjoost.util.filters ;

import java.util.List ;

import org.jjoost.collections.AnySet ;
import org.jjoost.collections.SetMaker ;
import org.jjoost.util.Filter ;

/**
 * A filter accepting values that are members of the provided set
 * 
 * @author b.elliottsmith
 */
public class AcceptIfMember<E> implements Filter<E> {

    /**
     * Returns a filter accepting values that are members of the provided set
     * @param members set of values to accept
     * @return a filter accepting values that are members of the provided set
     */
	public static <E> AcceptIfMember<E> get(List<E> members) {
		return new AcceptIfMember<E>(members) ;
	}

    /**
     * Returns a filter accepting values that are members of the provided set
     * @param members set of values to accept
     * @return a filter accepting values that are members of the provided set
     */
	public static <E> AcceptIfMember<E> get(Iterable<E> members) {
		return new AcceptIfMember<E>(members) ;
	}

    /**
     * Returns a filter accepting values that are members of the provided set
     * @param members set of values to accept
     * @return a filter accepting values that are members of the provided set
     */
	public static <E> AcceptIfMember<E> get(E... members) {
		return new AcceptIfMember<E>(members) ;
	}

    /**
     * Returns a filter accepting values that are members of the provided set
     * @param members set of values to accept
     * @return a filter accepting values that are members of the provided set
     */
	public static <E> AcceptIfMember<E> get(AnySet<E> members) {
		return new AcceptIfMember<E>(members) ;
	}

	private static final long serialVersionUID = 8506853231172669315L ;
	private final AnySet<E> members ;

    /**
     * Constructs a new filter accepting values that are members of the provided set
     * @param members set of values to accept
     */
	public AcceptIfMember(AnySet<E> members) {
		this.members = members ;
	}

    /**
     * Constructs a new filter accepting values that are members of the provided set
     * @param members set of values to accept
     */
	public AcceptIfMember(E... members) {
		this.members = SetMaker.<E> hash().newSet() ;
		for (E member : members)
			this.members.put(member) ;
	}

    /**
     * Constructs a new filter accepting values that are members of the provided set
     * @param members set of values to accept
     */
	public AcceptIfMember(Iterable<E> members) {
		this.members = SetMaker.<E> hash().newSet() ;
		for (E member : members)
			this.members.put(member) ;
	}

	public boolean accept(E test) {
		return members.contains(test) ;
	}

	public String toString() {
		return "is member of " + members.toString() ;
	}

}
