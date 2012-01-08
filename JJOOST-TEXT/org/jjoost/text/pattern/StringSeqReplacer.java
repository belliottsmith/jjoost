package org.jjoost.text.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jjoost.text.pattern.CharScheme.Char;
import org.jjoost.text.pattern.Parse.ParseException;
import org.jjoost.text.pattern.StringSeqMatcher.StringSeq;

public class StringSeqReplacer extends Matcher<List<String>, Iterable<Char>, List<String>> {

	private static final long serialVersionUID = 5297486235666030752L;

	StringSeqReplacer(Node<Iterable<Char>> node, Capture capture, MatchAction<List<String>, List<String>> action) {
		super(node, capture, action);
	}

	protected StringSeqReplacer(StringSeqReplacer a, StringSeqReplacer b) {
		super(a, b);
	}

	public StringSeqReplacer merge(StringSeqReplacer alt) {
		return new StringSeqReplacer(this, alt);
	}
	
	public List<List<String>> match(List<String> s) {
		return super.match(s, new StringSeq(s));
	}
	
	public List<List<String>> match(String ... s) {
		return super.match(Arrays.asList(s), new StringSeq(s));
	}
	
	public static Builder build() {
		return new Builder();
	}
	
	private static interface Part {
		List<String> f(List<String> i, Captured captured);
	}
	
	private static final class LiteralPart implements Part {
		final List<String> literal;
		private LiteralPart(String literal) {
			this.literal = Arrays.asList(literal);
		}
		@Override
		public List<String> f(List<String> i, Captured captured) {
			return literal;
		}
		public String toString() {
			return literal.get(0); 
		}
	}
	
	private static final class CapturedPart implements Part {
		final int id;
		private CapturedPart(int id) {
			this.id = id;
		}
		@Override
		public List<String> f(List<String> i, Captured captured) {
			final int[] starts = captured.starts[id];
			final int[] ends = captured.ends[id];
			if (starts == null || starts.length == 0) {
				return Collections.emptyList();
			}
			return i.subList(starts[0], ends[0]);
		}
		public String toString() {
			return "\\" + id; 
		}
	}

	private static final class StringSeqReplace implements MatchAction<List<String>, List<String>> {
		
		private static final long serialVersionUID = 7825868873125202926L;
		final Capture capture;
		final Part[] parts;
		private StringSeqReplace(Capture capture, Part[] parts) {
			this.capture = capture;
			this.parts = parts;
		}

		@Override
		public List<String> matched(List<String> input, Captured captured) {
			final List<String> b = new ArrayList<String>();
			for (int i = 0 ; i != parts.length ; i++) {
				b.addAll(parts[i].f(input, captured));
			}
			return b;
		}

	}
	
	private static final class BuildReplace extends Replace<List<String>, StringSeqReplace> {

		final List<Part> parts = new ArrayList<Part>();
		final List<int[]> captures = new ArrayList<int[]>();
		
		@Override
		protected void addLiteral(String s) {
			parts.add(new LiteralPart(s));
		}

		@Override
		protected void addCapture(String s) {
			captures.add(Capture.parse(s));
			parts.add(null);
		}

		@Override
		protected StringSeqReplace done() {
			final Capture capture = new Capture(captures.toArray(new int[captures.size()][]));
			int j = 0;
			for (int i = 0 ; i != parts.size() ; i++) {
				if (parts.get(i) == null) {
					parts.set(i, new CapturedPart(capture.labelid(j++)));
				}
			}
			return new StringSeqReplace(capture, parts.toArray(new Part[parts.size()]));
		}
		
	}

	public static final class Builder {

		private CharSeqScheme scheme = new CharSeqScheme();
		private MatchAction<List<String>, List<String>> action;
		private String pattern;
		private Capture capture;
		
		public Builder add(String name, String pattern) throws ParseException {
			scheme.setPattern(name, pattern);
			return this;
		}
		
		public Builder match(String pattern) {
			this.pattern = pattern;
			return this;
		}
		
		public Builder replaceWith(String pattern) {
			final StringSeqReplace r = new BuildReplace().parse(pattern);
			this.capture = r.capture;
			this.action = r;
			return this;
		}
		
		public StringSeqReplacer done() throws ParseException {
			return new StringSeqReplacer(scheme.parser().compile(pattern, capture), capture, action);
		}
		
	}
	
}
