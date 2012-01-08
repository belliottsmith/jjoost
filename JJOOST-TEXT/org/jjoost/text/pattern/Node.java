package org.jjoost.text.pattern;

import java.io.Serializable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jjoost.text.pattern.NodeMapSplit.NodeIntersect;
import org.jjoost.util.Function;

// TODO : capturing is far from 100% working with infinite loops and their modifications
// TODO : tail with self merges result in possibility of same capture group being visited multiple times
@SuppressWarnings("unchecked")
public final class Node<S> implements Serializable {

	private static final long serialVersionUID = 6329951467575507627L;
	
	// conceptually immutable, but mutated in order to simplify construction
	private NodeMap<S> next;
	private boolean inLoop;
	private IdSet routes;
	private IdSet accepts;
	
	public boolean isEmpty() {
		return next.isEmpty();
	}
	
	public IdSet routes() {
		return routes;
	}
	
	public IdSet accepts() {
		return accepts;
	}
	
	public Node(NodeMap<S> paths) {
		this(paths, IdSet.empty(), IdSet.unitary(), false);
	}
	
	private Node() { }
	Node(NodeMap<S> paths, IdSet accepting, IdSet routes, boolean inLoop) {
		this.next = paths;
		this.inLoop = inLoop;
		this.routes = routes;
		this.accepts = accepting;
	}
	
	public Node<S> capture(IdCapture capture) {
		if (capture.isEmpty() || next.isEmpty()) {
			return this;
		}
		return new Node<S>(next.addCapture(capture), accepts, routes, inLoop);
	}
	
	final MergeResult<S> mergeAlternatePath(Node<S> that, boolean mergeCapture) {
		return mergeAlternatePath(this, that, mergeCapture, new IdentityStackMap<Node<S>, Node<S>>(), new IdentityStackMap<Node<S>, Node<S>>());
	}
	
	private static <S> MergeResult<S> mergeAlternatePath(Node<S> a, Node<S> b, boolean mergeCapture, IdentityStackMap<Node<S>, Node<S>> leftReplace, IdentityStackMap<Node<S>, Node<S>> rightReplace) {
		if (a == b) {
			return new MergeResult<S>(a, b, a, true, true);
		} else if (a.next.isEmpty() && b.accepts.isSuperset(a.accepts)) {
			return new MergeResult<S>(a, b, b, b.next.isEmpty() && a.accepts.isSuperset(b.accepts), true);
		} else if (b.next.isEmpty() && a.accepts.isSuperset(b.accepts)) {
			return new MergeResult<S>(a, b, a, true, a.next.isEmpty() && b.accepts.isSuperset(a.accepts));
		} else {
			Node<S> r = new Node<S>();
			r.routes = a.routes;
			r.accepts = a.accepts.isEmpty() ? b.accepts : a.accepts;
			final NodeMapSplit<S> split = a.next.split(b.next, 0);
			final NodeIntersect<S>[] intersect = split.intersect;
			NodeMap<S> left = split.left;
			NodeMap<S> right = split.right;
			boolean sameAsLeft = right.isEmpty() && a.accepts.equals(r.accepts);
			boolean sameAsRight = left.isEmpty() && b.accepts.equals(r.accepts);
			if (intersect.length == 0) {
				r.next = a.next.scheme().merge(replace(left, leftReplace), replace(right, rightReplace));
			} else {
				final Node<S> match = leftReplace.commonValue(a, rightReplace, b);
				if (match != null) {
					return new MergeResult<S>(a, b, match, leftReplace.isSingletonStack(a), rightReplace.isSingletonStack(b));
				}
				final Node<S> ea = leftReplace.peek(a);
				final Node<S> eb = rightReplace.peek(b);
				if (a.inLoop && b.inLoop) {
					r.inLoop = true;
					leftReplace.push(a, r);
					if (ea != null) {
						a = a.ensureAccepts(r.accepts).unrollLoop();
					}
					rightReplace.push(b, r);
					if (eb != null) {
						b = b.ensureAccepts(r.accepts).unrollLoop();
					}
				} else if (a.inLoop) {
					a = a.ensureAccepts(r.accepts).unrollLoop();				
				} else if (b.inLoop) {
					b = b.ensureAccepts(r.accepts).unrollLoop();
				}
				final NodeMapBuilder<S> builder = a.next.scheme().build();
				for (final NodeIntersect<S> e : intersect) {
					final MergeResult<S> mr = mergeAlternatePath(e.left, e.right, mergeCapture, leftReplace, rightReplace);
					sameAsLeft &= mr.sameAsLeftInput;
					sameAsRight &= mr.sameAsRightInput;
					builder.bind(e.symbols, mr.result, mergeCapture ? e.leftCapture.append(e.rightCapture, false, 0) : e.leftCapture);
					if (!mergeCapture) {
						sameAsRight &= e.rightCapture.isSuperset(e.leftCapture);
					} else {
						sameAsLeft &= e.leftCapture.isSuperset(e.rightCapture);
						sameAsRight &= e.rightCapture.isSuperset(e.leftCapture);
					}
				}
				if (a.inLoop && b.inLoop) {
					rightReplace.pop(b);
					leftReplace.pop(a);
				}
				r.next = a.next.scheme().merge(replace(left, leftReplace), replace(right, rightReplace), builder.done());
			}
			return new MergeResult<S>(a, b, r, sameAsLeft, sameAsRight);
		}
	}
	
