//package org.jjoost.text.pattern;
//
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.Map;
//
//import org.jjoost.text.pattern.NodeMapSplit.NodeIntersect;
//
//public abstract class SeqMap<S, Seq extends Iterable<S>> implements NodeMap<Seq> {
//
//	private static final long serialVersionUID = 4549583502460020313L;
//
//	public static class PatGroup<S, Seq extends Iterable<S>> implements Group<Seq> {
//		private static final long serialVersionUID = -4474931342859398521L;
//		public final Node<S> node;
//		public PatGroup(Node<S> node) {
//			this.node = node;
//		}
//		public String toString() {
//			return node.toOneLineString();
//		}
//	}
//
//	protected final Node<S> lookup;
//	protected final Node<Seq>[] xref;
//	protected final IdCapture[] capture;
//
//	protected SeqMap(Node<S> lookup, Node<Seq>[] xref, IdCapture[] capture) {
//		if (xref.length != lookup.routes().len) {
//			throw new IllegalStateException();
//		}
//		this.lookup = lookup;
//		this.xref = xref;
//		this.capture = capture;		
//	}
//
//	public abstract NodeScheme<Seq> scheme();
//
//	protected abstract NodeScheme<S> base();
//
//	protected abstract SeqMap<S, Seq> empty();
//
//	protected abstract SeqMap<S, Seq> create(Node<S> lookup, Node<Seq>[] xref,
//			IdCapture[] capture);
//
//	public SeqMap<S, Seq> merge(SeqMap<S, Seq> that) {
//		@SuppressWarnings("unchecked")
//		final Node<Seq>[] xref = new Node[this.xref.length + that.xref.length];
//		final IdCapture[] capt = new IdCapture[xref.length];
//		final int c = this.xref.length;
//		System.arraycopy(this.xref, 0, xref, 0, c);
//		System.arraycopy(this.capture, 0, capt, 0, c);
//		Node<S> lookup = this.lookup;
//		System.arraycopy(that.xref, 0, xref, c, that.xref.length);
//		System.arraycopy(that.capture, 0, capt, c, that.xref.length);
//		if (this.lookup == null) {
//			lookup = that.lookup;
//		} else if (that.lookup == null) {
//			lookup = this.lookup;
//		} else {
//			lookup = this.lookup.mergeAlternatePattern(that.lookup, false);
//		}
//		return create(lookup, xref, capt);
//	}
//
//	public Node<Seq> lookup(Seq s) {
//		final Captured[] c = lookup.capture(null, s.iterator());
//		if (c.length > 1) {
//			throw new IllegalStateException();
//		} else if (c.length == 1) {
//			return xref[c[0].id];
//		} else {
//			return null;
//		}
//	}
//
//	@Override
//	public IdCapture capture(Seq s) {
//		final Captured[] c = lookup.capture(null, s.iterator());
//		if (c.length > 1) {
//			throw new IllegalStateException();
//		} else if (c.length == 1) {
//			return capture[c[0].id];
//		} else {
//			return null;
//		}
//	}
//
//	@Override
//	public boolean maps(Node<Seq> s) {
//		for (Node<Seq> n : xref) {
//			if (n == s) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public NodeMapSplit<Seq> split(NodeMap<Seq> that1, int offset) {
//		if (that1 instanceof SeqMap) {
//			final SeqMap<S, Seq> that = (SeqMap<S, Seq>) that1;
//			if (this.lookup == null || that.lookup == null) {
//				return new NodeMapSplit<Seq>(this, that,
//						new NodeIntersect[0]);
//			}
//			final Node<S> collisions = lookup.acceptanceCollision(that.lookup);
//			if (collisions == null) {
//				return new NodeMapSplit<Seq>(this, that,
//						new NodeIntersect[0]);
//			} else {
//				// TODO: remove no longer referenced values from xref
//				final Node<S>[] separate = collisions.separate(2);
//				final NodeIntersect<Seq>[] intersect = new NodeIntersect[separate.length];
//				for (int i = 0; i != separate.length; i++) {
//					intersect[i] = new NodeIntersect<Seq>(new PatGroup<S, Seq>(
//							separate[i].resetPaths()),
//							this.xref[separate[i].routes().ids[0]],
//							this.capture[separate[i].routes().ids[0]],
//							that.xref[separate[i].routes().ids[1]
//									- this.xref.length],
//							that.capture[separate[i].routes().ids[1]
//									- this.xref.length].shift(offset));
//				}
//				return new NodeMapSplit<Seq>(disjoint(collisions,
//						this.lookup, this.xref, this.capture, 0), disjoint(
//						collisions, that.lookup, that.xref, that.capture,
//						-this.xref.length), intersect);
//			}
//		} else {
//			throw new IllegalArgumentException();
//		}
//	}
//
//	SeqMap<S, Seq> disjoint(Node<S> collisions, Node<S> lookup, Node<Seq>[] xref,
//			IdCapture[] capture, int offset) {
//		final Node<S> newLookup = lookup.subtract(collisions, offset);
//		final IdSet oldRoutes = newLookup.routes();
//		if (oldRoutes.len == 0) {
//			return empty();
//		}
//		@SuppressWarnings("unchecked")
//		final Node<Seq>[] newXref = (Node<Seq>[]) new Node[oldRoutes.len];
//		final IdCapture[] newCapture = new IdCapture[oldRoutes.len];
//		for (int i = 0; i != oldRoutes.len; i++) {
//			newXref[i] = xref[oldRoutes.ids[i]];
//			newCapture[i] = capture[oldRoutes.ids[i]];
//		}
//		return create(newLookup.compactPaths(), newXref, newCapture);
//	}
//
//	@Override
//	public Iterator<Node<Seq>> iterator() {
//		return new Iterator<Node<Seq>>() {
//			int p = 0;
//			final Node<Seq>[] m = xref;
//
//			@Override
//			public boolean hasNext() {
//				return p < m.length;
//			}
//
//			@Override
//			public Node<Seq> next() {
//				return m[p++];
//			}
//
//			@Override
//			public void remove() {
//				throw new UnsupportedOperationException();
//			}
//		};
//	}
//
//	@Override
//	public Iterable<NodeMapEntry<Seq>> entries() {
//		if (lookup == null) {
//			return Collections.<NodeMapEntry<Seq>> emptyList();
//		}
//		final Node<S>[] separate = lookup.separate(1);
//		if (separate.length != xref.length) {
//			throw new IllegalStateException();
//		}
//		return new Iterable<NodeMapEntry<Seq>>() {
//			@Override
//			public Iterator<NodeMapEntry<Seq>> iterator() {
//				return new Iterator<NodeMapEntry<Seq>>() {
//					int p = 0;
//					final Node<Seq>[] l = xref;
//					final IdCapture[] c = capture;
//
//					@Override
//					public boolean hasNext() {
//						return p < separate.length;
//					}
//
//					@Override
//					public NodeMapEntry<Seq> next() {
//						final PatGroup<S, Seq> key = new PatGroup<S, Seq>(separate[p]);
//						final Node<Seq> value = l[separate[p].routes().ids[0]];
//						final IdCapture capt = c[separate[p].routes().ids[0]];
//						p++;
//						return new NodeMapEntry<Seq>() {
//							private static final long serialVersionUID = -8294375391653925308L;
//							@Override
//							public PatGroup<S, Seq> sym() {
//								return key;
//							}
//
//							@Override
//							public Node<Seq> next() {
//								return value;
//							}
//
//							@Override
//							public IdCapture capture() {
//								return capt;
//							}
//						};
//					}
//
//					@Override
//					public void remove() {
//						throw new UnsupportedOperationException();
//					}
//				};
//			}
//		};
//	}
//
//	@Override
//	public NodeMap<Seq> replace(Map<Node<Seq>, Node<Seq>> replace, int offset) {
//		if (xref == null) {
//			return this;
//		}
//		final Node<Seq>[] ms = xref.clone();
//		for (int i = 0; i != ms.length; i++) {
//			final Node<Seq> m = ms[i];
//			final Node<Seq> repl = replace.get(m);
//			if (repl != null) {
//				ms[i] = repl;
//			}
//		}
//		return create(lookup, ms, shift(capture, offset));
//	}
//
//	@Override
//	public boolean isEmpty() {
//		return xref.length == 0;
//	}
//
//	@Override
//	public void replaceInSitu(Node<Seq> replace, Node<Seq> with) {
//		for (int i = 0; i != xref.length; i++) {
//			if (xref[i] == replace) {
//				xref[i] = with;
//			}
//		}
//	}
//
//	@Override
//	public NodeMap<Seq> addCapture(IdCapture capture) {
//		final IdCapture[] c = new IdCapture[this.capture.length];
//		for (int i = 0; i != c.length; i++) {
//			c[i] = this.capture[i].append(capture, false, 0);
//		}
//		return create(lookup, xref.clone(), c);
//	}
//
//	private static IdCapture[] shift(IdCapture[] in, int offset) {
//		if (offset == 0) {
//			return in;
//		}
//		final IdCapture[] out = new IdCapture[in.length];
//		for (int i = 0; i != in.length; i++) {
//			out[i] = in[i].shift(offset);
//		}
//		return out;
//	}
//
//}
