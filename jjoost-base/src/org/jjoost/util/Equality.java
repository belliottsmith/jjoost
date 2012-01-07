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

import java.io.Serializable;

/**
 * This interface declares a method which defines equality been objects of type <code>E</code>
 * 
 * @author b.elliottsmith
 */
public interface Equality<E> extends Serializable {
	
	/**
	 * Returns a <code>boolean</code> indicating if the two parameters are considered equal by this <code>Equality</code>
	 * 
	 * @param a
	 *            an <code>Object</code> of type <code>E</code>
	 * @param b
	 *            another <code>Object</code> of type <code>E</code>
	 * @return <code>true</code> if this equality equates the two arguments
	 */
    public boolean equates(E a, E b);
    
    /**
	 * Returns the hash value of the object as defined by this <code>Equality</code>. For all objects this <code>Equality</code> can be
	 * applied to, it should be the case that <code>equates(a, b)</code> ==> <code>hash(a) == hash(b)</code>
	 * 
	 * @param o
	 *            an <code>Object</code> of type E
	 * @return the hash of the parameter
	 */
    public int hash(E o);

}
