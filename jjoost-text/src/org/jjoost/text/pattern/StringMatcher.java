package org.jjoost.text.pattern;

import java.util.Arrays;
import java.util.List;

import org.jjoost.collections.MultiSet;
import org.jjoost.collections.sets.serial.SerialCountingMultiHashSet;
import org.jjoost.text.pattern.CharScheme.Char;
import org.jjoost.text.pattern.CharScheme.CharIterator;
import org.jjoost.text.pattern.CharScheme.CharList;
import org.jjoost.text.pattern.Parse.ParseException;
import org.jjoost.text.pattern.StringTransformer.TransformAll;
import org.jjoost.text.pattern.StringTransformer.TransformFirst;
import org.jjoost.util.Function;
import org.jjoost.util.Objects;

public class StringMatcher<S extends CharSequence, R> extends Matcher<S, Char, R> {

	private static final long serialVersionUID = 7205275674097776924L;
	
	public StringMatcher(String regexp, MatchAction<? super S, ? extends R> action) throws ParseException {
		this(regexp, false, action);
	}
	public StringMatcher(String regexp, boolean insensitive, MatchAction<? super S, ? extends R> action) throws ParseException {
		this(regexp, (insensitive ? CharScheme.parserCaseInsensitive() : CharScheme.parser()), action);
	}
	public StringMatcher(String regexp, CharScheme.Parser parser, MatchAction<? super S, ? extends R> action) throws ParseException {
		super(parser.compile(regexp), null, action);
	}
	public StringMatcher(String regexp, Capture capture, MatchAction<? super S, ? extends R> action) throws ParseException {
		this(regexp, false, capture, action);
	}
	public StringMatcher(String regexp, boolean insensitive, Capture capture, MatchAction<? super S, ? extends R> action) throws ParseException {
		this(regexp, (insensitive ? CharScheme.parserCaseInsensitive() : CharScheme.parser()), capture, action);
	}
	public StringMatcher(String regexp, CharScheme.Parser parser, Capture capture, MatchAction<? super S, ? extends R> action) throws ParseException {
		super(parser.compile(regexp, capture), capture, action);
	}
	protected <R0> StringMatcher(StringMatcher<S, R0> copy, Function<? super MatchAction<? super S, ? extends R0>, ? extends MatchAction<? super S, ? extends R>> map, boolean keepCaptures) {
		super(copy, map, keepCaptures);
	}
	
	public MatchAction<? super S, ? extends R> getAction(int action) {
		return result[action];
	}
	
	public int getPatternCount() {
		return result.length;
	}
	
	private StringMatcher() {
		super(CharScheme.get());
	}
	
	protected StringMatcher(StringMatcher<S, R> a, StringMatcher<S, ? extends R> b, int truncateRecursionDepth) {
		super(a, b, truncateRecursionDepth);
	}

	public StringMatcher<S, R> merge(StringMatcher<S, ? extends R> alt) {
		return merge(alt, Integer.MAX_VALUE);
	}
	
	public StringMatcher<S, R> merge(StringMatcher<S, ? extends R> alt, int truncateRecursionDepth) {
		return new StringMatcher<S, R>(this, alt, truncateRecursionDepth);
	}
	
	public <R2> StringMatcher<S, R2> replaceActions(boolean keepCaptures, Function<? super MatchAction<? super S, ? extends R>, ? extends MatchAction<? super S, ? extends R2>> map) {
		return new StringMatcher<S, R2>(this, map, keepCaptures);
	}
	
	public List<R> match(S s) {
		return super.match(s, new CharIterator(s));
	}
	
	public R matchOne(S s) {
		return super.matchOneResult(s, new CharIterator(s));
	}
	
	public void applyToResults(S s, Function<? super R, FindAction> found) {
		super.applyToResults(s, new CharList(s), found);
	}
	
	public void applyToMatches(S s, Function<? super Match<R>, FindAction> found) {
		super.applyToMatches(s, new CharList(s), found);
	}
	
	public void applyToLongestMatches(S s, Function<? super Match<R>, FindAction> found) {
		super.applyToLongestMatches(s, new CharList(s), found);
	}
	
