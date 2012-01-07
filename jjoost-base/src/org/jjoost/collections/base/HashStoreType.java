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

package org.jjoost.collections.base;

public class HashStoreType {

	public static enum Type {
		SERIAL , LINKED_SERIAL , SYNCHRONIZED , LINKED_SYNCHRONIZED , LOCK_FREE , LINKED_LOCK_FREE, HASH_LOCK;
	}
	
	private final Type type;
	public Type type() { return type ; }
	public HashStoreType(Type type) { this.type = type ; }

	public static HashStoreType serial() { return SERIAL ; }
	public static HashStoreType blocking() { return BLOCKING ; }
	public static HashStoreType lockFree() { return LOCK_FREE ; }
	public static HashStoreType hashLock() { return HASH_LOCK ; }
	public static HashStoreType linkedSerial() { return LINKED_SERIAL ; }
	public static HashStoreType linkedBlocking() { return LINKED_BLOCKING ; }
	public static HashStoreType linkedLockFree() { return LINKED_LOCK_FREE ; }
	
	private static final HashStoreType SERIAL = new HashStoreType(Type.SERIAL);
	private static final HashStoreType BLOCKING = new HashStoreType(Type.SYNCHRONIZED);
	private static final HashStoreType LOCK_FREE = new HashStoreType(Type.LOCK_FREE);
	private static final HashStoreType HASH_LOCK = new HashStoreType(Type.HASH_LOCK);
	
	private static final HashStoreType LINKED_SERIAL = new HashStoreType(Type.SERIAL);
	private static final HashStoreType LINKED_BLOCKING = new HashStoreType(Type.SYNCHRONIZED);
	private static final HashStoreType LINKED_LOCK_FREE = new HashStoreType(Type.LOCK_FREE);
	
}
