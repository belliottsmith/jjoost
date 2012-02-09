package org.jjoost.text.pattern;

import java.util.Arrays;

public final class Captured {

	public final int id;
	public final int[][] starts;
	public final int[][] ends;
	public final Capture defs;
	
	Captured(int id, Capture defs, int[][] starts, int[][] ends, int[] lens) {
		this.id = id;
		if (defs == null) {
			this.defs = null;
			this.starts = null;		
			this.ends = null;
		} else {
			this.defs = defs;
			this.starts = starts;		
			this.ends = ends;
			for (int i = 0 ; i != starts.length ; i++) {
				if (starts[i] == null) {
					ends[i] = starts[i] = new int[0];
				} else if (lens[i] != starts[i].length) {
					starts[i] = Arrays.copyOf(starts[i], lens[i]);
					ends[i] = Arrays.copyOf(ends[i], lens[i]);
				}
			}
		}
	}
	
	public String toString() {
		return Integer.toString(id);
	}

}
