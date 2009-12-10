package org.jjoost.util.filters ;

public class AcceptGreaterEqual<E extends Comparable<? super E>> extends PartialOrderAcceptGreaterEqual<E> implements BothFilter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	public AcceptGreaterEqual(E than) {
		super(than) ;
	}

	public boolean accept(E test) {
		return test.compareTo(than) >= 0 ;
	}

	public static <E extends Comparable<? super E>> AcceptGreaterEqual<E> get(E than) {
		return new AcceptGreaterEqual<E>(than) ;
	}

	public String toString() {
		return "is greater or equal to " + than ;
	}

}