	public static final class MergeResult<S> {
		final Node<S> left;
		final Node<S> right;
		final Node<S> result;
		final boolean sameAsLeftInput;
		final boolean sameAsRightInput;
		MergeResult(Node<S> left, Node<S> right, Node<S> result, boolean sameAsLeftInput,
				boolean sameAsRightInput) {
			this.left = left;
			this.right = right;
			this.result = result;
			this.sameAsLeftInput = sameAsLeftInput;
			this.sameAsRightInput = sameAsRightInput;
		}
		Node<S> get() {
			if (sameAsLeftInput) {
				return left;
			} else if (sameAsRightInput) {
				return right;
			} else {
				return result;
			}
		}
	}
	
	
	private static <S> MergeResult<S> mergeTailWithSelf(Node<S> tail1, Node<S> tail2, IdentityStackMap<Node<S>, TailTailVal<S>> loop1, IdentityStackMap<Node<S>, TailTailVal<S>> loop2) {
		if (!tail1.inLoop && !tail2.inLoop) {
			return mergeAlternatePath(tail1, tail2, false, new IdentityStackMap<Node<S>, Node<S>>(), new IdentityStackMap<Node<S>, Node<S>>());
		} else {
			final TailTailVal<S> l1 = loop1.peek(tail1);
			final TailTailVal<S> l2 = loop2.peek(tail2);
			if (l1 != null && l2 != null) {
				if (l1.replacingWith == l2.replacingWith) {
					return new MergeResult<S>(tail1, tail2, l1.replacingWith, loop1.isSingletonStack(tail1), loop2.isSingletonStack(tail2));
				} else if (l1.mergingWith == tail2) {
					return new MergeResult<S>(tail1, tail2, l1.replacingWith, loop1.isSingletonStack(tail1), false);
				} else if (l2.mergingWith == tail1) {
					return new MergeResult<S>(tail1, tail2, l2.replacingWith, false, loop2.isSingletonStack(tail1));
				}
			} 
			final Node<S> tail1Orig = tail1;
			final Node<S> tail2Orig = tail2;			
//			if (l1 != null) {
//				tail1 = mergeTailWithSelf(tail1, l1, new IdentityStackMap<Node<S>, Node<S>>(), new IdentityStackMap<Node<S>, Node<S>>()).get();
//			} 
//			if (l2 != null) {
//				tail2 = mergeTailWithSelf(tail2, l2, new IdentityStackMap<Node<S>, Node<S>>(), new IdentityStackMap<Node<S>, Node<S>>()).get();
//			}
			Node<S> r = new Node<S>();
			loop1.push(tail1Orig, new TailTailVal<S>(tail2Orig, r));
			loop2.push(tail2Orig, new TailTailVal<S>(tail1Orig, r));
			r.inLoop = true;
			r.routes = tail1.routes.append(tail2.routes, 0);
			r.accepts = tail1.accepts.append(tail2.accepts, 0);
			final NodeMapSplit<S> split = tail1.next.split(tail2.next, 0);
			final NodeIntersect<S>[] intersect = split.intersect;
			NodeMap<S> left = split.left;
			NodeMap<S> right = split.right;
			boolean sameAsLeft = right.isEmpty() && tail1.accepts.equals(r.accepts);
			boolean sameAsRight = left.isEmpty() && tail2.accepts.equals(r.accepts);
			if (intersect.length == 0) {
				r.next = tail1.next.scheme().merge(left, right);
			} else {
				final NodeMapBuilder<S> builder = tail1.next.scheme().build();
				for (final NodeIntersect<S> e : intersect) {
					MergeResult<S> c = mergeTailWithSelf(e.left, e.right, loop1, loop2);
					sameAsLeft &= c.sameAsLeftInput;
					sameAsRight &= c.sameAsRightInput;
					builder.bind(e.symbols, c.result, e.leftCapture.append(e.rightCapture, false, 0));
				}
				r.next = tail1.next.scheme().merge(left, right, builder.done());
			}
			loop2.pop(tail2Orig);
			loop1.pop(tail1Orig);
			return new MergeResult<S>(tail1Orig, tail2Orig, r, sameAsLeft, sameAsRight);
		}
	}
	
	private static final class TailTailVal<S> {
		final Node<S> mergingWith;
		final Node<S> replacingWith;
		private TailTailVal(Node<S> mergingWith, Node<S> replacingWith) {
			super();
			this.mergingWith = mergingWith;
			this.replacingWith = replacingWith;
		}
	} 
	
