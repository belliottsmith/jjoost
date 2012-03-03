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
	
	protected Matcher(NodeScheme<S> scheme) {
		this.node = scheme.empty();
		this.capture = new Capture[0];
		this.result = new MatchAction[0];
	}

	protected Matcher(Matcher<I, S, ? extends R> a, Matcher<I, S, ? extends R> b, int truncateRecursionDepth) {
		if (a.result.length == 0 ) {
			this.node = b.node;
			this.capture = b.capture;
			this.result = b.result;
		} else if (b.result.length == 0) {
			this.node = a.node;
			this.capture = a.capture;
			this.result = a.result;
		} else if (a.result.length == 1 && Arrays.equals(a.result, b.result) && ((a.capture == null && b.capture == null) || Arrays.equals(a.capture, b.capture))) {
			this.node = a.node.mergeAlternatePath(b.node, true).get();
			this.capture = a.capture;
			this.result = a.result;			
		} else {
			this.node = a.node.mergeAlternatePattern(b.node, a.result.length, true, truncateRecursionDepth);
			if (a.capture == null && b.capture == null) {
				this.capture = null;
			} else if (a.capture == null) {
				this.capture = new Capture[a.result.length + b.result.length];
				Arrays.fill(capture, 0, a.result.length, Capture.capture());
				System.arraycopy(b.capture, 0, capture, a.result.length, b.capture.length);
			} else if (b.capture == null) {
				this.capture = new Capture[a.result.length + b.result.length];
				System.arraycopy(a.capture, 0, capture, 0, a.result.length);
				Arrays.fill(capture, a.result.length, a.result.length + b.result.length, Capture.capture());
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
	
	protected Matcher(Node<S> node, Capture capture, MatchAction<? super I, ? extends R> action) {
		this.node = node;
		this.capture = capture == null ? null : new Capture[] { capture };
		this.result = new MatchAction[] { action };
	}

	public <R0> Matcher(Matcher<I, S, R0> copy, Function<? super MatchAction<? super I, ? extends R0>, ? extends MatchAction<? super I, ? extends R>> map, boolean keepCaptures) {
		this.result = new MatchAction[copy.result.length];
		for (int i = 0 ; i != result.length ; i++) {
			result[i] = map.apply(copy.result[i]);
		}
		this.capture = keepCaptures ? copy.capture : null;
		this.node = copy.node;
	}
	
	protected boolean matches(Iterator<? extends S> sequence) {
		return node.match(capture, sequence).length > 0;
	}
	
	protected List<R> match(I input, Iterator<? extends S> sequence) {
		final Found[] found = node.match(capture, sequence);
		if (found.length == 0) {
			return Collections.emptyList();
		}
		final R[] results = (R[]) new Object[found.length];
		for (int i = 0 ; i != results.length ; i++) {
			results[i] = result[found[i].id].matched(input, found[i]);
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
		applyToResults(input, sequence, new org.jjoost.util.Function<R, FindAction>() {
			private static final long serialVersionUID = 1L;
			@Override
			public FindAction apply(R v) {
				r.add(v);
				return FindAction.continueAll();
			}
		});
		return r;
	}
	
	protected boolean contains(I input, List<? extends S> sequence) {
		return !findFirstResults(input, sequence).isEmpty();
	}
	
	protected List<R> findFirstResults(I input, List<? extends S> sequence) {
		final FoundGroup found = node.findFirst(capture, sequence);
		if (found == null) {
			return Collections.emptyList();
		}
		final R[] results = (R[]) new Object[found.found.length];
		for (int i = 0 ; i != results.length ; i++) {
			results[i] = result[found.found[i].id].matched(input, found.found[i]);
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
	
	protected void applyToResults(final I input, List<? extends S> sequence, final Function<? super R, FindAction> f) {
		node.find(capture, sequence, new Function<FindState, FindAction>() {
			private static final long serialVersionUID = 1L;
			@Override
			public FindAction apply(FindState state) {
				final FoundGroup found = state.get();
				FindAction r = null;
				for (int i = 0 ; i != found.found.length ; i++) {
					r = f.apply(result[found.found[i].id].matched(input, found.found[i]));
					switch (r.type) {
					case TERMINATE:
					case SKIP_N_CHARS_NOW:
					case SKIP_MATCHED_CHARS_NOW:
						return r;
					}
				}				
				return r;
			}
		});
	}
	
	protected void applyToMatches(final I input, List<? extends S> sequence, final Function<? super Match<R>, FindAction> f) {
		node.find(capture, sequence, new Function<FindState, FindAction>() {
			private static final long serialVersionUID = 1L;
			@Override
			public FindAction apply(FindState state) {
				final FoundGroup found = state.get();
				FindAction r = FindAction.continueAll();
				for (int i = 0 ; i != found.found.length ; i++) {
					r = f.apply(new Match<R>(
							found.found[i].start, found.found[i].end, found.found[i], 
							result[found.found[i].id], 
							result[found.found[i].id].matched(input, found.found[i])));
					switch (r.type) {
					case TERMINATE:
					case SKIP_N_CHARS_NOW:
					case SKIP_MATCHED_CHARS_NOW:
						return r;
					}
				}
				return r;
			}
		});
	}
	
	protected void applyToLongestMatches(final I input, List<? extends S> sequence, final Function<? super Match<R>, FindAction> f) {
		final class MyFunc implements Function<FindState, FindAction> {
			private static final long serialVersionUID = 1L;
			FoundGroup last = null;
			
			@Override
			public FindAction apply(FindState state) {
				final FoundGroup found = state.get();
				FindAction r = FindAction.continueAll();
				if (last != null) {
					if (last.end < found.end) {
						if (last.start < found.start) {
							r = _apply(last);
						}
					} else {
						return r;
					}
				}
				last = found;
				return r;
			}
			private FindAction _apply(FoundGroup found) {
				FindAction r = FindAction.continueAll();				
				for (int i = 0 ; i != found.found.length ; i++) {
					r = f.apply(new Match<R>(
							found.start, found.end, found.found[i], 
							result[found.found[i].id], 
							result[found.found[i].id].matched(input, found.found[i])));
					switch (r.type) {
					case TERMINATE:
					case SKIP_N_CHARS_NOW:
					case SKIP_MATCHED_CHARS_NOW:
						return r;
					}
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
	
	protected void applyToMatchStates(final I input, List<? extends S> sequence, final Function<? super FindState, FindAction> f) {
		node.find(capture, sequence, f);
	}
	
}
