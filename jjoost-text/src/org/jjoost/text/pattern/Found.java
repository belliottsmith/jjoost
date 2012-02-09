package org.jjoost.text.pattern;

public class Found {

	public final int offset;
	public final Captured[] captured;
	
	Found(int offset, Captured[] captured) {
		super();
		this.offset = offset;
		this.captured = captured;
	}
	
}
