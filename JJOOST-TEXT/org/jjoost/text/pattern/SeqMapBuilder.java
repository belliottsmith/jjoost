package org.jjoost.text.pattern;

import java.util.Arrays;

import org.jjoost.text.pattern.SeqMap.PatGroup;

abstract class SeqMapBuilder<S, Seq extends Iterable<S>> implements NodeMapBuilder<Seq> {

	Node<S> lookup = null;
	Node<Seq>[] xref = null;
	IdCapture[] capture = null;
	int c = 0;
	
	protected abstract SeqMap<S, Seq> create(Node<S> lookup, Node<Seq>[] xref, IdCapture[] capture);
	
	@Override
	public NodeMap<Seq> done() {
		return create(lookup, xref, capture);
	}

	@SuppressWarnings("unchecked")
	@Override
	public NodeMapBuilder<Seq> bind(Group<Seq> s, Node<Seq> n, IdCapture capt) {
		if (s instanceof PatGroup) {
			final PatGroup<S, Seq> g = (PatGroup<S, Seq>) s;
			if (lookup == null) {
				lookup = g.node;
				xref = new Node[] { n };
				capture = new IdCapture[] { capt };
			} else {
				lookup = lookup.mergeAlternatePattern(g.node, false);
				xref = Arrays.copyOf(xref, c + 1);
				xref[c] = n;
				capture = Arrays.copyOf(capture, c + 1);
				capture[c] = capt;
			}
			c++;
			return this;
		} else {
			throw new IllegalStateException();
		}
	}
	
}

