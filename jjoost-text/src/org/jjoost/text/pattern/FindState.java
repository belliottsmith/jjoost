package org.jjoost.text.pattern;

public interface FindState {

	public FoundGroup get();
	public int start();
	public int end();
	public IdSet found();
	
}
