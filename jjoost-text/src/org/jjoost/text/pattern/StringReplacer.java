package org.jjoost.text.pattern;

import java.util.ArrayList;
import java.util.List;

import org.jjoost.text.pattern.Parse.ParseException;

public class StringReplacer extends StringMatcher<String, String> {

	private static final long serialVersionUID = 655802573811276962L;

	public StringReplacer(String regexp, String replace) throws ParseException {
		this(regexp, new Replace(replace));
	}

	private StringReplacer(String regexp, Replace replace) throws ParseException {
		super(regexp, replace.capture, replace);
	}
	
	public StringReplacer(String regexp, boolean insensitive, String replace) throws ParseException {
		this(regexp, insensitive, new Replace(replace));
	}
	
	private StringReplacer(String regexp, boolean insensitive, Replace replace) throws ParseException {
		super(regexp, insensitive, replace.capture, replace);
	}
	
	private StringReplacer(StringReplacer a, StringReplacer b, int truncateRecusionDepth) {
		super(a, b, truncateRecusionDepth);
	}
	
	public StringReplacer merge(StringReplacer that) {
		return merge(that, Integer.MAX_VALUE);
	}
	
	public StringReplacer merge(StringReplacer that, int truncateRecursionDepth) {
		return new StringReplacer(this, that, truncateRecursionDepth);
	}

	private static interface Part {
		String f(String i, Found captured);
	}
	
	private static final class LiteralPart implements Part {
		final String literal;
		private LiteralPart(String literal) {
			this.literal = literal;
		}
		@Override
		public String f(String i, Found captured) {
			return literal;
		}
		public String toString() {
			return literal; 
		}
	}
	
	private static final class CapturedPart implements Part {
		final int id;
		private CapturedPart(int id) {
			this.id = id;
		}
		@Override
		public String f(String i, Found captured) {
			final int[] starts = captured.starts[id];
			final int[] ends = captured.ends[id];
			if (starts == null || starts.length == 0) {
				return "";
			}
			return i.substring(starts[0], ends[0]);
		}
		public String toString() {
			return "\\" + id; 
		}
	}
	
	public static final class Replace implements MatchAction<String, String> {

		private static final long serialVersionUID = -4495752181906436869L;
		final Capture capture;
		final Part[] parts;
		
		public Replace(String input) {
			final List<Part> parts = new ArrayList<Part>();
			final List<String> captures = new ArrayList<String>();
			final char[] buf = new char[input.length()];
			int bufc = 0;
			int i = 0;
			boolean escaping = false;
			while (i < input.length()) {
				
				final char c = input.charAt(i);
				if (escaping) {
					buf[bufc++] = c;
				} else if (c == '[') {
					if (bufc != 0) {
						parts.add(new LiteralPart(new String(buf, 0, bufc)));
					}
					int j = i + 1;
					boolean matched = false;
					while (j != input.length()) {
						final char c2 = input.charAt(j);
						if (escaping) {
							escaping = false;
						} else if (c2 == ']') {
							matched = true;
							captures.add(input.substring(i + 1, j));
							parts.add(null);
							break;
						} else if (c2 == '\\') {
							escaping = true;
						}
						j++;
					}
					if (!matched) {
						throw new IllegalArgumentException("Unmatched [ in expression " + input);
					}
					i = j;
					bufc = 0;
				} else if (c == '\\') {
					escaping = true;
				} else {
					buf[bufc++] = c;
				}

				i++;
			}

			if (bufc != 0) {
				parts.add(new LiteralPart(new String(buf, 0, bufc)));
			}
			this.capture = Capture.capture(captures);
			int j = 0;
			for (i = 0 ; i != parts.size() ; i++) {
				if (parts.get(i) == null) {
					parts.set(i, new CapturedPart(capture.labelid(j++)));
				}
			}
			this.parts = parts.toArray(new Part[parts.size()]);
		}

		@Override
		public String matched(String input, Found captured) {
			final StringBuilder b = new StringBuilder();
			for (int i = 0 ; i != parts.length ; i++) {
				b.append(parts[i].f(input, captured));
			}
			return b.toString();
		}
		
		public Capture capture() {
			return capture;
		}
		
	}
	
}
