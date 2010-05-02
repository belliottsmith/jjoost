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

package org.jjoost.util;

/**
 * A simple interface encapsulating some dynamic integer greater than or equal to zero
 * 
 * @author b.elliottsmith
 */
public interface Counter {
	
	/**
	 * returns true if adding the provided integer does not reduce the value to below zero; if true
	 * then the value was added to the <code>Counter</code>
	 * 
	 * @param i to add
	 * @return true, if successful
	 */
	public boolean add(int i) ;
	
	/**
	 * Gets the current value of the <code>Counter</code>
	 * @return current value
	 */
	public int get() ;
	
	/**
	 * returns a new <code>Counter</code> of the same type as this one, with a value of zero
	 * @return new <code>Counter</code>
	 */
	public Counter newInstance() ;
	
}
