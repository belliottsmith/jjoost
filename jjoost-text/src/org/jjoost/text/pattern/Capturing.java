package org.jjoost.text.pattern;

import java.util.Arrays;

final class Capturing {

	private final int[][] lens;
	private final int[][][] starts;
	private final int[][][] ends;
	private final Capture[] defs;
	
	Capturing(Capture[] defs, int[][] lens, int[][][] starts, int[][][] ends) {
		this.defs = defs;
		this.starts = starts;
		this.ends = ends;
		this.lens = lens;
	}
	Capturing(Capture[] defs) {
		if (defs == null) {
			starts = null;
			ends = null;
			lens = null;
			this.defs = null;
		} else {
			starts = new int[defs.length][][];
			ends = new int[defs.length][][];
			lens = new int[defs.length][];
			for (int i = 0 ; i != defs.length ; i++) {
				lens[i] = new int[defs[i].capture.length];
				starts[i] = new int[defs[i].capture.length][];
				ends[i] = new int[defs[i].capture.length][];
			}
			this.defs = defs;
		}
	}
	
	void update(IdCapture capture, int pos) {
		if (lens == null) {
			return;
		}
		for (int i = 0 ; i != capture.len ; i += 2) {
			int id = capture.capt[i];
			int label = capture.capt[i + 1];
			int labelid = label & IdCapture.LABELMASK;
			if ((label & IdCapture.END) == IdCapture.END) {
				int len = lens[id][labelid];
				int[] s = starts[id][labelid];
				if (s != null) {
					if (s.length != len && s[len] != -1) {
						ends[id][labelid][lens[id][labelid]++] = pos + ((label & IdCapture.INCLUSIVE) == IdCapture.INCLUSIVE ? 1 : 0);
					} else if (len > 0) {
						ends[id][labelid][len - 1] = pos + ((label & IdCapture.INCLUSIVE) == IdCapture.INCLUSIVE ? 1 : 0);
					}
				}
			} else {
				int len = lens[id][labelid];
				if (starts[id][labelid] == null || starts[id][labelid].length == 0) {
					starts[id][labelid] = new int[] { -1, -1} ;
					ends[id][labelid] = new int[2];
				}
				if (len == starts[id][labelid].length) {
					starts[id][labelid] = Arrays.copyOf(starts[id][labelid], len << 1);
					ends[id][labelid] = Arrays.copyOf(ends[id][labelid], len << 1);
					Arrays.fill(starts[id][labelid], len, len << 1, -1);
				}
				if (starts[id][labelid][len] < 0) {
					starts[id][labelid][len] = pos;
				}
			}
		}
	}

	Captured[] select(IdSet select, int pos) {
		final Captured[] r = new Captured[select.len];
		for (int i = 0 ; i != r.length ; i++) {
			if (lens == null) {
				r[i] = new Captured(select.ids[i], null, null, null, null);
			} else {
				final int o = select.ids[i];
				for (int j = 0 ; j != lens[o].length ; j++) {
					int l = lens[o][j];
					if (starts[o][j] != null && l < starts[o][j].length && starts[o][j][l] >= 0) {
						ends[o][j][l] = pos;
						lens[o][j] = l + 1;
					}
				}
				r[i] = new Captured(o, defs[o], starts[o], ends[o], lens[o]);
			}
		}
		return r;
	}
	
	public Capturing copy() {
		final int[][] lens = new int[this.lens.length][];
		for (int i = 0 ; i != lens.length ; i++) {
			lens[i] = this.lens[i].clone();
		}
		final int[][][] starts = copy(this.starts, lens);
		final int[][][] ends = copy(this.ends, lens);
		return new Capturing(defs, lens, starts, ends);
	}
	
	private static int[][][] copy(int[][][] src1, int[][] lens1) {
		final int[][][] trg1 = new int[src1.length][][];
		for (int i = 0 ; i != src1.length ; i++) {
			final int[][] src2 = src1[i];
			final int[][] trg2 = trg1[i] = new int[src2.length][];
			final int[] lens2 = lens1[i];
			for (int j = 0 ; j != src2.length ; j++) {
				trg2[j] = Arrays.copyOf(src2[j], lens2[j]);
			}
		}
		return trg1;
	}

	public void reset() {
		if (defs != null) {
			for (int i = 0 ; i!= lens.length ; i++) {
				final int[] lens = this.lens[i];
				for (int j = 0 ; j != lens.length ; j++) {
					lens[j] = 0;
				}
			}
		}
	}
	
}
