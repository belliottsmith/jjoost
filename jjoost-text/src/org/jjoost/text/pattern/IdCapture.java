package org.jjoost.text.pattern;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public final class IdCapture implements Serializable {

	private static final long serialVersionUID = -411385644831835521L;
	
	public static final int END = Integer.MIN_VALUE;
	public static final int INCLUSIVE = Integer.MIN_VALUE >> 1;
	public static final int LABELMASK = (1 << 24) - 1;
	static final IdCapture EMPTY = new IdCapture(new int[0]);
	static final ConcurrentHashMap<IdCapture, IdCapture> CACHE = new ConcurrentHashMap<IdCapture, IdCapture>();
	
	// map from pattern id->capture id
	// same pattern id may occur multiple times, only adjacently
	final int[] capt;
	final int len;
	IdCapture(int[] ids) {
		this.capt = ids;
		this.len = ids.length;
	}
	IdCapture(int[] ids, int len) {
		this.capt = ids;
		this.len = len;
	}

	static final class Captures {
		
		private final int start;
		private final int end;
		private final int[] capt;
		
		private Captures(int start, int end, int[] capt) {
			this.start = start;
			this.end = end;
			this.capt = capt;
		}
		
		int count() {
			return (end - start) >>> 1;
		}
		
		int get(int i) {
			return capt[1 + ((start + i) << 1)];
		}
		
	}
	
	IdCapture inclusive() {
		final int[] capt = this.capt.clone();
		for (int i = 0 ; i!= capt.length ; i++) {
			if ((capt[i + 1] & END) == END) {
				capt[i + 1] |= INCLUSIVE;
			}
		}
		return cache(new IdCapture(capt, len));
	}
	IdCapture start(int capture) {
		return add(capture);
	}
	IdCapture end(int capture, boolean inclusive) {
		return add(capture | END | (inclusive ? INCLUSIVE : 0));
	}
	private IdCapture add(int key) {
		final int[] src = this.capt;
		final int[] trg = new int[len + 2];
		int pos = floor(src, key, len >> 1);
		if (pos >= 0) {
			if (src[pos] == key) {
				return this;
			}
			System.arraycopy(src, 0, trg, 0, pos += 2);
		} else {
			pos += 2;
		}
		trg[pos + 1] = key;
		System.arraycopy(src, pos, trg, pos + 2, len - pos);
		return new IdCapture(trg, len + 2);
		
	}
	public IdCapture append(IdCapture that, boolean startsOnly, int offset) {
		if (that.isEmpty()) {
			return this;
		}
		if (offset == 0) {
			if (!startsOnly && this.isEmpty()) {
				return that;
			}
			final int ac = this.len;
			final int bc = that.len;
			final int[] a = this.capt;
			final int[] b = that.capt;
			final int[] r = new int[ac + bc];
			int i = 0, j = 0, c = 0;
			while (i != ac && j != bc) {
				if (startsOnly && b[j + 1] < 0) {
					j += 2;
					continue;
				}
				final int av = idCmp(a[i + 1]);
				final int bv = idCmp(b[j + 1]);
				if (av == bv) {
					r[c + 1] = a[i + 1];
					i += 2;
					j += 2;
				} else if (av < bv) {
					r[c + 1] = a[i + 1];
					i += 2;
				} else {
					r[c + 1] = b[j + 1];
					j += 2;
				}
				c += 2;
			}
			while (i != ac) {
				r[c + 1] = a[i + 1];
				c += 2;
				i += 2;
			}
			while (j != bc) {
				if (startsOnly && b[j + 1] < 0) {
					j += 2;
					continue;
				}
				r[c + 1] = b[j + 1];
				c += 2;
				j += 2;
			}
			return cache(new IdCapture(r, c));
		} else {
			if (startsOnly) {
				throw new IllegalStateException();
			}
			final int ac = this.len;
			final int bc = that.len;
			final int rc = ac + bc;
			final int[] a = this.capt;
			final int[] b = that.capt;
			final int[] r = new int[rc];
			System.arraycopy(a, 0, r, 0, ac);
			for (int i = ac ; i != rc ; i += 2) {
				r[i] = b[i - ac] + offset;
				r[i + 1] = b[i + 1 - ac];
			}
			return cache(new IdCapture(r, rc));
		}
	}

	static final int idCmp(int id) {
		return ((id & LABELMASK) << 1) + ((id & END) != 0 ? 1 : 0);
	}
	
	public static int floor(int[] a, int id, int count) {

		int i = -1;
		int j = count;
		// a[-1] ^= -infinity
		final int cmp = idCmp(id);

		while (i < j - 1) {

			// { a[i] <= v ^ a[j] > v }

			final int m = (i + j) | 1;
			int v = idCmp(a[m]);

			if (v <= cmp) {
				i = m >> 1;
			} else {
				j = m >> 1;
			}

			// { a[m] > v => a[j] > v => a[i] <= v ^ a[j] > v }
			// { a[m] <= v => a[i] <= v => a[i] <= v ^ a[j] > v }

		}
		
		return i << 1;
	}

	public boolean isEmpty() {
		return capt.length == 0;
	}

	public static IdCapture empty() {
		return EMPTY;
	}
	
	public static IdCapture create(int ... capture) {
		final int[] capt = new int[capture.length << 1];
		for (int i = 0 ; i != capture.length ; i++) {
			capt[1 + (i << 1)] = capture[i];
		}
		return cache(new IdCapture(capt));
	}

	public String toString() {
		if (len == 0) {
			return "[]";
		}
		final StringBuilder b = new StringBuilder();
		b.append("[");
		for (int i = 0 ; i != len ; i +=2) {
			if (i > 0 && capt[i] == capt[i-2]) {
				b.append(",");
				b.append(idCmp(capt[i + 1]));
				if ((capt[i + 1] & END) == END) {
					b.append("*");
				}
			} else {
				if (i > 0) {
					b.append("), ");
				}
				b.append(capt[i]);
				b.append("=>(");
				b.append(idCmp(capt[i + 1]));
				if ((capt[i + 1] & END) == END) {
					b.append("*");
				}
			}
		}
		b.append(")]");
		return b.toString();
	}
	
	public int hashCode() {
		int h = 0;
		for (int i = 0 ; i != len ; i++) {
			h *= 31;
			h += capt[i];
		}
		return h;
	}
	
	public boolean equals(IdCapture that) {
		final int len = this.len;
		if (len != that.len) {
			return false;
		}
		final int[] capt1 = this.capt;
		final int[] capt2 = that.capt;
		for (int i = 0 ; i != len ; i++) {
			if (capt1[i] != capt2[i]) {
				return false;
			}
		}
		return true;
	}
	
	public boolean equals(Object that) {
		return that instanceof IdCapture && equals((IdCapture) that);
	}
	
	public IdCapture intern() {
		return cache(this);
	}
	
	static IdCapture cache(IdCapture set) {
		IdCapture alt = CACHE.get(set);
		if (alt == null && set.len != set.capt.length) {
			set = new IdCapture(Arrays.copyOf(set.capt, set.len));
			alt = CACHE.putIfAbsent(set, set);
		}
		if (alt != null) {
			return alt;
		}
		return set;
	}
	
	public IdCapture shift(int offset) {
		if (offset == 0 || len == 0) {
			return this;
		}
		final int[] ids = new int[len];
		for (int i = 0 ; i != len ; i += 2) {
			ids[i] = this.capt[i] + offset;
			ids[i + 1] = this.capt[i + 1];
		}
		return cache(new IdCapture(ids));
	}
	
	public boolean isSuperset(IdCapture that) {
		if (that.isEmpty()) {
			return true;
		}
		if (this.len < that.len) {
			return false;
		}
		final int ac = this.len;
		final int bc = that.len;
		final int[] a = this.capt;
		final int[] b = that.capt;
		int i = 0, j = 0;
		while (i != ac && j != bc) {
			int av = a[i];
			int bv = b[j];
			if (av == bv) {
				av = idCmp(a[i + 1]);
				bv = idCmp(b[j + 1]);
			}
			if (av == bv) {
				i += 2;
				j += 2;
			} else if (av < bv) {
				return false;
			} else {
				j += 2;
			}
		}
		return j == bc;
	}
	
}
