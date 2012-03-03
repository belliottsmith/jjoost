package org.jjoost.text.pattern;

public final class FoundGroup {

	public final int start;
	public final int end;
	public final Found[] found;
	
	FoundGroup(int start, int end, Found[] captured) {
		super();
		this.start = start;;
		this.end = end;
		this.found = captured;
	}
	
	public final int start() {
		return start;
	}
	
	public final int end() {
		return end;
	}
	
}
