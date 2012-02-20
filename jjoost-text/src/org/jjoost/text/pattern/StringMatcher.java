package org.jjoost.text.pattern;

import java.util.Arrays;
import java.util.List;

import org.jjoost.collections.MultiSet;
import org.jjoost.collections.sets.serial.SerialCountingMultiHashSet;
import org.jjoost.text.pattern.CharScheme.Char;
import org.jjoost.text.pattern.CharScheme.CharIterator;
import org.jjoost.text.pattern.CharScheme.CharList;
import org.jjoost.text.pattern.Parse.ParseException;
import org.jjoost.util.Function;
import org.jjoost.util.Objects;

public class StringMatcher<S extends CharSequence, R> extends Matcher<S, Char, R> {

	private static final long serialVersionUID = 7205275674097776924L;
	
	public StringMatcher(String regexp, MatchAction<? super S, R> action) throws ParseException {
		this(regexp, false, action);
	}
	public StringMatcher(String regexp, boolean insensitive, MatchAction<? super S, R> action) throws ParseException {
		super((insensitive ? CharScheme.parserCaseInsensitive() : CharScheme.parser()).compile(regexp), null, action);
	}
	
	public StringMatcher(String regexp, Capture capture, MatchAction<? super S, R> action) throws ParseException {
		this(regexp, false, capture, action);
	}
	public StringMatcher(String regexp, boolean insensitive, Capture capture, MatchAction<? super S, R> action) throws ParseException {
		super((insensitive ? CharScheme.parserCaseInsensitive() : CharScheme.parser()).compile(regexp, capture), capture, action);
	}
	
	private StringMatcher() {
		super(new Node<Char>(CharScheme.get().emptyMap(), IdSet.empty(), false), null, null);
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
	
	public R matchOne(S s) {
		return super.matchOneResult(s, new CharIterator(s));
	}
	
	public void applyToResults(S s, Function<? super R, Boolean> found) {
		super.applyToResults(s, new CharList(s), found);
	}
	
	public void applyToMatches(S s, Function<? super Match<R>, Boolean> found) {
		super.applyToMatches(s, new CharList(s), found);
	}
	
	public void applyToLongestMatches(S s, Function<? super Match<R>, Boolean> found) {
		super.applyToLongestMatches(s, new CharList(s), found);
	}
	
	public void applyToMatchGroups(S s, Function<? super MatchGroup, Boolean> found) {
		super.applyToMatchGroups(s, new CharList(s), found);
	}
	
	public List<R> findAll(S s) {
		return super.findAllResults(s, new CharList(s));
	}
	
	public List<R> findFirst(S s) {
		return super.findFirstResults(s, new CharList(s));
	}
	
	public R findFirstOne(S s) {
		return super.findFirstOneResult(s, new CharList(s));
	}
	
	public static StringMatcher<String, Boolean> matcher(String regexp) throws ParseException {
		return new StringMatcher<String, Boolean>(regexp, TRUE);
	}
	
	public static StringMatcher<String, Boolean> matcher(String regexp, boolean ignoreCase) throws ParseException {
		return new StringMatcher<String, Boolean>(regexp, ignoreCase, TRUE);
	}
	
	public static <E> StringMatcher<String, E> matcher(String regexp, MatchAction<? super String, E> action) throws ParseException {
		return new StringMatcher<String, E>(regexp, action);
	}
	
	public static <E> StringMatcher<String, E> matcher(String regexp, Capture capture, MatchAction<? super String, E> action) throws ParseException {
		return new StringMatcher<String, E>(regexp, capture, action);
	}
	
	public static <E> StringMatcher<String, E> matcher(String regexp, boolean ignoreCase, MatchAction<? super String, E> action) throws ParseException {
		return new StringMatcher<String, E>(regexp, ignoreCase, action);
	}
	
	public static <E> StringMatcher<String, E> matcher(String regexp, boolean ignoreCase, Capture capture, MatchAction<? super String, E> action) throws ParseException {
		return new StringMatcher<String, E>(regexp, ignoreCase, capture, action);
	}
	
	public static StringMatcher<CharSequence, Boolean> seqMatcher(String regexp) throws ParseException {
		return new StringMatcher<CharSequence, Boolean>(regexp, TRUE);
	}
	
	public static StringMatcher<CharSequence, Boolean> seqMatcher(String regexp, boolean ignoreCase) throws ParseException {
		return new StringMatcher<CharSequence, Boolean>(regexp, ignoreCase, TRUE);
	}
	
	public static <E> StringMatcher<CharSequence, E> seqMatcher(String regexp, boolean ignoreCase, MatchAction<? super CharSequence, E> action) throws ParseException {
		return new StringMatcher<CharSequence, E>(regexp, ignoreCase, action);
	}
	
	public static <E> StringMatcher<CharSequence, E> seqMatcher(String regexp, boolean ignoreCase, Capture capture, MatchAction<? super CharSequence, E> action) throws ParseException {
		return new StringMatcher<CharSequence, E>(regexp, ignoreCase, capture, action);
	}
	
	public static <E> StringMatcher<String, E> matchNothing() {
		return new StringMatcher<String, E>();
	}
	
	public static <E> StringMatcher<CharSequence, E> seqMatchNothing() {
		return new StringMatcher<CharSequence, E>();
	}
	
	public String replaceAllMatches(final S input, final Function<? super String, ? extends CharSequence> replaceFunc) {
		final StringBuilder sb = new StringBuilder();
		class MyFunc implements Function<MatchGroup, Boolean> {
			private static final long serialVersionUID = 1L;
			int last = 0;
			@Override
			public Boolean apply(MatchGroup v) {
				if (v.start >= last) {
					sb.append(input.subSequence(last, v.start));
					sb.append(replaceFunc.apply(input.subSequence(v.start, v.end).toString()));
					last = v.end;
				}
				return Boolean.TRUE;
			}
		}
		final MyFunc myFunc = new MyFunc();
		applyToMatchGroups(input, myFunc);
		sb.append(input.subSequence(myFunc.last, input.length()));
		return sb.toString();
	}
	
	public boolean containsAnyMatch(S match) {
		@SuppressWarnings("serial")
		class Contains implements Function<MatchGroup, Boolean> {
			private boolean contains = false;
			@Override
			public Boolean apply(MatchGroup c) {
				contains = true;
				return Boolean.FALSE;
			}
		};
		final Contains contains = new Contains();
		applyToMatchGroups(match, new CharList(match), contains);
		return contains.contains;
	}
	
	public MultiSet<R> countMatches(S match) {
		final MultiSet<R> r = new SerialCountingMultiHashSet<R>();
		countMatches(match, r);
		return r;
	}
	
	public void countMatches(S match, final MultiSet<R> r) {
		applyToResults(match, new CharList(match), new Function<R, Boolean>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Boolean apply(R c) {
				r.add(c);
				return true;
			}
		});
	}
	
