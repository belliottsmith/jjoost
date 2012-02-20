package org.jjoost.text.pattern;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jjoost.util.Function;

@SuppressWarnings("unchecked")
public class Matcher<I, S, R> implements Serializable {

	private static final long serialVersionUID = 342206936206227320L;
	
	public final Node<S> node;	
	final Capture[] capture;
	final MatchAction<? super I, ? extends R>[] result;
	
	protected Matcher(Matcher<I, S, ? extends R> a, Matcher<I, S, ? extends R> b) {
		if (Arrays.equals(a.capture, b.capture) && Arrays.equals(a.result, b.result)) {
			this.node = a.node.mergeAlternatePatternWithSameCaptureAndResult(b.node);
			this.capture = a.capture;
			this.result = a.result;			
		} else {
			this.node = a.node.mergeAlternatePattern(b.node, a.result.length, true);
			if (a.capture == null && b.capture == null) {
				this.capture = null;
			} else if (a.capture == null) {
				this.capture = new Capture[a.result.length + b.result.length];
				Arrays.fill(capture, 0, a.result.length, new Capture());
				System.arraycopy(b.capture, 0, capture, a.result.length, b.capture.length);
			} else if (b.capture == null) {
				this.capture = new Capture[a.result.length + b.result.length];
				System.arraycopy(a.capture, 0, capture, 0, a.result.length);
				Arrays.fill(capture, a.result.length, a.result.length + b.result.length, new Capture());
			} else {
				this.capture = new Capture[a.capture.length + b.capture.length];
				System.arraycopy(a.capture, 0, capture, 0, a.capture.length);
				System.arraycopy(b.capture, 0, capture, a.capture.length, b.capture.length);
			}
			this.result = new MatchAction[a.result.length + b.result.length];
			System.arraycopy(a.result, 0, result, 0, a.result.length);
			System.arraycopy(b.result, 0, result, a.result.length, b.result.length);
		}
	}
	
	protected Matcher(Node<S> node, Capture capture, MatchAction<? super I, R> action) {
		this.node = node;
		this.capture = capture == null ? null : new Capture[] { capture };
		this.result = new MatchAction[] { action };
	}

	protected List<R> match(I input, Iterator<? extends S> sequence) {
		final Captured[] captured = node.match(capture, sequence);
		if (captured.length == 0) {
			return Collections.emptyList();
		}
		final R[] results = (R[]) new Object[captured.length];
		for (int i = 0 ; i != results.length ; i++) {
			results[i] = result[captured[i].id].matched(input, captured[i]);
		}
		return Arrays.asList(results);
	}
	
	protected R matchOneResult(I input, Iterator<? extends S> sequence) {
		final List<R> matches = match(input, sequence);
		if (matches.isEmpty()) {
			return null;
		}
		return matches.get(0);
	}
	
	protected List<R> findAllResults(I input, List<? extends S> sequence) {
		final List<R> r = new ArrayList<R>();
		applyToResults(input, sequence, new org.jjoost.util.Function<R, Boolean>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Boolean apply(R v) {
				r.add(v);
				return Boolean.TRUE;
			}
		});
		return r;
	}
	
	protected boolean contains(I input, List<? extends S> sequence) {
		return !findFirstResults(input, sequence).isEmpty();
	}
	
	protected List<R> findFirstResults(I input, List<? extends S> sequence) {
		final MatchGroup found = node.findFirst(capture, sequence);
		if (found == null) {
			return Collections.emptyList();
		}
		final R[] results = (R[]) new Object[found.matched.length];
		for (int i = 0 ; i != results.length ; i++) {
			results[i] = result[found.matched[i].id].matched(input, found.matched[i]);
		}
		return Arrays.asList(results);
	}
	
	protected final R findFirstOneResult(I input, List<? extends S> sequence) {
		final List<R> found = findFirstResults(input, sequence);
		if (found.isEmpty()) {
			return null;
		}
		return found.get(0);
	}
	
	protected void applyToResults(final I input, List<? extends S> sequence, final Function<? super R, Boolean> f) {
		node.find(capture, sequence, new Function<MatchGroup, Boolean>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Boolean apply(MatchGroup found) {
				Boolean r = null;
				for (int i = 0 ; (r == null || r) && i != found.matched.length ; i++) {
					r = f.apply(result[found.matched[i].id].matched(input, found.matched[i]));
				}				
				return r;
			}
		});
	}
	
	protected void applyToMatches(final I input, List<? extends S> sequence, final Function<? super Match<R>, Boolean> f) {
		node.find(capture, sequence, new Function<MatchGroup, Boolean>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Boolean apply(MatchGroup found) {
				Boolean r = null;
				for (int i = 0 ; (r == null || r) && i != found.matched.length ; i++) {
					r = f.apply(new Match<R>(
							found.start, found.end, found.matched[i], 
							result[found.matched[i].id], 
							result[found.matched[i].id].matched(input, found.matched[i])));
				}
				return r;
			}
		});
	}
	
	protected void applyToLongestMatches(final I input, List<? extends S> sequence, final Function<? super Match<R>, Boolean> f) {
		final class MyFunc implements Function<MatchGroup, Boolean> {
			private static final long serialVersionUID = 1L;
			MatchGroup last = null;
			
			@Override
			public Boolean apply(MatchGroup found) {
				if (last != null) {
					if (last.end < found.end) {
						if (last.start < found.start) {
							final Boolean r = _apply(last);
							if (r != null && !r) {
								last = null;
								return Boolean.FALSE;
							}
						}
					} else {
						return Boolean.TRUE;
					}
				}
				last = found;
				return Boolean.TRUE;
			}
			private Boolean _apply(MatchGroup found) {
				Boolean r = null;				
				for (int i = 0 ; (r == null || r) && i != found.matched.length ; i++) {
					r = f.apply(new Match<R>(
							found.start, found.end, found.matched[i], 
							result[found.matched[i].id], 
							result[found.matched[i].id].matched(input, found.matched[i])));
				}
				return r;
			}
		}
		final MyFunc f2 = new MyFunc();
		node.find(capture, sequence, f2);
		if (f2.last != null) {
			f2._apply(f2.last);
		}
	}
	
	protected void applyToMatchGroups(final I input, List<? extends S> sequence, final Function<? super MatchGroup, Boolean> f) {
		node.find(capture, sequence, f);
	}
	
}
