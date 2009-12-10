package org.jjoost.util.filters ;

import java.util.List ;

import org.jjoost.collections.ArbitrarySet ;
import org.jjoost.collections.SetMaker ;
import org.jjoost.util.Filter ;

public class AcceptIfMember<E> implements Filter<E> {

	public static <E> AcceptIfMember<E> get(List<E> members) {
		return new AcceptIfMember<E>(members) ;
	}

	public static <E> AcceptIfMember<E> get(Iterable<E> members) {
		return new AcceptIfMember<E>(members) ;
	}

	public static <E> AcceptIfMember<E> get(E... members) {
		return new AcceptIfMember<E>(members) ;
	}

	public static <E> AcceptIfMember<E> get(ArbitrarySet<E> members) {
		return new AcceptIfMember<E>(members) ;
	}

	private static final long serialVersionUID = 8506853231172669315L ;
	private final ArbitrarySet<E> members ;

	public AcceptIfMember(ArbitrarySet<E> members) {
		this.members = members ;
	}

	public AcceptIfMember(E... members) {
		this.members = SetMaker.<E> hash().newScalarSet() ;
		for (E member : members)
			this.members.put(member) ;
	}

	public AcceptIfMember(Iterable<E> members) {
		this.members = SetMaker.<E> hash().newScalarSet() ;
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
