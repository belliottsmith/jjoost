package org.jjoost.text.pattern;

import java.util.Arrays;

final class Capturing {

	private int resetCounter = 0;
	private final int[] resetCounters;
	private final int[][] lens;
	private final int[][][] starts;
	private final int[][][] ends;
	private final Capture[] defs;
	
	Capturing(Capture[] defs) {
		if (defs == null) {
			starts = null;
			ends = null;
			lens = null;
			this.resetCounters = null;
			this.defs = null;
		} else {
			starts = new int[defs.length][][];
			ends = new int[defs.length][][];
			lens = new int[defs.length][];
			resetCounters = new int[defs.length];
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
			final int[] lens = this.lens[id];
			int[][] starts = this.starts[id];
			int[][] ends = this.ends[id];
			if (resetCounters[id] != resetCounter) {
				for (int j = 0 ; j != lens.length ; j++) {
					lens[j] = 0;
				}
				resetCounters[id] = resetCounter;
			}
			if ((label & IdCapture.END) == IdCapture.END) {
				int len = lens[labelid];
				int[] s = starts[labelid];
				if (s != null) {
					if (s.length != len && s[len] != -1) {
						ends[labelid][lens[labelid]++] = pos + ((label & IdCapture.INCLUSIVE) == IdCapture.INCLUSIVE ? 1 : 0);
					} else if (len > 0) {
						ends[labelid][len - 1] = pos + ((label & IdCapture.INCLUSIVE) == IdCapture.INCLUSIVE ? 1 : 0);
					}
				}
			} else {
				int len = lens[labelid];
				if (starts[labelid] == null || starts[labelid].length == 0) {
					starts[labelid] = new int[] { -1, -1};					
					ends[labelid] = new int[2];
				}
				if (len == starts[labelid].length) {
					starts[labelid] = Arrays.copyOf(starts[labelid], len << 1);
					ends[labelid] = Arrays.copyOf(ends[labelid], len << 1);
					Arrays.fill(starts[labelid], len, len << 1, -1);
				}
				if (starts[labelid][len] < 0) {
					starts[labelid][len] = pos;
				}
			}
		}
	}

	void update(IdSet capture, int start, int end) {
		if (lens == null) {
			return;
		}
		for (int i = 0 ; i != capture.len ; i += 1) {
			int id = capture.ids[i];
			final int[] lens = this.lens[id];
			int[][] starts = this.starts[id];
			int[][] ends = this.ends[id];
			if (resetCounters[id] != resetCounter) {
				for (int j = 0 ; j != lens.length ; j++) {
					lens[j] = 0;
				}
				resetCounters[id] = resetCounter;
			}
			final Capture def = defs[id];
			if (def != null && def.capture.length > 0 && def.capture[0].length == 1 && def.capture[0][0] == 0) {
				if (starts[0] == null) {
					starts[0] = new int[1];
					ends[0] = new int[1];
				}
				starts[0][0] = start;
				ends[0][0] = end;
			}
		}
	}
	
	Captured[] select(IdSet select, int pos) {
		final Captured[] r = new Captured[select.len];
		for (int i = 0 ; i != r.length ; i++) {
			if (lens == null) {
				r[i] = new Captured(select.ids[i], null, null, null, null);
			} else {
				final int id = select.ids[i];				
				final int[] lens = this.lens[id];
				int[][] starts = this.starts[id];
				int[][] ends = this.ends[id];
				if (resetCounters[id] != resetCounter) {
					for (int j = 0 ; j != lens.length ; j++) {
						lens[j] = 0;
					}
					resetCounters[id] = resetCounter;
				}
				for (int j = 0 ; j != lens.length ; j++) {
					int l = lens[j];
					if (starts[j] != null && l < starts[j].length && starts[j][l] >= 0) {
						ends[j][l] = pos;
						lens[j] = l + 1;
					}
				}
				r[i] = new Captured(id, defs[id], starts, ends, lens);
			}
		}
		return r;
	}
	
	public void reset() {
		resetCounter++;
	}
	
}
