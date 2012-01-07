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

package org.jjoost.collections;

/**
 * <p> This interface declares that a set supports precisely one occurence of each value
 * 
 * <p>Note that an <code>Iterator</code> returned by concurrent implementors of this class is permitted
 * to return values more times than they actually ever occurred <b>if a valid sequence of deletes and inserts happens</b>
 * to cause the <code>Iterator</code> to see the values multiple times. See the javadoc of the implementing classes
 * to determine their behaviour in this case.
 * 
 * @author b.elliottsmith
 */
public interface Set<V> extends AnySet<V>, ReadSet<V> {

	/**
	 * Insert the parameter into the set, removing and returning any value equal to the parameter that was already present, as determined by
	 * the set's definition of equality.
	 * 
	 * @param value
	 *            value to insert
	 * @return value that was evicted from the set as a result of the action
	 */
	@Override public V put(V value);
	
	/**
	 * Returns a copy of the set. This method may or may not return a set of the same class as the one it was
	 * called on, however return a <code>Set</code>
	 */
	@Override public Set<V> copy();
	
	/**
	 * Returns <code>this</code>
	 * 
	 * @return <code>this</code>
	 */
	public Set<V> unique();

}
