package org.jjoost.text.pattern;

import java.util.List;

import org.jjoost.text.pattern.CharScheme.Char;
import org.jjoost.text.pattern.CharScheme.CharIterator;
import org.jjoost.text.pattern.CharScheme.CharList;
import org.jjoost.text.pattern.Parse.ParseException;
import org.jjoost.util.Function;

public class StringMatcher<S extends CharSequence, R> extends Matcher<S, Char, R> {

	private static final long serialVersionUID = 7205275674097776924L;
	
	public StringMatcher(String regexp, MatchAction<S, R> action) throws ParseException {
		this(regexp, false, action);
	}
	public StringMatcher(String regexp, boolean insensitive, MatchAction<S, R> action) throws ParseException {
		super((insensitive ? CharScheme.parserCaseInsensitive() : CharScheme.parser()).compile(regexp), null, action);
	}
	
	public StringMatcher(String regexp, Capture capture, MatchAction<S, R> action) throws ParseException {
		this(regexp, false, capture, action);
	}
	public StringMatcher(String regexp, boolean insensitive, Capture capture, MatchAction<S, R> action) throws ParseException {
		super((insensitive ? CharScheme.parserCaseInsensitive() : CharScheme.parser()).compile(regexp, capture), capture, action);
	}
	
	protected StringMatcher(StringMatcher<S, R> a, StringMatcher<S, R> b) {
		super(a, b);
	}

	public StringMatcher<S, R> merge(StringMatcher<S, R> alt) {
		return new StringMatcher<S, R>(this, alt);
	}
	
	public List<R> match(S s) {
		return super.match(s, new CharIterator(s));
	}
	
	public void find(S s, Function<? super R, ?> found) {
		super.find(s, found, new CharList(s));
	}
	
	public static StringMatcher<String, Boolean> matcher(String regexp) throws ParseException {
		return new StringMatcher<String, Boolean>(regexp, TRUE);
	}
	
	public static MatchAction<String, Boolean> boolAction() {
		return TRUE;
	}
	private static final TrueAction TRUE = new TrueAction();	
	private static final class TrueAction implements MatchAction<String, Boolean> {

		private static final long serialVersionUID = 2277797439174840230L;

		@Override
		public Boolean matched(String input, Captured captured) {
			return Boolean.TRUE;
		}
		
	}
	
}
