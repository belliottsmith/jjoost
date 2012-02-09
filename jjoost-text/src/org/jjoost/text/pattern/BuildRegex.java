package org.jjoost.text.pattern;

public abstract class BuildRegex<S> {

	final NodeScheme<S> scheme;
	public BuildRegex(NodeScheme<S> scheme) {
		this.scheme = scheme;
	}

	public abstract Node<S> toNodeGraph(NodeRef<S> tail, IdCapture end);
	
	@SuppressWarnings("unchecked")
	BuildRegex<S> or(BuildRegex<S> expr) {
		return new BuildSelect<S>(this, expr);
	}

	@SuppressWarnings("unchecked")
	BuildRegex<S> then(BuildRegex<S> expr) {
		return new BuildSequence<S>(this, expr);
	}
	
	BuildRegex<S> rep(int reps) {
		return new BuildFiniteRepeat<S>(reps, this);
	}
	
	BuildRegex<S> repinf() {
		return new BuildInfiniteRepeat<S>(this);
	}
	
	BuildRegex<S> opt() {
		return new BuildOption<S>(this);
	}
	
}
