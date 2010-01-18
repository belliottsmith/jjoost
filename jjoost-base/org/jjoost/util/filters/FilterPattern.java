package org.jjoost.util.filters ;

import java.util.regex.Pattern ;

import org.jjoost.util.Filter ;

/**
 * A filter accepting strings that match the provided pattern
 * 
 * @author b.elliottsmith
 */
public class FilterPattern implements Filter<String> {

	private static final long serialVersionUID = 2312285149003933324L ;

	private final Pattern pattern ;

    /**
     * Constructs a new filter accepting strings that match the provided pattern
     * 
     * @param pattern the pattern to filter by
     */
	public FilterPattern(Pattern pattern) {
		this.pattern = pattern ;
	}

    /**
     * Constructs a new filter accepting strings that match the provided pattern
     * 
     * @param pattern the pattern to filter by
     */
	public FilterPattern(String pattern) {
		this.pattern = Pattern.compile(pattern) ;
	}

	public boolean accept(String test) {
		return pattern.matcher(test).matches() ;
	}

	public String toString() {
		return "matches \"" + pattern.toString() + "\"" ;
	}
	
    /**
     * Returns a filter accepting strings that match the provided pattern
     * 
     * @param pattern the pattern to filter by
     * @return a filter accepting strings that match the provided pattern
     */
	public static FilterPattern get(Pattern pattern) {
		return new FilterPattern(pattern) ;
	}

    /**
     * Returns a filter accepting strings that match the provided pattern
     * 
     * @param pattern the pattern to filter by
     * @return a filter accepting strings that match the provided pattern
     */
	public static FilterPattern get(String pattern) {
		return new FilterPattern(pattern) ;
	}
	
}
