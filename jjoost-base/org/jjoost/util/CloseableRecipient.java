package org.jjoost.util;

import java.io.Closeable;

public interface CloseableRecipient<V> extends Recipient<V>, Closeable {

}
