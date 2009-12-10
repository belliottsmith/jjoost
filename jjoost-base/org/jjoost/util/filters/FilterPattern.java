package org.jjoost.util.filters ;

import java.util.regex.Pattern ;

import org.jjoost.util.Filter ;

public class FilterPattern implements Filter<String> {

	private static final long serialVersionUID = 2312285149003933324L ;

	private final Pattern pattern ;

	public FilterPattern(Pattern pattern) {
		this.pattern = pattern ;
	}

	public FilterPattern(String pattern) {
		this.pattern = Pattern.compile(pattern) ;
	}

	public boolean accept(String test) {
		return pattern.matcher(test).matches() ;
	}

	public String toString() {
		return "matches \"" + pattern.toString() + "\"" ;
	}

}