	private static final class LoopTailKey<S> {
		final Node<S> loop;
		final Node<S> tail;
		private LoopTailKey(Node<S> loop, Node<S> tail) {
			super();
			this.loop = loop;
			this.tail = tail;
		}
		public int hashCode() {
			return System.identityHashCode(loop) + System.identityHashCode(tail.hashCode());
		}
		public boolean equals(Object that) {
			return that instanceof LoopTailKey<?> && equals((LoopTailKey<?>) that);
		}
		public boolean equals(LoopTailKey<?> that) {
			return this.loop == that.loop && this.tail == that.tail;
		}
	} 
	
	final Node<S> mergeLoopWithTail(Node<S> tail) {
		return mergeLoopWithTail(this, tail, new IdentityHashMap<Node<S>, Node<S>>(8), new IdentityHashMap<Node<S>, Node<S>>(8), new IdentityHashMap<Node<S>, Node<S>>(8), new HashMap<LoopTailKey<S>, Node<S>>(8));
	}
	
	private static <S> Node<S> mergeLoopWithTail(Node<S> loop, Node<S> tail, Map<Node<S>, Node<S>> loopRepl, Map<Node<S>, Node<S>> tailRepl, Map<Node<S>, Node<S>> tailForLoop, Map<LoopTailKey<S>, Node<S>> pairRepl) {
		if (loop == tail) {
			return loop;
		}
		final Node<S> tailKey = tail;
		final LoopTailKey<S> key = new LoopTailKey<S>(loop, tail);
		Node<S> r = pairRepl.get(key);
		if (r != null) {
			if (r.accepts.isSuperset(tail.accepts)) {
				return r;
			}
			pairRepl = new HashMap<LoopTailKey<S>, Node<S>>(pairRepl);
		} else if ((r = loopRepl.get(loop)) != null) {
			Node<S> prevTail = tailForLoop.get(loop);
			if (prevTail != null) {
				final MergeResult<S> mergeTail = mergeTailWithSelf(tail, prevTail, new IdentityStackMap<Node<S>, TailTailVal<S>>(), new IdentityStackMap<Node<S>, TailTailVal<S>>());
				if (mergeTail.sameAsRightInput) {
					return r;
				} else {
					tail = mergeTail.get();
				}
			}
		}
		r = new Node<S>();
		pairRepl.put(key, r);
		if (loopRepl.containsKey(loop)) {
			loopRepl = new IdentityHashMap<Node<S>, Node<S>>(loopRepl);
			tailForLoop = new IdentityHashMap<Node<S>, Node<S>>(tailForLoop);
		}
		loopRepl.put(loop, r);
		tailForLoop.put(loop, tail);
		if (tailKey.inLoop) {
			if (tailRepl.containsKey(tailKey)) {
				tailRepl = new IdentityHashMap<Node<S>, Node<S>>(tailRepl);
			}
			tailRepl.put(tailKey, r);
		}
		tail = tail.unrollLoop();
		r.accepts = tail.accepts;
		r.inLoop = true;
		r.routes = loop.routes;
		final NodeMapSplit<S> split = tail.next.split(loop.next, 0);
		final NodeIntersect<S>[] intersect = split.intersect;
		NodeMap<S> left = split.left;
		NodeMap<S> right = split.right;
		if (intersect.length == 0) {				
			r.next = loop.next.scheme().merge(left, replace(right, loopRepl));
		} else {
			final NodeMapBuilder<S> builder = loop.next.scheme().build();
			for (final NodeIntersect<S> e : intersect) {
				builder.bind(e.symbols, mergeLoopWithTail(e.right, e.left, loopRepl, tailRepl, tailForLoop, pairRepl), e.rightCapture);
			}
			r.next = loop.next.scheme().merge(left, replace(right, loopRepl), builder.done());
		}
		return r;
	}

	// essentially ensures the loop never overlaps its tail - effectively subtracts the start of the loop from the start of the tail
	final Node<S> mergeExclusiveExtendingLoopWithTail(Node<S> tail) {
		return mergeExclusiveExtendingLoopWithTail(this, tail, new IdentityHashMap<Node<S>, Node<S>>(8));
	}
	
	private static <S> Node<S> mergeExclusiveExtendingLoopWithTail(Node<S> loop, Node<S> tail, Map<Node<S>, Node<S>> remaps) {
		Node<S> r = new Node<S>();		
		r.accepts = tail.accepts;
		r.inLoop = true;
		r.routes = loop.routes;
		tail = tail.unrollLoop();
		remaps.put(loop, r);
		remaps.put(tail, r);
		final NodeMapSplit<S> split = loop.next.split(tail.next, 0);
		final NodeIntersect<S>[] intersect = split.intersect;
		NodeMap<S> left = split.left;
		NodeMap<S> right = split.right;
		if (intersect.length == 0) {				
			r.next = loop.next.scheme().merge(replace(left, remaps), replace(right, remaps));
		} else {
			final NodeMapBuilder<S> builder = loop.next.scheme().build();
			for (final NodeIntersect<S> e : intersect) {
				builder.bind(e.symbols, e.left.replace(remaps), e.leftCapture.append(e.rightCapture, false, 0));
			}
			r.next = loop.next.scheme().merge(replace(left, remaps), replace(right, remaps), builder.done());
		}
		return r;
	}
	
