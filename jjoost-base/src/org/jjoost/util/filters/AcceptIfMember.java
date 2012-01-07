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

import java.util.List;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.SetMaker;
import org.jjoost.util.Filter;

/**
 * A filter accepting values that are members of the provided set
 * 
 * @author b.elliottsmith
 */
public class AcceptIfMember<E> implements Filter<E> {

    /**
     * Returns a filter accepting values that are members of the provided set
     * @param members set of values to accept
     * @return a filter accepting values that are members of the provided set
     */
	public static <E> AcceptIfMember<E> get(List<E> members) {
		return new AcceptIfMember<E>(members);
	}

    /**
     * Returns a filter accepting values that are members of the provided set
     * @param members set of values to accept
     * @return a filter accepting values that are members of the provided set
     */
	public static <E> AcceptIfMember<E> get(Iterable<E> members) {
		return new AcceptIfMember<E>(members);
	}

    /**
     * Returns a filter accepting values that are members of the provided set
     * @param members set of values to accept
     * @return a filter accepting values that are members of the provided set
     */
	public static <E> AcceptIfMember<E> get(E... members) {
		return new AcceptIfMember<E>(members);
	}

    /**
     * Returns a filter accepting values that are members of the provided set
     * @param members set of values to accept
     * @return a filter accepting values that are members of the provided set
     */
	public static <E> AcceptIfMember<E> get(AnySet<E> members) {
		return new AcceptIfMember<E>(members);
	}

	private static final long serialVersionUID = 8506853231172669315L;
	private final AnySet<E> members;

    /**
     * Constructs a new filter accepting values that are members of the provided set
     * @param members set of values to accept
     */
	public AcceptIfMember(AnySet<E> members) {
		this.members = members;
	}

    /**
     * Constructs a new filter accepting values that are members of the provided set
     * @param members set of values to accept
     */
	public AcceptIfMember(E... members) {
		this.members = SetMaker.<E> hash().newSet();
		for (E member : members)
			this.members.put(member);
	}

    /**
     * Constructs a new filter accepting values that are members of the provided set
     * @param members set of values to accept
     */
	public AcceptIfMember(Iterable<E> members) {
		this.members = SetMaker.<E> hash().newSet();
		for (E member : members)
			this.members.put(member);
	}

	public boolean accept(E test) {
		return members.contains(test);
	}

	public String toString() {
		return "is member of " + members.toString();
	}

}
