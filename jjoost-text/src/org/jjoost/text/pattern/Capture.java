package org.jjoost.text.pattern;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class Capture implements Serializable {

	private static final long serialVersionUID = -8178461145283468010L;

	private static final Comparator<int[]> CMP = new Comparator<int[]>() {
		@Override
		public int compare(int[] a, int[] b) {
			for (int i = 0 ; i != a.length && i != b.length ; i++) {
				if (a[i] < b[i]) {
					return -1;
				} else if (a[i] > b[i]) {
					return 1;
				}
			}
			return a.length - b.length;
		}
	};

	// two dimensional because labels are vectors, not scalars
	final int[][] capture;
	final int[] pos;
	
	int count() {
		return capture.length;
	}
	
	int labelid(int[] arr, int len) {
		// want the larger array (i.e. the one passed in) to be sorted to directly after any possible matches
		int i = Arrays.binarySearch(capture, arr, CMP);
		if (i > 0 && arr.length != len) {
			throw new IllegalStateException();
		} else if (i < 0) {
			i = -2 - i;
		}
		while (i >= 0) {
			final int[] test = capture[i];
			final int testlen = test.length;
			if (testlen == len) {
				for (int j = 0 ; j != len ; j++) {
					if (test[j] != arr[j]) {
						return -1;
					}
				}
				return i;
			} else if (testlen < len) {
				return -1;
			} else {
				i -= 1;
			}
		}
		return -1;
	}
	
	public int labelid(int[] group) {
		return labelid(group, group.length);
	}
	
	public int labelid(String group) {
		return labelid(parse(group));
	}
	
	public int labelid(int declarepos) {
		return pos[declarepos];
	}
	
	public Capture(int[][] capture) {
		if (capture.length == 0) {
			this.capture = capture;
			this.pos = new int[0];
		} else {
			int[][] capt = capture.clone();
			Arrays.sort(capt, CMP);
			int len = capt.length;
			int uniq = 1;
			for (int i = 1 ; i < len ; i++) {
				if (Arrays.equals(capt[i], capt[i - 1])) {
					// remove i
				} else if (uniq != i) {
					capt[uniq++] = capt[i++];
				} else {
					uniq++;
				}
			}
			if (uniq != len) {
				capt = Arrays.copyOf(capt, uniq);
			}
			this.capture = capt;
			this.pos = new int[capture.length];
			for (int i = 0 ; i != len ; i++) {
				this.pos[i] = Arrays.binarySearch(capt, capture[i], CMP);
			}
		}
	}
	
	public Capture(List<String> capture) {
		this(parse(capture));
	}
	
	public Capture(String ... capture) {
		this(parse(capture));
	}
	
	public static int[][] parse(List<String> capture) {
		final int[][] r = new int[capture.size()][];
		for (int i = 0 ; i != capture.size() ; i++) {
			r[i] = parse(capture.get(i));
		}
		return r;
	}
	
	public static int[][] parse(String[] capture) {
		final int[][] r = new int[capture.length][];
		for (int i = 0 ; i != capture.length ; i++) {
			r[i] = parse(capture[i]);
		}
		return r;
	}
	
	public static int[] parse(String capture) {
		final String[] pos = capture.split(",");
		final int[] r = new int[pos.length];
		for (int j = 0 ; j != pos.length ; j++) {
			r[j] = Integer.parseInt(pos[j].trim());
		}
		return r;
	}
	
	public boolean equals(Object that) {
		return that instanceof Capture && equals((Capture) that);
	}
	
	public boolean equals(Capture that) {
		return java.util.Arrays.equals(this.pos, that.pos) && java.util.Arrays.deepEquals(this.capture, that.capture);
	}
	
}
