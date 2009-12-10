package org.jjoost.util.filters ;

public class AcceptLessEqual<E extends Comparable<? super E>> extends PartialOrderAcceptLessEqual<E> implements BothFilter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	public AcceptLessEqual(E than) {
		super(than) ;
	}

	public boolean accept(E test) {
		return test.compareTo(than) <= 0 ;
	}

	public static <E extends Comparable<? super E>> AcceptLessEqual<E> get(E than) {
		return new AcceptLessEqual<E>(than) ;
	}

	public String toString() {
		return "is less than or equal to " + than ;
	}

}
