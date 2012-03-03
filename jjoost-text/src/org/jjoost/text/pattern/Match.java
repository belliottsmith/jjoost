package org.jjoost.text.pattern;

public final class Match<E> implements Comparable<Match<?>> {

	public final int start;
	public final int end;
	public final Found captured;
	public final MatchAction<?, ? extends E> action;
	public final E match;
	
	Match(int start, int end, Found captured, MatchAction<?, ? extends E> action, E match) {
		this.start = start;
		this.end = end;
		this.captured = captured;
		this.action = action;
		this.match = match;
	}

	@Override
	public int compareTo(Match<?> that) {
		final int r = this.start - that.start;
		if (r != 0) {
			return r;
		}
		return that.end - this.end;
	}

}
