package org.jjoost.util.concurrent.atomic;


public final class AtomicIntArrayUpdater {

	private final long offset;
	private final int shift;
	public AtomicIntArrayUpdater(Class<?> c) {
		if (c.getComponentType() != int.class) {
			throw new IllegalArgumentException("Array type is " + c.getComponentType());
		}
		offset = Unsafe.arrayBaseOffset(c);
		shift = Unsafe.arrayIndexShift(c);
	}

	public final boolean compareAndSet(Object trg, int i, int exp, int upd) {
		return Unsafe.compareAndSetInt(trg, offset + (i << shift), exp, upd);
	}

	public final void putVolatile(Object trg, int i, int val) {
		Unsafe.putIntVolatile(trg, offset + (i << shift), val);
	}
	
	public final int get(Object trg, int i) {
		return Unsafe.getInt(trg, offset + (i << shift));
	}
	
	public final int getVolatile(Object trg, int i) {
		return Unsafe.getIntVolatile(trg, offset + (i << shift));
	}

	public final int addAndGet(Object trg, int i, int val) {
		while (true) {
			final int cur = getVolatile(trg, i);
			if (compareAndSet(trg, i, cur, cur + val)) {
				return cur;
			}
		}
	}

	public final int decrementAndGet(Object trg, int i) {
		while (true) {
			final int cur = getVolatile(trg, i);
			if (compareAndSet(trg, i, cur, cur - 1)) {
				return cur;
			}
		}
	}
	
	public final int setAndGetDelta(Object trg, int i, int upd) {
		while (true) {
			final int cur = getVolatile(trg, i);
			if (compareAndSet(trg, i, cur, upd)) {
				return cur - upd;
			}
		}
	}
	
	public long incrementAndGet(Object trg, int i) {
		while (true) {
			final int cur = getVolatile(trg, i);
			if (compareAndSet(trg, i, cur, cur + 1)) {
				return cur;
			}
		}
	}
	

}