	// essentially ensures the loop never overlaps its tail - effectively subtracts the start of the tail from the start of the loop
	final Node<S> mergeExclusiveLimitingLoopWithTail(Node<S> tail) {
		return mergeExclusiveLimitingLoopWithTail(this, tail, new IdentityHashMap<Node<S>, Node<S>>(8));
	}
	
	private static <S> Node<S> mergeExclusiveLimitingLoopWithTail(Node<S> loop, Node<S> tail, Map<Node<S>, Node<S>> remaps) {
		Node<S> r = new Node<S>();		
		r.accepts = tail.accepts;
		r.inLoop = true;
		r.routes = loop.routes;
		tail = tail.unrollLoop();
		remaps.put(loop, r);
		remaps.put(tail, r);
		final NodeMapSplit<S> split = tail.next.split(loop.next, 0);
		final NodeIntersect<S>[] intersect = split.intersect;
		NodeMap<S> left = split.left;
		NodeMap<S> right = split.right;
		if (intersect.length == 0) {				
			r.next = loop.next.scheme().merge(replace(left, remaps), replace(right, remaps));
		} else {
			final NodeMapBuilder<S> builder = loop.next.scheme().build();
			for (final NodeIntersect<S> e : intersect) {
				builder.bind(e.symbols, e.left.replace(remaps), e.leftCapture.append(e.rightCapture, false, 0));
			}
			r.next = loop.next.scheme().merge(replace(left, remaps), replace(right, remaps), builder.done());
		}
		return r;
	}
	
	public Node<S> mergeAlternatePatternWithSameCaptureAndResult(Node<S> that) {
		return mergeAlternatePattern(this, that, true, 0, new IdentityHashMap<Node<S>, Node<S>>(8), new IdentityHashMap<Node<S>, Node<S>>(8));
	}
	public Node<S> mergeAlternatePattern(Node<S> that, boolean permitOverlappingAccepts) {
		return mergeAlternatePattern(this, that, permitOverlappingAccepts, this.routes.len, new IdentityHashMap<Node<S>, Node<S>>(8), new IdentityHashMap<Node<S>, Node<S>>(8));
	}
	
	private static <S> Node<S> mergeAlternatePattern(Node<S> a, Node<S> b, boolean permitOverlappingAccepts, int offset, Map<Node<S>, Node<S>> leftRemaps, Map<Node<S>, Node<S>> rightRemaps) {
		if (leftRemaps.containsKey(a) || rightRemaps.containsKey(b)) {
			// TODO : is this logic definitely correct? hacked it in quickly, but not convinced
			final Node<S> r = leftRemaps.get(a);
			final Node<S> r2 = rightRemaps.get(b);
			if (r != r2) {
				if (r != null) {
					leftRemaps = new IdentityHashMap<Node<S>, Node<S>>(leftRemaps);
				} 
				if (r2 != null) {
					rightRemaps = new IdentityHashMap<Node<S>, Node<S>>(rightRemaps);
				}
			} else {
				return r;
			}
		}
		final Node<S> r = new Node<S>();
		r.inLoop = a.inLoop | b.inLoop;
		r.accepts = a.accepts.append(b.accepts, offset);
		if (!permitOverlappingAccepts && r.accepts.size() > 1) {
			throw new IllegalStateException("The provided patterns have overlapping acceptance states, which has been forbidden");
		}
		r.routes = a.routes.append(b.routes, offset);
		final boolean aRefersToSelf = a.next.maps(a);
		final boolean bRefersToSelf = b.next.maps(b);
		if (aRefersToSelf != bRefersToSelf) {
			if (aRefersToSelf) {
				rightRemaps.put(b, r);
			} else {
				leftRemaps.put(a, r);
			}
		} else {
			leftRemaps.put(a, r);
			rightRemaps.put(b, r);
		}
		final NodeMapSplit<S> split = a.next.split(b.next, offset);
		final NodeIntersect<S>[] intersections = split.intersect;
		NodeMap<S> left = split.left;
		NodeMap<S> right = split.right;
		if (intersections.length == 0) {				
			r.next = a.next.scheme().merge(replace(left, leftRemaps), replace(right, rightRemaps, offset));
		} else {
			final NodeMapBuilder<S> builder = a.next.scheme().build();
			for (final NodeIntersect<S> e : intersections) {
				builder.bind(e.symbols, mergeAlternatePattern(e.left, e.right, permitOverlappingAccepts, offset, leftRemaps, rightRemaps), e.leftCapture.append(e.rightCapture, false, 0));
			}
			r.next = a.next.scheme().merge(replace(left, leftRemaps), replace(right, rightRemaps, offset), builder.done());
		}
		return r;
	}
	
