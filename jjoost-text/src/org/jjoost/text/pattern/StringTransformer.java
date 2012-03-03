package org.jjoost.text.pattern;

import org.jjoost.text.pattern.Parse.ParseException;

public class StringTransformer<E> extends StringMatcher<String, E> {

	private static final long serialVersionUID = 655802573811276962L;

	public StringTransformer(String regexp, Capture capture, TransformFirst<E> transform) throws ParseException {
		super(regexp, capture, new DoTransformFirst<E>(transform));
	}
	
	public StringTransformer(String regexp, Capture capture, TransformAll<E> transform) throws ParseException {
		super(regexp, capture, new DoTransformAll<E>(transform));
	}
	
	private StringTransformer(StringTransformer<E> a, StringTransformer<E> b, int truncateRecursionDepth) {
		super(a, b, truncateRecursionDepth);
	}
	
	public StringTransformer<E> merge(StringTransformer<E> that) {
		return merge(that, Integer.MAX_VALUE);
	}
	
	public StringTransformer<E> merge(StringTransformer<E> that, int truncateRecursionDepth) {
		return new StringTransformer<E>(this, that, truncateRecursionDepth);
	}

	public static interface TransformAll<E> {
		E transform(String[][] captured);
	}
	
	public static interface TransformFirst<E> {
		E transform(String[] captured);
	}
	
	private static final class DoTransformAll<E> implements MatchAction<String, E> {

		private static final long serialVersionUID = -4495752181906436869L;
		final TransformAll<E> transform;
		
		public DoTransformAll(TransformAll<E> transform) {
			this.transform = transform;
		}

		@Override
		public E matched(String input, Found captured) {
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
			return transform.transform(r);
		}
		
	}
	
	private static final class DoTransformFirst<E> implements MatchAction<String, E> {
		
		private static final long serialVersionUID = -4495752181906436869L;
		final TransformFirst<E> transform;
		
		public DoTransformFirst(TransformFirst<E> transform) {
			this.transform = transform;
		}
		
		@Override
		public E matched(String input, Found captured) {
			final String[] r = new String[captured.ends.length];
			for (int i = 0 ; i != r.length ; i++) {
				final int[] starts = captured.starts[i];
				final int[] ends = captured.ends[i];
				if (starts == null || starts.length == 0) {
					continue;
				}
				r[i] = input.substring(starts[0], ends[0]);
			}
			return transform.transform(r);
		}
		
	}
	
}
