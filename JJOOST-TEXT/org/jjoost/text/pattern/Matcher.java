package org.jjoost.text.pattern;

import java.io.Serializable;
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
	final MatchAction<I, ? extends R>[] result;
	
	protected Matcher(Matcher<I, S, ? extends R> a, Matcher<I, S, ? extends R> b) {
		if (Arrays.equals(a.capture, b.capture) && Arrays.equals(a.result, b.result)) {
			this.node = a.node.mergeAlternatePatternWithSameCaptureAndResult(b.node);
			this.capture = a.capture;
			this.result = a.result;			
		} else {
			this.node = a.node.mergeAlternatePattern(b.node, true);
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
	
	protected Matcher(Node<S> node, Capture capture, MatchAction<I, R> action) {
		this.node = node;
		this.capture = capture == null ? null : new Capture[] { capture };
		this.result = new MatchAction[] { action };
	}

	protected List<R> match(I input, Iterator<? extends S> sequence) {
		final Captured[] captured = node.capture(capture, sequence);
		if (captured.length == 0) {
			return Collections.emptyList();
		}
		final R[] results = (R[]) new Object[captured.length];
		for (int i = 0 ; i != results.length ; i++) {
			results[i] = result[captured[i].id].matched(input, captured[i]);
		}
		return Arrays.asList(results);
	}
	
	protected List<R> first(I input, List<? extends S> sequence) {
		final Found found = node.find(capture, sequence);
		if (found == null) {
			return Collections.emptyList();
		}
		final R[] results = (R[]) new Object[found.captured.length];
		for (int i = 0 ; i != results.length ; i++) {
			results[i] = result[found.captured[i].id].matched(input, found.captured[i]);
		}
		return Arrays.asList(results);
	}
	
	protected void find(final I input, final Function<? super R, ?> f, List<? extends S> sequence) {
		node.find(capture, sequence, new Function<Found, Object>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Object apply(Found found) {
				for (int i = 0 ; i != found.captured.length ; i++) {
					f.apply(result[found.captured[i].id].matched(input, found.captured[i]));
				}				
				return null;
			}
		});
	}
	
}
