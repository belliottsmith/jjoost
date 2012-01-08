package org.jjoost.text.pattern;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jjoost.text.pattern.CharScheme.Char;
import org.jjoost.text.pattern.CharSeqScheme.CharSeq;
import org.jjoost.text.pattern.Parse.ParseException;

public class StringSeqMatcher<R> extends Matcher<String[], Iterable<Char>, R> {

	private static final long serialVersionUID = 3310670413264979466L;

	StringSeqMatcher(Node<Iterable<Char>> node, Capture capture, MatchAction<String[], R> action) {
		super(node, capture, action);
	}

	protected StringSeqMatcher(StringSeqMatcher<R> a, StringSeqMatcher<R> b) {
		super(a, b);
	}

	public StringSeqMatcher<R> merge(StringSeqMatcher<R> alt) {
		return new StringSeqMatcher<R>(this, alt);
	}
	
	public List<R> match(String[] s) {
		return super.match(s, new StringSeq(s));
	}
	
	public static <R> Builder<R> build() {
		return new Builder<R>();
	}
	
	public static <R> Builder<R> build(MatchAction<String[], R> action) {
		return new Builder<R>().onMatch(action);
	}
	
	public static final class StringSeq implements Iterator<CharSeq> {

		final List<String> seq;
		int p = 0;
		StringSeq(String[] seq) {
			this.seq = Arrays.asList(seq);
		}
		StringSeq(List<String> seq) {
			this.seq = seq;
		}

		@Override
		public boolean hasNext() {
			return p < seq.size();
		}

		@Override
		public CharSeq next() {
			return new CharSeq(seq.get(p++));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	public static final class Builder<R> {

		private MatchAction<String[], R> action;
		private String pattern;
		private CharSeqScheme scheme = new CharSeqScheme();
		private Capture capture;
		
		public Builder<R> capture(Capture capture) {
			this.capture = capture;
			return this;
		}
		
		public Builder<R> add(String name, String pattern) throws ParseException {
			scheme.setPattern(name, pattern);
			return this;
		}
		
		public Builder<R> onMatch(MatchAction<String[], R> action) {
			this.action = action;
			return this;
		}
		
		public Builder<R> pattern(String pattern) {
			return this;
		}
		
		public StringSeqMatcher<R> done() throws ParseException {
			return new StringSeqMatcher<R>(scheme.parser().compile(pattern, capture), capture, action);
		}
		
	}
	
}
