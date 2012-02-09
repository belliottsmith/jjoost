package org.jjoost.text.pattern;

public abstract class Replace<I, R extends MatchAction<I, I>> {

	protected abstract void addLiteral(String s);
	protected abstract void addCapture(String s);
	protected abstract R done();
	
	public R parse(String input) {
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
					addLiteral(new String(buf, 0, bufc));
				}
				int j = i + 1;
				boolean matched = false;
				while (j != input.length()) {
					final char c2 = input.charAt(j);
					if (escaping) {
						escaping = false;
					} else if (c2 == ']') {
						matched = true;
						addCapture(input.substring(i + 1, j));
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
			addLiteral(new String(buf, 0, bufc));
		}
		
		return done();
	}

}
