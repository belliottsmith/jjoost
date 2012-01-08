package org.jjoost.text.pattern;

import java.util.Iterator;

import org.jjoost.text.pattern.CharScheme.Char;
import org.jjoost.text.pattern.CharScheme.CharIterator;
import org.jjoost.text.pattern.Parse.ParseException;

public class CharSeqScheme extends SeqScheme<Char, Iterable<Char>> {

	public void setPattern(String name, String pattern) throws ParseException {
		super.setPattern(name, CharScheme.parser().compile(pattern));
	}
	
	public static final class CharSeq implements Iterable<Char> {
		final String s;
		public CharSeq(String s) {
			this.s = s;
		}
		@Override
		public Iterator<Char> iterator() {
			return new CharIterator(s);
		}
	}

	@Override
	protected NodeScheme<Char> base() {
		return CharScheme.get();
	}

}
