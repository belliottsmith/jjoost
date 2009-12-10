package org.jjoost.util.filters ;

public class AcceptGreater<E extends Comparable<? super E>> extends PartialOrderAcceptGreater<E> implements BothFilter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	public AcceptGreater(E than) {
		super(than) ;
	}

	public boolean accept(E test) {
		return test.compareTo(than) > 0 ;
	}

	public static <E extends Comparable<? super E>> AcceptGreater<E> get(E than) {
		return new AcceptGreater<E>(than) ;
	}

	public String toString() {
		return "is greater than " + than ;
	}

}