	private Node<S> ensureAccepts(IdSet accepts) {
		if (this.accepts.isSuperset(accepts)) {
			return this;
		} else {
			final Map<Node<S>, Node<S>> remaps = new IdentityHashMap<Node<S>, Node<S>>();
			Node<S> self = new Node<S>();
			self.inLoop = inLoop;
			self.accepts = this.accepts.append(accepts, 0);
			self.routes = routes;
			remaps.put(this, self);
			self.next = replace(next, remaps);
			return replace(remaps);
		}
	}
	Node<S> unrollLoop() {
		if (!inLoop) {
			return this;
		}
		return unroll(this, new IdentityHashMap<Node<S>, Boolean>(8));
	}
	
	private static final <S> Node<S> unroll(Node<S> a, Map<Node<S>, Boolean> unrolled) {
		if (a.inLoop && null == unrolled.put(a, Boolean.TRUE)) {
			final Map<Node<S>, Node<S>> replace = new IdentityHashMap<Node<S>, Node<S>>(8);
			for (Node<S> next : a.next) {
				if (next.inLoop) {
					replace.put(next, unroll(next, unrolled));
				}
			}
			return new Node<S>(a.next.replace(replace, 0), a.accepts, a.routes, false);
		} else {
			return a;
		}
	}
	
	public Captured[] capture(Capture[] capture, Iterator<? extends S> iter) {
		return capture(capture, this, iter);
	}
	
	private static <S> Captured[] capture(Capture[] capture, Node<S> cur, Iterator<? extends S> iter) {
		final Capturing capturing = new Capturing(cur.routes, capture);
		int i = 0;
		IdSet patterns = cur.routes;
		while (true) {
			if (!patterns.isEmpty() && iter.hasNext()) {
				final S sym = iter.next();
				final Node<S> next = cur.next.lookup(sym);
				if (next == null) {
					return new Captured[0];
				}
				capturing.update(cur.next.capture(sym), i);
				cur = next;
				patterns = patterns.intersect(cur.routes);
				i++;
			} else {
				patterns = patterns.intersect(cur.accepts);
				return capturing.select(patterns, i);
			}
		}
	}
	
	public void find(Capture[] capture, List<? extends S> list, Function<? super Found, ?> found) {
		find(capture, this, list, found);
	}
	
	// TODO : optimise to avoid resetting completely after each match attempt
	// TODO : a pattern should never match a region of input multiple times?? not sure if technically cheap enough to implement
	private static <S> void find(Capture[] capture, Node<S> head, List<? extends S> list, Function<? super Found, ?> found) {
		int o = 0;
		final Capturing capturing = new Capturing(head.routes, capture);
		while (o != list.size()) {
			capturing.reset();
			Node<S> cur = head; 
			int i = o++;
			IdSet patterns = cur.routes;
			while (true) {
				final IdSet accept = patterns.intersect(cur.accepts);
				if (!accept.isEmpty()) {
					found.apply(new Found(o - 1, capturing.select(accept, i)));
				}
				if (!patterns.isEmpty() && i < list.size()) {
					final Node<S> next = cur.next.lookup(list.get(i));
					if (next == null) {
						break;
					}
					capturing.update(cur.next.capture(list.get(i)), i);
					cur = next;
					patterns = i - o <= 1 ? cur.routes : patterns.intersect(cur.routes);
					i++;
				} else {
					break;
				}
			}
		}
	}
	
	public Found find(Capture[] capture, List<? extends S> list) {
		return find(capture, this, list);
	}
	
	private static <S> Found find(Capture[] capture, Node<S> head, List<? extends S> list) {
		int o = 0;
		Found r = null;		
		while (o != list.size()) {
			Node<S> cur = head; 
			final Capturing capturing = new Capturing(cur.routes, capture);
			int i = o++;
			IdSet patterns = cur.routes;
			while (true) {
				final IdSet accept = patterns.intersect(cur.accepts);
				if (!accept.isEmpty()) {
					r = new Found(o - 1, capturing.select(accept, i));
				}
				if (!patterns.isEmpty() && i < list.size()) {
					final Node<S> next = cur.next.lookup(list.get(i));
					if (next == null) {
						break;
					}
					capturing.update(cur.next.capture(list.get(i)), i);
					cur = next;
					patterns = patterns.intersect(cur.routes);
					i++;
				} else {
					break;
				}
			}
		}
		return r;
	}
	
