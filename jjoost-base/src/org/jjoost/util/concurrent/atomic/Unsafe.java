package org.jjoost.util.concurrent.atomic;

import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;

public class Unsafe {

    private static final sun.misc.Unsafe UNSAFE = getUnsafe();

    public static long arrayBaseOffset(Class<?> c) {
    	return UNSAFE.arrayBaseOffset(c);
    }
    
	public static Field field(Class<?> c, Class<?> expectedType, String field) {
		try {			
			return c.getDeclaredField(field);
		} catch (NoSuchFieldException e) {
			throw new UndeclaredThrowableException(e);
		} catch (SecurityException e) {
			throw new UndeclaredThrowableException(e);
		}
	}
	
    public static int arrayIndexShift(Class<?> c) {
    	final int i = UNSAFE.arrayIndexScale(c);
        if ((i & (i-1)) != 0)
            throw new Error("data type scale not a power of two");
        return 31 - Integer.numberOfLeadingZeros(i);
    }
    
    public static long objectFieldOffset(Field f) {
    	return UNSAFE.objectFieldOffset(f);
    }
    
    public static boolean compareAndSetObject(Object obj, long fieldOffset, Object expected, Object update) {
    	return UNSAFE.compareAndSwapObject(obj, fieldOffset, expected, update);
    }
    
    public static boolean compareAndSetInt(Object obj, long fieldOffset, int expected, int update) {
    	return UNSAFE.compareAndSwapInt(obj, fieldOffset, expected, update);
    }
    
    public static boolean compareAndSetLong(Object obj, long fieldOffset, long expected, long update) {
    	return UNSAFE.compareAndSwapLong(obj, fieldOffset, expected, update);
    }
    
    public static void putObjectVolatile(Object obj, long fieldOffset, Object update) {
    	UNSAFE.putObjectVolatile(obj, fieldOffset, update);
    }
    
    public static void putIntVolatile(Object obj, long fieldOffset, int update) {
    	UNSAFE.putIntVolatile(obj, fieldOffset, update);
    }
    
    public static void putLongVolatile(Object obj, long fieldOffset, long update) {
    	UNSAFE.putLongVolatile(obj, fieldOffset, update);
    }
    
    public static Object getObjectVolatile(Object obj, long fieldOffset) {
    	return UNSAFE.getObjectVolatile(obj, fieldOffset);
    }
    
    public static Object getObject(Object obj, long fieldOffset) {
    	return UNSAFE.getObject(obj, fieldOffset);
    }
    
    public static int getIntVolatile(Object obj, long fieldOffset) {
    	return UNSAFE.getIntVolatile(obj, fieldOffset);
    }
    
    public static int getInt(Object obj, long fieldOffset) {
    	return UNSAFE.getInt(obj, fieldOffset);
    }
    
    public static long getLongVolatile(Object obj, long fieldOffset) {
    	return UNSAFE.getLongVolatile(obj, fieldOffset);
    }
    
    public static long getLong(Object obj, long fieldOffset) {
    	return UNSAFE.getLong(obj, fieldOffset);
    }
    
    /**
     * Returns a sun.misc.Unsafe.  Suitable for use in a 3rd party package.
     * Replace with a simple call to Unsafe.getUnsafe when integrating
     * into a jdk.
     *
     * @return a sun.misc.Unsafe
     */
    static sun.misc.Unsafe getUnsafe() {
        try {
            return sun.misc.Unsafe.getUnsafe();
        } catch (SecurityException se) {
            try {
                return java.security.AccessController.doPrivileged
                    (new java.security
                     .PrivilegedExceptionAction<sun.misc.Unsafe>() {
                        public sun.misc.Unsafe run() throws Exception {
                            java.lang.reflect.Field f = sun.misc
                                .Unsafe.class.getDeclaredField("theUnsafe");
                            f.setAccessible(true);
                            return (sun.misc.Unsafe) f.get(null);
                        }});
            } catch (java.security.PrivilegedActionException e) {
                throw new RuntimeException("Could not initialize intrinsics",
                                           e.getCause());
            }
        }
    }


}