	public void applyToMatchStates(S s, Function<? super FindState, FindAction> found) {
		super.applyToMatchStates(s, new CharList(s), found);
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
	
	public static <S extends CharSequence> StringMatcher<S, Boolean> matcher(String regexp) throws ParseException {
		return new StringMatcher<S, Boolean>(regexp, TRUE);
	}
	
	public static <S extends CharSequence> StringMatcher<S, Boolean> matcher(String regexp, boolean ignoreCase) throws ParseException {
		return new StringMatcher<S, Boolean>(regexp, ignoreCase, TRUE);
	}
	
	public static <S extends CharSequence> StringMatcher<S, Boolean> matcher(String regexp, CharScheme.Parser parser) throws ParseException {
		return new StringMatcher<S, Boolean>(regexp, parser, TRUE);
	}
	
	public static <S extends CharSequence, E> StringMatcher<S, E> matcher(String regexp, MatchAction<? super S, ? extends E> action) throws ParseException {
		return new StringMatcher<S, E>(regexp, action);
	}
	
	public static <S extends CharSequence, E> StringMatcher<S, E> matcher(String regexp, Capture capture, MatchAction<? super S, ? extends E> action) throws ParseException {
		return new StringMatcher<S, E>(regexp, capture, action);
	}
	
	public static <S extends CharSequence, E> StringMatcher<S, E> matcher(String regexp, boolean ignoreCase, MatchAction<? super S, ? extends E> action) throws ParseException {
		return new StringMatcher<S, E>(regexp, ignoreCase, action);
	}
	
	public static <S extends CharSequence, E> StringMatcher<S, E> matcher(String regexp, CharScheme.Parser parser, MatchAction<? super S, ? extends E> action) throws ParseException {
		return new StringMatcher<S, E>(regexp, parser, action);
	}
	
	public static <S extends CharSequence, E> StringMatcher<S, E> matcher(String regexp, boolean ignoreCase, Capture capture, MatchAction<? super S, ? extends E> action) throws ParseException {
		return new StringMatcher<S, E>(regexp, ignoreCase, capture, action);
	}
	
	public static <S extends CharSequence, E> StringMatcher<S, E> matcher(String regexp, CharScheme.Parser parser, Capture capture, MatchAction<? super S, ? extends E> action) throws ParseException {
		return new StringMatcher<S, E>(regexp, parser, capture, action);
	}
	
	public static <S extends CharSequence, E> StringMatcher<S, E> matchNothing() {
		return new StringMatcher<S, E>();
	}
	
	public static <E> StringMatcher<CharSequence, E> seqMatchNothing() {
		return new StringMatcher<CharSequence, E>();
	}
	
	public String replaceAllMatches(final S input, final Function<? super String, ? extends CharSequence> replaceFunc) {
		final StringBuilder sb = new StringBuilder();
		class MyFunc implements Function<FindState, FindAction> {
			private static final long serialVersionUID = 1L;
			int last = 0;
			@Override
			public FindAction apply(FindState v) {
				if (v.start() >= last) {
					sb.append(input.subSequence(last, v.start()));
					sb.append(replaceFunc.apply(input.subSequence(v.start(), v.end()).toString()));
					last = v.end();
				}
				return FindAction.continueAll();
			}
		}
		final MyFunc myFunc = new MyFunc();
		applyToMatchStates(input, myFunc);
		sb.append(input.subSequence(myFunc.last, input.length()));
		return sb.toString();
	}
	
	public boolean matches(S match) {
		return super.matches(new CharIterator(match));
	}
	
	public boolean containsAnyMatch(S match) {
		@SuppressWarnings("serial")
		class Contains implements Function<FindState, FindAction> {
			private boolean contains = false;
			@Override
			public FindAction apply(FindState c) {
				contains = true;
				return FindAction.terminate();
			}
		};
		final Contains contains = new Contains();
		applyToMatchStates(match, new CharList(match), contains);
		return contains.contains;
	}
	
	public int countAllMatches(S match) {
		@SuppressWarnings("serial")
		class Count implements Function<FindState, FindAction> {
			private int count;
			@Override
			public FindAction apply(FindState c) {
				count++;
				return FindAction.continueAll();
			}
		};
		final Count contains = new Count();
		applyToMatchStates(match, new CharList(match), contains);
		return contains.count;
	}
	
	public MultiSet<R> countMatches(S match) {
		final MultiSet<R> r = new SerialCountingMultiHashSet<R>();
		countMatches(match, r);
		return r;
	}
	
	public void countMatches(S match, final MultiSet<R> r) {
		applyToResults(match, new CharList(match), new Function<R, FindAction>() {
			private static final long serialVersionUID = 1L;
			@Override
			public FindAction apply(R c) {
				r.add(c);
				return FindAction.continueAll();
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
		public E matched(CharSequence input, Found captured) {
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
		public Boolean matched(CharSequence input, Found captured) {
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
		public Boolean matched(CharSequence input, Found captured) {
			return Boolean.FALSE;
		}
		
		public boolean equals(Object that) {
			return that instanceof FalseAction;
		}
		
		public int hashCode() {
			return 0;
		}
		
	}
	
	public static <S extends CharSequence, E> StringMatcher<S, E> opts(List<String> regexps, boolean literal, E result) throws ParseException {
		java.util.Collections.sort(regexps);
		return opts(regexps, literal, constAction(result));
	}
	
	public static <S extends CharSequence> StringMatcher<S, Boolean> opts(List<String> regexps, boolean literal) throws ParseException {
		java.util.Collections.sort(regexps);
		return opts(regexps, literal, trueAction());
	}

	public static <S extends CharSequence, E> StringMatcher<S, E> opts(List<String> regexps, boolean literal, MatchAction<CharSequence, E> result) throws ParseException {
		if (regexps.size() < 4) {
			StringMatcher<S, E> r = matchNothing();
			for (String exp : regexps) {
				r = r.merge(StringMatcher.<S, E>matcher(literal ? Parse.escape(exp) : exp, result));
			}
			return r;
		} else {
			return StringMatcher.<S, E>opts(regexps.subList(0, regexps.size() >> 1), literal, result).merge(StringMatcher.<S, E>opts(regexps.subList(regexps.size() >> 1, regexps.size()), literal, result));
		}
	}
	
	public static String[] firstCaptures(String input, Found captured) {
		final String[] r = new String[captured.ends.length];
		for (int i = 0 ; i != r.length ; i++) {
			final int[] starts = captured.starts[i];
			final int[] ends = captured.ends[i];
			if (starts == null || starts.length == 0) {
				continue;
			}
			r[i] = input.substring(starts[0], ends[0]);
		}
		return r;
	}
	
	public static String[][] allCaptures(String input, Found captured) {
		final String[][] r = new String[captured.ends.length][];
		for (int i = 0 ; i != r.length ; i++) {
			final int[] starts = captured.starts[i];
			final int[] ends = captured.ends[i];
			if (starts == null || starts.length == 0) {
				continue;
			}
			final String[] trg = new String[ends.length];
			r[i] = trg;
			for (int j = 0 ; j != trg.length ; j++) {
				trg[j] = input.substring(starts[i], ends[i]);
			}
		}
		return r;
	}
	public static CharSequence[] firstCaptures(CharSequence input, Found captured) {
		final CharSequence[] r = new CharSequence[captured.ends.length];
		for (int i = 0 ; i != r.length ; i++) {
			final int[] starts = captured.starts[i];
			final int[] ends = captured.ends[i];
			if (starts == null || starts.length == 0) {
				continue;
			}
			r[i] = input.subSequence(starts[0], ends[0]);
		}
		return r;
	}
	
	public static CharSequence[][] allCaptures(CharSequence input, Found captured) {
		final CharSequence[][] r = new CharSequence[captured.ends.length][];
		for (int i = 0 ; i != r.length ; i++) {
			final int[] starts = captured.starts[i];
			final int[] ends = captured.ends[i];
			if (starts == null || starts.length == 0) {
				continue;
			}
			final CharSequence[] trg = new CharSequence[ends.length];
			r[i] = trg;
			for (int j = 0 ; j != trg.length ; j++) {
				trg[j] = input.subSequence(starts[i], ends[i]);
			}
		}
		return r;
	}
}