	// TODO: cycles still end up being copied - should try to prevent this!
	Node<S> replace(final Map<Node<S>, Node<S>> replace, int offset) {
		final ReplaceResult<S> r = replace(replace, new IdentityHashMap<Node<S>, Node<S>>(), 0);
		if (r.action == ReplaceAction.REPLACED_PROVIDED) {
			return r.result;
		}		
		return this;
	}
	Node<S> replace(final Map<Node<S>, Node<S>> replace) {
		return replace(replace, 0);
	}
	private static enum ReplaceAction {
		REPLACED_NONE, REPLACED_EXTRA, REPLACED_PROVIDED 
	}
	private static final class ReplaceResult<S> {
		final ReplaceAction action;
		final Node<S> result;
		private ReplaceResult(ReplaceAction action, Node<S> result) {
			this.action = action;
			this.result = result;
		}
	}
	private ReplaceResult<S> replace(final Map<Node<S>, Node<S>> replaceProvided, final Map<Node<S>, Node<S>> replaceExtra, int offset) {
		Node<S> self = replaceProvided.get(this);
		if (self != null) {			
			return new ReplaceResult<S>(ReplaceAction.REPLACED_PROVIDED, self);
		}
		self = replaceExtra.get(this);
		if (self != null) {			
			return new ReplaceResult<S>(ReplaceAction.REPLACED_EXTRA, self);
		}
		if (offset == 0 && next.isEmpty()) {
			return new ReplaceResult<S>(ReplaceAction.REPLACED_NONE, this);
		}
		boolean chg1 = offset > 0;
		boolean chg2 = offset > 0;
		self = new Node<S>((NodeMap<S>) null, accepts.shift(offset), routes.shift(offset), inLoop);
		replaceExtra.put(this, self);
		for (Node<S> next : this.next) {
			final ReplaceResult<S> r = next.replace(replaceProvided, replaceExtra, offset);
			switch(r.action) {
			case REPLACED_EXTRA:
				chg2 = true; 
				break;
			case REPLACED_PROVIDED:
				chg1 = true;
			}
		}
		if (chg1) {
			self.next = next.replace(replaceExtra, offset).replace(replaceProvided, 0);
			return new ReplaceResult<S>(ReplaceAction.REPLACED_PROVIDED, self);
		} else if (chg2) {
			self.next = next.replace(replaceExtra, offset);
			return new ReplaceResult<S>(ReplaceAction.REPLACED_EXTRA, self);
		} else {
			replaceExtra.put(this, this);
			return new ReplaceResult<S>(ReplaceAction.REPLACED_NONE, this);
		}
	}
	
	Node<S> copy() {
		return copy(new IdentityHashMap<Node<S>, Node<S>>());
	}
	private Node<S> copy(final Map<Node<S>, Node<S>> replace) {
		Node<S> self = replace.get(this);
		if (self != null) {
			return self;
		}
		self = new Node<S>((NodeMap<S>) null, accepts, routes, inLoop);
		replace.put(this, self);
		for (Node<S> next : this.next) {
			next.copy(replace);
		}
		self.next = next.replace(replace, 0);
		return self;
	}

	/**
	 * @return a copy of the node graph where all routes and acceptance states have been reset to IdSet.unitary()
	 */
	public Node<S> resetPaths() {
		return resetPaths(copy(), new IdentityHashMap<Node<S>, Node<S>>());
	}
	
	private static <S> Node<S> resetPaths(Node<S> node, Map<Node<S>, Node<S>> visited) {
		if (visited.put(node, node) != null) {
			return node;
		}
		if (!node.accepts.isEmpty()) {
			node.accepts = IdSet.unitary();
		}
		node.routes = IdSet.unitary();
		for (Node<S> child : node.next) {
			resetPaths(child, visited);
		}
		return node;
	}
	
	/**
	 * @return a copy of the node graph where all routes and acceptance states have been compacted to remove any gaps in the route numbering scheme
	 */
	public Node<S> compactPaths() {
		if (routes.len - 1 == routes.ids[routes.len - 1]) {
			return this;
		}
		final int[] idmap = new int[routes.ids[routes.len - 1] + 1];
		java.util.Arrays.fill(idmap, -1);
		for (int i = 0 ; i != routes.len ; i++) {
			idmap[routes.ids[i]] = i;
		}
		return compactPaths(copy(), idmap, new IdentityHashMap<Node<S>, Node<S>>());
	}
	
	private static <S> Node<S> compactPaths(Node<S> node, int[] idmap, Map<Node<S>, Node<S>> visited) {
		if (visited.put(node, node) != null) {
			return node;
		}
		if (!node.accepts.isEmpty()) {
			node.accepts = node.accepts.remap(idmap);
		}
		node.routes = node.routes.remap(idmap);
		for (Node<S> child : node.next) {
			compactPaths(child, idmap, visited);
		}
		return node;
	}

