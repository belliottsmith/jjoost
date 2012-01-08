package org.jjoost.util.concurrent.atomic;


public final class AtomicRefArrayUpdater {

	private final long offset;
	private final int shift;
	public AtomicRefArrayUpdater(Class<?> c) {
		if (c.getComponentType() == null) {
			throw new IllegalArgumentException("Not an array class");
		} else if (c.getComponentType().isPrimitive()) {
			throw new IllegalArgumentException("Array is primitive");
		}
		offset = Unsafe.arrayBaseOffset(c);
		shift = Unsafe.arrayIndexShift(c);
	}

	public final boolean compareAndSet(Object trg, int i, Object exp, Object upd) {
		return Unsafe.compareAndSetObject(trg, offset + (i << shift), exp, upd);
	}

	public final void putVolatile(Object trg, int i, Object val) {
		Unsafe.putObjectVolatile(trg, offset + (i << shift), val);
	}
	
	public final Object get(Object trg, int i) {
		return Unsafe.getObject(trg, offset + (i << shift));
	}
	
	public final Object getVolatile(Object trg, int i) {
		return Unsafe.getObjectVolatile(trg, offset + (i << shift));
	}
	
}
