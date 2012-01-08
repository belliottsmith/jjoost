package org.jjoost.text.pattern;

import java.util.HashMap;

import org.jjoost.text.pattern.SeqMap.PatGroup;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class SeqScheme<S, Seq extends Iterable<S>> extends NodeScheme<Seq> {

	final HashMap<String, Node<S>> patterns = new HashMap<String, Node<S>>();
	final SeqMap EMPTY = new SeqMapImpl(base().empty(), new Node[0], new IdCapture[0]);
	
	void setPattern(String name, Node<S> pattern) {
		patterns.put(name, pattern);
	}
	
	protected abstract NodeScheme<S> base();
	
	protected final class SeqMapImpl extends SeqMap<S, Seq> {

		private static final long serialVersionUID = -8923054668825626729L;

		protected SeqMapImpl(Node<S> lookup, Node<Seq>[] xref,
				IdCapture[] capture) {
			super(lookup, xref, capture);
		}

		@Override
		public NodeScheme<Seq> scheme() {
			return SeqScheme.this;
		}

		@Override
		protected NodeScheme<S> base() {
			return SeqScheme.this.base();
		}

		@Override
		protected SeqMap<S, Seq> empty() {
			return EMPTY;
		}

		@Override
		protected SeqMap<S, Seq> create(Node<S> lookup, Node<Seq>[] xref, IdCapture[] capture) {
			return new SeqMapImpl(lookup, xref, capture);
		}

	}
	
	protected final class SeqMapBuilderImpl extends SeqMapBuilder<S, Seq> {
		
		@Override
		protected SeqMap<S, Seq> create(Node<S> lookup, Node<Seq>[] xref, IdCapture[] capture) {
			return new SeqMapImpl(lookup, xref, capture);
		}
		
	}
	
	@Override
	public SeqMap<S, Seq> merge(NodeMap<Seq> ... merge) {
		if (merge.length == 0) {
			return EMPTY;
		} else {
			SeqMap<S, Seq> acc = null;
			for (int i = 0 ; i != merge.length ; i++) {
				if (merge[0] instanceof SeqMap) {
					if (acc == null) {
						acc = (SeqMap<S, Seq>) merge[i];
					} else {
						acc = acc.merge((SeqMap<S, Seq>) merge[i]);
					}
				} else if (merge[0] == null) {
					throw new NullPointerException();
				} else {
					throw new IllegalArgumentException("Incompatible map type provided: expected SeqMap, received " + merge[0].getClass());
				}
			}
			return acc;
		}
	}
	
	@Override
	public NodeMapBuilder<Seq> build() {
		return new SeqMapBuilderImpl();
	}
	
	@Override
	public NodeMap<Seq> emptyMap() {
		return EMPTY;
	}

	public Parser parser() {
		return new Parser();
	}

	final class BuildPatternStep extends BuildRegex<Seq> {

		private final Node<S> pattern;	
		public BuildPatternStep(Node<S> pattern) {
			super(SeqScheme.this);
			this.pattern = pattern;
		}

		@Override
		public Node<Seq> toNodeGraph(NodeRef<Seq> tail, IdCapture end) {
			SeqMapBuilder builder = new SeqMapBuilderImpl();
			builder.bind(new PatGroup<S, Seq>(pattern), tail.ref, end);
			return new Node<Seq>(builder.done());
		}

	}

	final class Parser extends Parse<Seq> {
		@Override
		public NodeScheme<Seq> scheme() {
			return SeqScheme.this;
		}
		@Override
		protected void parseToken(int type, String var, Accumulator accum) {
			if (type < 0) {
				super.parseToken(type, var, accum);
			} else {
				switch (type) {
				case 1: case 2:
					final Node<S> pattern = patterns.get(var);
					if (pattern == null) {
						throw new IllegalArgumentException("Unrecognised pattern identifier " + var);
					}
					accum.add(new BuildPatternStep(pattern));
					break;
				default:
					throw new IllegalStateException();
				}
			}
		}
	}
	
}