	/**
	 * To be used in on result of acceptanceCollisions() - all acceptance states in the node graph
	 * should accept exactly two route ids - each combination of two routes that can be accepted are
	 * returned.
	 * 
	 * @param groups
	 * @return
	 */
	public Node<S>[] separate(int groups) {
		if (routes.len == groups) {
			return new Node[] { this };
		}
		Node<S>[] results = new Node[routes.len];
		IdSet separated = IdSet.empty();
		int i = 0;
		while (separated.len != routes.len) {
			results[i] = separate(this, groups, routes, separated, new IdentityHashMap<Node<S>, Node<S>>());
			separated = separated.append(results[i].routes, 0);
			i += 1;
		}
		if (results.length != i) {
			results = java.util.Arrays.copyOf(results, i);
		}
		return results;
	}
	
	private static <S> Node<S> separate(Node<S> cur, int groups, IdSet ids, IdSet separated, Map<Node<S>, Node<S>> remaps) {		
		Node<S> self = remaps.get(cur);
		if (self != null) {
			if (cur.inLoop) {
				// may be overzealously assigning loop-ness (as elsewhere)				
				self.inLoop = true;
			}
			self.routes = ids; 
			return self;
		}
		if (cur.routes.len == groups) {
			return cur.replace(remaps);
		} else if (cur.routes.len < groups) {
			throw new IllegalStateException();
		}
		self = new Node<S>();
		remaps.put(cur, self);
		final NodeMapBuilder<S> build = cur.next.scheme().build();
		for (NodeMapEntry<S> e : cur.next.entries()) {
			Group<S> syms = e.sym();
			Node<S> next = e.next();
			if (ids.isSuperset(next.routes) && !separated.isSuperset(next.routes)) {
				next = separate(next, groups, ids, separated, remaps);
				self.inLoop |= next.inLoop;
				ids = next.routes;
				build.bind(syms, next, e.capture());
			}
		}
		self.next = build.done();
		self.accepts = cur.accepts.intersect(ids);
		self.routes = self.next.isEmpty() ? self.accepts : ids;
		return self;
	}
	
	public Node<S> acceptanceCollision(Node<S> right) {
		return acceptanceCollision(this, right, this.routes.len, new IdentityHashMap<Node<S>, Node<S>>());
	}
	
	private static <S> Node<S> acceptanceCollision(Node<S> left, Node<S> right, int offset, Map<Node<S>, Node<S>> remaps) {
		if (remaps.containsKey(left) && remaps.containsKey(right)) {
			if (remaps.get(left) == remaps.get(right)) {
				Node<S> r = remaps.get(left);
				if (left.inLoop & right.inLoop) {
					r.inLoop = true;
				}
				return r;
			} else {
				return null;
			}
		}
		final NodeMapSplit<S> split = left.next.split(right.next, 0);
		if (split.intersect.length == 0) {
			if (left.accepts.isEmpty() || right.accepts.isEmpty()) {
				return null;
			}
			final IdSet accepts = left.accepts.append(right.accepts, offset);
			return new Node<S>(left.next.scheme().emptyMap(), accepts, accepts, false);
		}
		IdSet accepts = left.accepts.isEmpty() || right.accepts.isEmpty() ? IdSet.empty() : left.accepts.append(right.accepts, offset);
		final Node<S> self = new Node<S>(null, accepts, accepts, false);
		remaps.put(left, self);
		remaps.put(right, self);
		NodeMapBuilder<S> next = null;
		for (NodeIntersect<S> i : split.intersect) {			
			Node<S> collision = acceptanceCollision(i.left, i.right, offset, remaps);
			if (collision != null) {
				self.inLoop |= collision.inLoop;
				self.routes = self.routes.append(collision.routes, 0);
				if (next == null) {
					 next = left.next.scheme().build();
				}
				next.bind(i.symbols, collision, IdCapture.empty());
			}
		}
		if (next == null) {
			remaps.put(left, left);
			remaps.put(right, right);
			return null;
		} else {
			self.next = next.done();
			return self;
		}
	}
	
	public Node<S> subtract(Node<S> intersectionWithRightOperand, int offset) {
		final Node<S> s = subtract(this, intersectionWithRightOperand, offset, new IdentityHashMap<Node<S>, Node<S>>(), new IdentityHashMap<Node<S>, Node<S>>());
		if (s == null) {
			return next.scheme().empty();
		}
		return s;
	}
	
	// removes the acceptance states and routes; minus should have been derived from trg
	private static <S> Node<S> subtract(Node<S> trg, Node<S> minus, int offset, Map<Node<S>, Node<S>> remaps, Map<Node<S>, Node<S>> nulls) {
		Node<S> self = nulls.get(trg);
		if (self != null) {
			return null;
		}
		self = remaps.get(trg);
		if (self != null) {
			return self;
		}
		if (!minus.inLoop) {
			trg = trg.unrollLoop();
		}
		final NodeMapSplit<S> split = trg.next.split(minus.next, 0);
		if (!split.right.isEmpty()) {
			throw new IllegalStateException();
		}
		self = new Node<S>(null, trg.accepts.subtract(minus.accepts, offset), IdSet.empty(), trg.inLoop);
		remaps.put(trg, self);
		NodeMapBuilder<S> next = trg.next.scheme().build();
		for (Node<S> n : split.left) {
			self.routes = self.routes.append(n.routes, 0);
		}
		for (NodeIntersect<S> i : split.intersect) {			
			Node<S> n = subtract(i.left, i.right, offset, remaps, nulls);
			if (n != null) {
				next.bind(i.symbols, n, i.leftCapture);
				self.routes = self.routes.append(n.routes, 0);
			}
		}
		self.next = trg.next.scheme().merge(replace(split.left, remaps), next.done());
		if (self.next.isEmpty() && self.accepts.isEmpty()) {
			nulls.put(trg, trg);
			remaps.remove(trg);
			return null;
		}
		return self;
	}
	
