package org.jjoost.text.pattern;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.jjoost.util.order.IntOrder;

public final class IdSet implements Serializable {

	private static final long serialVersionUID = 8712752376088567363L;
	static final IdSet EMPTY = new IdSet(new int[0]);
	static final IdSet UNITARY = new IdSet(new int[] { 0 });
	static final ConcurrentHashMap<IdSet, IdSet> CACHE = new ConcurrentHashMap<IdSet, IdSet>();
	
	public final int[] ids;
	final int len;
	IdSet(int[] ids) {
		this.ids = ids;
		this.len = ids.length;
	}
	IdSet(int[] ids, int len) {
		this.ids = ids;
		this.len = len;
	}

	IdSet intersect(IdSet that) {
		if (this == that) {
			return this;
		} else if (this.isEmpty() || that.isEmpty()) {
			return EMPTY;
		}		
		final int ac = this.len;
		final int bc = that.len;
		final int[] a = this.ids;
		final int[] b = that.ids;		
		final int[] r = IntOrder.intersect(a, 0, ac, b, 0, bc);
		if (r == a) {
			return this;
		} else if (r == b) {
			return that;
		}
		return new IdSet(r, r.length);
	}
	
	IdSet append(IdSet that, int offset) {
		if (that.isEmpty()) {
			return this;
		} 
		if (offset == 0) {
			if (this.isEmpty()) {
				return that;
			}
			final int ac = this.len;
			final int bc = that.len;
			final int[] a = this.ids;
			final int[] b = that.ids;
			final int[] r = new int[ac + bc];
			int i = 0, j = 0, c = 0;
			while (i != ac && j != bc) {
				if (a[i] == b[j]) {
					r[c++] = a[i];
					i++;
					j++;
				} else if (a[i] < b[j]) {
					r[c++] = a[i++];
				} else {
					r[c++] = b[j++];
				}
			}
			while (i != ac) {
				r[c++] = a[i++];
			}
			while (j != bc) {
				r[c++] = b[j++];
			}
			return cache(new IdSet(r, c));
		} else {
			final int ac = this.len;
			final int bc = that.len;
			final int rc = ac + bc;
			final int[] a = this.ids;
			final int[] b = that.ids;
			final int[] r = new int[rc];
			System.arraycopy(a, 0, r, 0, ac);
			for (int i = ac ; i != rc ; i++) {
				r[i] = b[i - ac] + offset;
			}
			return cache(new IdSet(r));
		}
	}
	
	boolean isEmpty() {
		return len == 0;
	}

	public static IdSet empty() {
		return EMPTY;
	}
	
	public static IdSet unitary() {
		return UNITARY;
	}
	
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("[");
		for (int i = 0 ; i != len ; i++) {
			if (i > 0) {
				b.append(",");
			}
			b.append(ids[i]);
		}
		b.append("]");
		return b.toString();
	}
	
	public int hashCode() {
		int h = 0;
		for (int i = 0 ; i != len ; i++) {
			h *= 31;
			h += ids[i];
		}
		return h;
	}
	
	public boolean equals(IdSet that) {
		if (this.len != that.len) {
			return false;
		}
		for (int i = 0 ; i != this.len ; i++) {
			if (this.ids[i] != that.ids[i]) {
				return false;
			}
		}
		return true;
	}
		
	public boolean equals(Object that) {
		return that instanceof IdSet && equals((IdSet) that);
	}
	
	public IdSet intern() {
		return cache(this);
	}
	
	static IdSet cache(IdSet set) {
		IdSet alt = CACHE.get(set);
		if (alt == null) {
			if (set.len != set.ids.length) {
				set = new IdSet(Arrays.copyOf(set.ids, set.len));
			}
			alt = CACHE.putIfAbsent(set, set);
		}
		if (alt != null) {
			return alt;
		}
		return set;
	}
	public IdSet shift(int offset) {
		if (offset == 0 || len == 0) {
			return this;
		}
		final int[] ids = new int[len];
		for (int i = 0 ; i != len ; i++) {
			ids[i] = this.ids[i] + offset;
		}
		return cache(new IdSet(ids));
	}
	
	// TODO : can optimise much like with intersect
	public IdSet subtract(IdSet that, int offset) {
		int ai = 0, bi = 0, ri = 0;
		final int[] a = this.ids;
		final int[] b = that.ids;
		final int[] r = new int[this.ids.length];
		while (ai != a.length && bi != b.length) {
			final int bv = b[bi] + offset;
			if (a[ai] < bv) {
				r[ri++] = a[ai++];
			} else if (a[ai] > bv) {
				bi++;
			} else {
				ai++;
				bi++;
			}
		}
		while (ai != a.length) {
			r[ri++] = a[ai++];
		}
		return cache(new IdSet(r, ri));
	}
	
	// TODO : can optimise much like with intersect
	public boolean isSuperset(IdSet that) {
		int ai = 0, bi = 0;
		final int[] a = this.ids;
		final int[] b = that.ids;
		while (ai != a.length && bi != b.length) {
			final int bv = b[bi];			
			final int av = a[ai];			
			if (av < bv) {
				ai++;
			} else if (av > bv) {
				return false;
			} else {
				ai++;
				bi++;
			}
		}
		return bi == b.length;
	}
	
	public IdSet firstOnly() {
		if (len < 1) {
			throw new IllegalStateException("The set is empty - cannot take the first item");
		}
		return cache(new IdSet(ids, 1));
	}
	
	public int size() {
		return len;
	}
	
	public IdSet remap(int[] idmap) {
		final int[] r = new int[len];
		for (int i = 0 ; i != len ; i++) {
			r[i] = idmap[ids[i]];
		}
		return cache(new IdSet(r, len));
	}
	
}
