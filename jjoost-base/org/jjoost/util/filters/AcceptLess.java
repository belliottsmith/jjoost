package org.jjoost.util.filters ;

public class AcceptLess<E extends Comparable<? super E>> extends PartialOrderAcceptLess<E> implements BothFilter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	public AcceptLess(E than) {
		super(than) ;
	}

	public boolean accept(E test) {
		return test.compareTo(than) < 0 ;
	}

	public static <E extends Comparable<? super E>> AcceptLess<E> get(E than) {
		return new AcceptLess<E>(than) ;
	}

	public String toString() {
		return "is less than " + than ;
	}

}