	// this method modifies the Node, whereas all others treat is as immutable
	// this is to massively simplify the logic for making the loop, and reduce the
	// compilation time of an expression, since loops are by definition self contained
	// so mutating them during their construction is safe
	void makeLoop(Node<S> marker) {
		makeLoop(marker, this, this, new IdentityHashMap<Node<S>, Boolean>(4));
//		capture = capture.loop();
	}
	
	private static <S> boolean makeLoop(Node<S> marker, Node<S> head, Node<S> cur, IdentityHashMap<Node<S>, Boolean> visited) {
		final Boolean v = visited.put(cur, Boolean.TRUE);
		if (v != null) {
			if (v != Boolean.TRUE) {
				visited.put(cur, v);
			}
			return v;
		}
		boolean dorepl = false;
		boolean loop = false;
		for (Node<S> n : cur.next) {
			if (n == marker) {
				loop = dorepl = true;				
			} else {
				loop |= makeLoop(marker, head, n, visited);
			}
		}
		if (dorepl) {
			cur.next.replaceInSitu(marker, head);
		}
		cur.inLoop = loop;
		return loop;
	}
	
	private static <S> NodeMap<S> replace(NodeMap<S> paths, Map<Node<S>, Node<S>> replace) {
		return replace(paths, replace, 0);
	}
	private static <S> NodeMap<S> replace(NodeMap<S> paths, Map<Node<S>, Node<S>> replace, int offset) {
		final Map<Node<S>, Node<S>> replaceExtra = new IdentityHashMap<Node<S>, Node<S>>();
		for (Node<S> next : paths) {
			next.replace(replace, replaceExtra, offset);
		}
		return paths.replace(replaceExtra, offset).replace(replace, 0);
	}
	
	public String toString() {
		final StringBuilder b = new StringBuilder();
		toString(this, 0, 0, b, new IdentityHashMap<Node<S>, Integer>(8));
		return b.toString();
	}
	
	public String toOneLineString() {
		final StringBuilder b = new StringBuilder();
		toOneLineString(this, 0, b, new IdentityHashMap<Node<S>, Integer>(8));
		return b.toString();
	}
	
	private static <S> int toString(Node<S> node, int depth, int count, StringBuilder builder, IdentityHashMap<Node<S>, Integer> visited) {
		if (count > 0) {
			builder.append(" => ");
		}
		if (visited.containsKey(node)) {			
			builder.append(visited.get(node));
			if (!node.accepts.isEmpty()) {
				builder.append("*");
			}
			return count;
		}
		visited.put(node, count);
		if (count > 0) {
			builder.append(count);
			depth += 1;
		}
		boolean sameLine = count == 0;
		count += 1;
		if (!node.accepts.isEmpty()) {
			builder.append("*");
		}
		for (NodeMapEntry<S> e : node.next.entries()) {
			if (!sameLine) {
				builder.append("\n");
				for (int i = 0 ; i != depth ; i++) {
					builder.append("\t");
				}
			} else {
				sameLine = false;
			}
			builder.append(e.sym());
			count = toString(e.next(), depth, count, builder, visited);
		}
		return count;
	}
	
	private static <S> int toOneLineString(Node<S> node, int count, StringBuilder builder, IdentityHashMap<Node<S>, Integer> visited) {
		if (count > 0) {
			builder.append(" => ");
		}
		if (visited.containsKey(node)) {			
			builder.append(visited.get(node));
			if (!node.accepts.isEmpty()) {
				builder.append("*");
			}
			return count;
		}
		visited.put(node, count);
		if (count > 0) {
			builder.append(count);
		}
		if (!node.accepts.isEmpty()) {
			builder.append("*");
		}
		boolean sameLine = false;
		count += 1;
		if (!node.next.isEmpty()) {
			builder.append(" {");
			for (NodeMapEntry<S> e : node.next.entries()) {
				if (!sameLine) {
					sameLine = false;
				} else {
					builder.append(", ");
				}
				builder.append(e.sym());
				count = toOneLineString(e.next(), count, builder, visited);
			}
			builder.append("}");
		}
		return count;
	}

	public boolean inLoop() {
		return inLoop;
	}
	
}
