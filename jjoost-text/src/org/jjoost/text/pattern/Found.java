package org.jjoost.text.pattern;

import java.util.Arrays;

public final class Found {

	private static final int[] EMPTY = new int[0];	
	
	public final int id;
	public final int start;
	public final int end;
	public final int[][] starts;
	public final int[][] ends;
	public final Capture defs;
	
	Found(int id, Capture defs, int start, int end, int[][] starts, int[][] ends, int[] lens) {
		this.id = id;
		this.start = start;
		this.end = end;
		if (defs == null) {
			this.defs = null;
			this.starts = null;
			this.ends = null;
		} else {
			this.defs = defs;
			this.starts = new int[starts.length][];
			this.ends = new int[ends.length][];
			for (int i = 0 ; i != starts.length ; i++) {
				if (starts[i] == null) {
					this.ends[i] = this.starts[i] = EMPTY;
				} else {
					this.starts[i] = Arrays.copyOf(starts[i], lens[i]);
					this.ends[i] = Arrays.copyOf(ends[i], lens[i]);
				}
			}
		}
	}
	
	public String toString() {
		return Integer.toString(id);
	}
	
	private int[] _starts(int group) {
		return starts[group];
	}

	private int[] _ends(int group) {
		return ends[group];
	}
	
	private int[] _firstMatch(int group) {
		if (starts == null || starts[group] == null || starts[group].length == 0) {
			return null;
		}
		return new int[] { starts[group][0], ends[group][0] };
	}
	
	public int[] starts(int ... group) {
		return _starts(defs.labelid(group));
	}
	
	public int[] ends(int ... group) {
		return _ends(defs.labelid(group));
	}
	
	public int matchCount(int ... group) {
		return starts[defs.labelid(group)].length;
	}
	
	public int[] firstMatch(int ... group) {
		return _firstMatch(defs.labelid(group));
	}
	
	public int[] starts(String group) {
		return _starts(defs.labelid(group));
	}
	
	public int[] ends(String group) {
		return _ends(defs.labelid(group));
	}
	
	public int[] firstMatch(String group) {
		return _firstMatch(defs.labelid(group));
	}
	
}
