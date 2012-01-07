/**
 * Copyright (c) 2010 Benedict Elliott Smith
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jjoost.util.filters;

import java.util.regex.Pattern;

import org.jjoost.util.Filter;

/**
 * A filter accepting strings that match the provided pattern
 * 
 * @author b.elliottsmith
 */
public class FilterPattern implements Filter<String> {

	private static final long serialVersionUID = 2312285149003933324L;

	private final Pattern pattern;

    /**
     * Constructs a new filter accepting strings that match the provided pattern
     * 
     * @param pattern the pattern to filter by
     */
	public FilterPattern(Pattern pattern) {
		this.pattern = pattern;
	}

    /**
     * Constructs a new filter accepting strings that match the provided pattern
     * 
     * @param pattern the pattern to filter by
     */
	public FilterPattern(String pattern) {
		this.pattern = Pattern.compile(pattern);
	}

	public boolean accept(String test) {
		return pattern.matcher(test).matches();
	}

	public String toString() {
		return "matches \"" + pattern.toString() + "\"";
	}
	
    /**
     * Returns a filter accepting strings that match the provided pattern
     * 
     * @param pattern the pattern to filter by
     * @return a filter accepting strings that match the provided pattern
     */
	public static FilterPattern get(Pattern pattern) {
		return new FilterPattern(pattern);
	}

    /**
     * Returns a filter accepting strings that match the provided pattern
     * 
     * @param pattern the pattern to filter by
     * @return a filter accepting strings that match the provided pattern
     */
	public static FilterPattern get(String pattern) {
		return new FilterPattern(pattern);
	}
	
}