	public static <I extends CharSequence, R> StringMatcher<I, R> merge(List<StringMatcher<I, R>> matchers) {
		if (matchers.size() < 10) {
			StringMatcher<I, R> r = matchers.get(0);
			for (int i = 1 ; i < matchers.size() ; i++) {
				r = r.merge(matchers.get(i));
			}
			return r;
		} else {
			return merge(Arrays.asList(
					merge(matchers.subList(0, matchers.size() / 2)), 
					merge(matchers.subList(matchers.size() / 2, matchers.size()))
			));
		}
	}

	public static <E> ConstAction<E> constAction(E val) {
		return new ConstAction<E>(val);
	}
	
	public static final class ConstAction<E> implements MatchAction<CharSequence, E> {

		private static final long serialVersionUID = 2277797439174840214L;
		final E val;
		public ConstAction(E val) {
			this.val = val;
		}

		@Override
		public E matched(CharSequence input, Captured captured) {
			return val;
		}
		
		public boolean equals(Object that) {
			return that instanceof ConstAction && Objects.equalQuick(((ConstAction<?>) that).val, val);
		}
		
		public int hashCode() {
			return val == null ? 0 : val.hashCode();
		}
		
	}

	
	public static MatchAction<CharSequence, Boolean> trueAction() {
		return TRUE;
	}
	private static final TrueAction TRUE = new TrueAction();	
	private static final class TrueAction implements MatchAction<CharSequence, Boolean> {

		private static final long serialVersionUID = 2277797439174840230L;

		@Override
		public Boolean matched(CharSequence input, Captured captured) {
			return Boolean.TRUE;
		}
		
		public boolean equals(Object that) {
			return that instanceof TrueAction;
		}
		
		public int hashCode() {
			return 1;
		}
		
	}
	
	public static MatchAction<CharSequence, Boolean> falseAction() {
		return FALSE;
	}
	private static final FalseAction FALSE = new FalseAction();	
	private static final class FalseAction implements MatchAction<CharSequence, Boolean> {
		
		private static final long serialVersionUID = 2277797439174840279L;
		
		@Override
		public Boolean matched(CharSequence input, Captured captured) {
			return Boolean.FALSE;
		}
		
		public boolean equals(Object that) {
			return that instanceof FalseAction;
		}
		
		public int hashCode() {
			return 0;
		}
		
	}
	
}
