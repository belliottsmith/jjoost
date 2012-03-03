package org.jjoost.text.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jjoost.collections.Map;
import org.jjoost.collections.lists.UniformList;
import org.jjoost.collections.maps.serial.SerialHashMap;
import org.jjoost.text.pattern.NodeMapSplit.NodeIntersect;
import org.jjoost.util.Function;

public final class CharScheme extends NodeScheme<CharScheme.Char> {

//	private static final Parser PARSER = new Parser();
//	private static final Parser PARSER_INSENSITIVE = new Parser().setCaseInsensitive(true);
	private static final NodeMap<Char> EMPTY = new CharMapBuilder().done();
	private static CharScheme SCHEME = new CharScheme();
	public static CharScheme get() {
		return SCHEME;
	}
	
	public static Parser parser() {
		return new Parser();
	}
	
	public static Parser parserCaseInsensitive() {
		return new Parser().setCaseInsensitive(true);
	}
	
	@Override
	public NodeMap<Char> merge(NodeMap<Char>... merge) {
		if (merge.length <= 1) {
			if (merge.length == 0) {
				return EMPTY;
			} else {
				return merge[0];
			}
		}
		final Maplet[][] srcs = new Maplet[merge.length][];
		for (int i = 0 ; i != srcs.length ; i++) {
			if (merge[i] instanceof CharMap) {
				srcs[i] = ((CharMap) merge[i]).maplets;
			} else {
				throw new IllegalStateException();
			}
		}
		Maplet[] src1 = null;
		for (Maplet[] src2 : srcs) {
			if (src1 == null) {
				src1 = src2;
			} else {
				final Maplet[] trg = new Maplet[src1.length + src2.length];
				int i = 0, j = 0, c = 0;
				while (i != src1.length && j != src2.length) {
					if (src1[i].overlaps(src2[j])) {
						throw new IllegalStateException();
					}
					if (src1[i].first < src2[j].first) {
						trg[c++] = src1[i++];
					} else {
						trg[c++] = src2[j++];
					}
				}
				while (i != src1.length) {
					trg[c++] = src1[i++];
				}
				while (j != src2.length) {
					trg[c++] = src2[j++];
				}
				src1 = trg;
			}
		}
		return new CharMap(src1);
	}

	@Override
	public NodeMapBuilder<Char> build() {
		return new CharMapBuilder();
	}

	@Override
	public NodeMap<Char> emptyMap() {
		return EMPTY;
	}
	
	static final class BuildCharClass extends BuildRegex<Char> {

		private final CharRange[] groups;	
		public BuildCharClass(char c) {
			super(CharScheme.get());
			this.groups = new CharRange[] { new CharRange(c, c) };
		}
		public BuildCharClass(char start, char end) {
			super(CharScheme.get());
			this.groups = new CharRange[] { new CharRange(start, end) };
		}
		public BuildCharClass(CharRange[] groups) {
			super(CharScheme.get());
			this.groups = groups;
		}

		@Override
		public Node<Char> toNodeGraph(NodeRef<Char> tail, IdCapture end) {
			CharMapBuilder builder = new CharMapBuilder();
			for (CharRange g : groups) {
				builder.bind(g, tail.ref, IdSet.unitary(), end);
			}
			return new Node<Char>(builder.done());
		}
		
		public String toString() {
			if (groups.length == 1 && groups[0].first == groups[0].last) {
				return Character.toString(groups[0].first);
			}
			final StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (CharRange g : groups) {
				sb.append(g.first == g.last ? Character.toString(g.first) : g.first + "-" + g.last);
			}
			sb.append("]");
			return sb.toString();
		}
		
	}

	// doesn't support full UTF-16; should perhaps switch to Character? 
	public static final class Char {

		final char c;
		public Char(char c) {
			this.c = c;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + c;
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Char other = (Char) obj;
			if (c != other.c)
				return false;
			return true;
		}

		public static BuildSequence<Char> lit(String literal) {
			final BuildCharClass[] seq = new BuildCharClass[literal.length()];
			for (int i = 0 ; i != literal.length() ; i++) {
				seq[i] = new BuildCharClass(literal.charAt(i));
			}
			return new BuildSequence<CharScheme.Char>(seq);
		}
		
		public String toString() {
			return Character.toString(c);
		}
		
		private static Char[] cache = new Char[65536];
		
		public static Char valueOf(char c) {
			int i = (int) c;
			if ((i & 65535) == i) {
				Char r = cache[i];
				if (r == null) {
					r = cache[i] = new Char(c);
				}
				return r;
			} else {
				return new Char(c);
			}
		}
		
	}
	
	static final class CharMapBuilder implements NodeMapBuilder<Char> {

		Maplet[] ms = new Maplet[4];
		int c = 0;
		
		@Override
		public NodeMap<Char> done() {
			return new CharMap(Arrays.copyOf(ms, c));
		}

		@Override
		public NodeMapBuilder<Char> bind(Group<Char> s, Node<Char> n, IdSet routes, IdCapture capture) {
			if (s instanceof CharRange) {
				final CharRange g = (CharRange) s;
				int i = floor(ms, g.first, c);
				if (i > 0 && ms[i].overlaps(g)) {
					throw new IllegalArgumentException(g + " overlaps with existing binding " + ms[i]);
				}
				shift(i += 1);
				c++;
				ms[i] = new Maplet(n, routes, capture, g.first, g.last);
				return this;
			} else {
				throw new IllegalStateException();
			}
		}
		
		public NodeMapBuilder<Char> bind(Maplet m) {
			int i = floor(ms, m.first, c);
			if (i > 0 && ms[i].overlaps(m)) {
				throw new IllegalArgumentException(m + " overlaps with existing binding " + ms[i]);
			}
			shift(i += 1);
			ms[i] = m;
			c++;
			return this;
		}
		
		void shift(int i) {
			if (c == ms.length) {
				ms = Arrays.copyOf(ms, c << 1);
			}
			int j = c;
			while (j > i) {
				ms[j] = ms[j - 1];
				j -= 1;
			}
		}
		
	}

	public static final class CharMap implements NodeMap<Char>{

		private static final long serialVersionUID = -723788106826374001L;
		final Maplet[] maplets;
		final char[] fastlookup;
//		final SpecialMaplet special;
		private CharMap(Maplet[] maplets) {
			this.maplets = maplets;
			this.fastlookup = new char[maplets.length];
			for (int i = 0 ; i != maplets.length ; i++) {
				fastlookup[i] = maplets[i].first;
			}
		}

		public NodeMapEntry<Char> lookup(Char s) {
			final char c = s.c;
			int i = floor(fastlookup, c);
			Maplet maplet;
			if (i >= 0 && (maplet = maplets[i]).last >= c) {
				return maplet;
			}
			return null;
		}

		public NodeMapEntry<Char> lookupSpecial(SpecialTransition t) {
//			switch (t) {
//			case START:
//			case END:
//			}
//			final char c = s.c;
//			int i = floor(maplets, c, maplets.length);
//			Maplet maplet;
//			if (i >= 0 && (maplet = maplets[i]).last >= c) {
//				return maplet;
//			}
			return null;
		}
		
		@Override
		public NodeMapSplit<Char> split(NodeMap<Char> that1, int offset) {
			if (that1 instanceof CharMap) {
				CharMap that = (CharMap) that1;
				final Maplet[] lin = this.maplets;
				final Maplet[] rin = that.maplets;
				final Maplet[] lout = new Maplet[1 + lin.length + rin.length];
				final Maplet[] rout = new Maplet[1 + lin.length + rin.length];
				@SuppressWarnings("unchecked")
				final NodeIntersect<Char>[] lrout = new NodeIntersect[lout.length];
				int i = 1, j = 1;
				int lc = 0, rc = 0, lrc = 0;
				Maplet lr = lin.length > 0 ? lin[0] : null;
				Maplet rr = rin.length > 0 ? rin[0] : null; // remainder from any split
				while (lr != null & rr != null) {
					if (lr.last < rr.first) {
						lout[lc++] = lr;
						lr = i < lin.length ? lin[i++] : null;
					} else if (lr.first > rr.last) {
						rout[rc++] = rr;
						rr = j < rin.length ? rin[j++] : null;
					} else {
						final CharRange intersect = new CharRange(max(lr.first, rr.first), min(lr.last, rr.last));
						lrout[lrc++] = new NodeIntersect<Char>(intersect, lr.next, lr.routes, lr.capture, rr.next, rr.routes.shift(offset), rr.capture.shift(offset));
						if (lr.first < rr.first) {
							lout[lc++] = new Maplet(lr.next, lr.routes, lr.capture, lr.first, (char)(intersect.first - 1));							
						}
						if (rr.first < lr.first) {
							rout[rc++] = new Maplet(rr.next, rr.routes.shift(offset), rr.capture.shift(offset), rr.first, (char)(intersect.first - 1));
						}
						if (lr.last > rr.last) {
							lr = new Maplet(lr.next, lr.routes, lr.capture, (char)(intersect.last + 1), lr.last);
							rr = j < rin.length ? rin[j++] : null;
						} else if (lr.last < rr.last) {
							rr = new Maplet(rr.next, rr.routes.shift(offset), rr.capture.shift(offset), (char)(intersect.last + 1), rr.last);
							lr = i < lin.length ? lin[i++] : null;
						} else {
							lr = i < lin.length ? lin[i++] : null;
							rr = j < rin.length ? rin[j++] : null;
						}
					}
				}
				while (lr != null) {
					lout[lc++] = lr;
					lr = i < lin.length ? lin[i++] : null;
				}
				while (rr != null) {
					rout[rc++] = rr;
					rr = j < rin.length ? rin[j++] : null;
				}
				for (int lri = 0 ; lri < lrc - 1 ; lri++) {
					int newlrc = lri + 1;
					for (int lrj = newlrc ; lrj < lrc ; lrj++) {
						final NodeIntersect<Char> merge = lrout[lri].merge(lrout[lrj]);
						if (merge != null) {
							lrout[lri] = merge;
						} else {
							lrout[newlrc++] = lrout[lrj];
						}
					}
					lrc = newlrc;
				}
				return new NodeMapSplit<Char>(new CharMap(Arrays.copyOf(lout, lc)), new CharMap(Arrays.copyOf(rout, rc)), Arrays.copyOf(lrout, lrc));
			} else {
				throw new IllegalArgumentException();
			}
		}

		@Override
		public NodeScheme<Char> scheme() {
			return SCHEME;
		}

		@Override
		public Iterator<Node<Char>> iterator() {
			return new Iterator<Node<Char>>() {
				int p = 0;
				final Maplet[] m = maplets;
				@Override
				public boolean hasNext() {
					return p < m.length;
				}
				@Override
				public Node<Char> next() {
					return m[p++].next;
				}
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}				
			};
		}
		
		@Override
		public Iterable<NodeMapEntry<Char>> entries() {
			return Arrays.asList((NodeMapEntry<Char>[])maplets);
		}
		
		@Override
		public NodeMap<Char> replace(Function<Node<Char>, Node<Char>> replace, int offset) {
			final Maplet[] ms = maplets.clone();
			for (int i = 0 ; i != ms.length ; i++) {
				final Maplet m = ms[i];
				final Node<Char> repl = replace.apply(m.next);				
				if (repl != null) {
					ms[i] = new Maplet(repl, m.routes.shift(offset), m.capture.shift(offset), m.first, m.last);
				} else if (offset > 0) {
					ms[i] = new Maplet(m.next, m.routes.shift(offset), m.capture.shift(offset), m.first, m.last);
				}
			}
			return new CharMap(ms);
		}

		@Override
		public boolean isEmpty() {
			return maplets.length == 0;
		}

		@Override
		public void replaceInSitu(Node<Char> replace, Node<Char> with) {
			for (Maplet m : maplets) {
				if (m.next == replace) {
					m.next = with;
				}
			}
		}

		@Override
		public NodeMap<Char> addCapture(IdCapture capture) {
			final Maplet[] maplets = this.maplets;
			final Maplet[] ms = new Maplet[maplets.length];
			for (int i = 0 ; i != ms.length ; i++) {
				final Maplet m = maplets[i];
				ms[i] = new Maplet(m.next, m.routes, m.capture.append(capture, false, 0), m.first, m.last);
			}
			return new CharMap(ms);
		}

	}
	
	private static final List<CharRange> SPACES = Arrays.asList(new CharRange(' ', ' '), new CharRange('\t', '\t'), new CharRange('\n', '\n'), new CharRange('\r', '\r'));

	// TODO : support more comples group expressions (e.g. intersection, union and Unicode groups)
	public static final class Parser extends Parse<Char> {
		@Override
		public NodeScheme<Char> scheme() {
			return SCHEME;
		}
		
		private boolean caseInsensitive;
		private Map<String, List<CharRange>> userDefinedGroups;
		public boolean isCaseInsensitive() {
			return caseInsensitive;
		}
		public Parser setCaseInsensitive(boolean on) {
			caseInsensitive = on;
			return this;
		}
		public Parser defineGroup(String name, String expression) {
			return defineGroup(name, parseGroupExpression(expression));
		}
		public Parser defineGroup(String name, List<CharRange> group) {
			if (userDefinedGroups == null) {
				userDefinedGroups = new SerialHashMap<String, List<CharRange>>();
			}
			userDefinedGroups.put(name, clean(group, false, false));
			return this;
		}
		@Override
		protected void parseToken(int type, String var, Accumulator accum) throws ParseException {
			if (type < 0) {
				super.parseToken(type, var, accum);
			} else {
				List<CharRange> groups;
				switch (type) {
				case 1:
					if (var.length() != 1) {
						if (var.charAt(0) != '\\' || var.length() != 2) {
							throw new IllegalStateException();
						}
						final char c = var.charAt(1);
						switch(c) {
						case 's':
							groups = SPACES;
							break;
						default:
							groups = new UniformList<CharRange>(new CharRange(c, c), 1);						
						}
					} else {
						final char c = var.charAt(0);
						switch (c) {
						case '.':
							groups = new UniformList<CharRange>(new CharRange(Character.MIN_VALUE, Character.MAX_VALUE), 1);
							break;
						default:
							groups = new UniformList<CharRange>(new CharRange(c, c), 1);
						}
					}
					break;
				case 2:
					groups = parseGroupExpression(var);
					break;
				default:
					throw new IllegalStateException();
				}
				groups = clean(groups, false, caseInsensitive);
				accum.add(new BuildCharClass(groups.toArray(new CharRange[groups.size()])));
			}
		}
		
		private List<CharRange> parseGroupExpression(String expression) {
			final List<CharRange> groups = new ArrayList<CharRange>();
			boolean negate = expression.startsWith("^");
			boolean range = false;
			boolean escape = false;
			for (int i = negate ? 1 : 0 ; i != expression.length() ; i++) {
				char c = expression.charAt(i);
				switch (c) {
				case '-':
					if (escape) {
						groups.add(new CharRange(c, c));
						escape = false;
					} else {
						range = true;
					}
					break;
					
				case '\\':
					if (!escape) {
						escape = true;
						break;
					}
				case 'g': 
					if (escape & (c == 'g')) {
						escape = false;
						if (i + 1 == expression.length() || expression.charAt(i + 1) != '{') {
							throw new IllegalArgumentException("user-defined group expression incomplete (expected { following \\g in group expression)");
						}
						final int start = i += 2;
						for (; i < expression.length() && expression.charAt(i) != '}' ; i++);
						if (i == expression.length()) {
							throw new IllegalArgumentException("user-defined group expression incomplete (expected closing } following \\g{ in group expression)");
						}
						final String name = expression.substring(start, i);
						final List<CharRange> ranges = userDefinedGroups == null ? null : userDefinedGroups.get(name);
						if (ranges == null) {
							throw new IllegalArgumentException("no user-defined group called " + name);
						}
						groups.addAll(ranges);
						break;
					}
				default:
					escape = false;
					if (range) {
						if (groups.size() == 0) {
							throw new IllegalArgumentException("range declared without start; hyphens should be preceded by at least one char in a character class declaration");
						}
						final CharRange prev = groups.get(groups.size() - 1);
						if (prev.first != prev.last) {
							throw new IllegalArgumentException("range declared without valid start - preceded by end of another range declaration; two hyphens should be separated by at least two chars in any character class declaration");
						}
						if (prev.first >= c) {
							throw new IllegalArgumentException("Illegal range [" + prev.first + "-" + c + "] declared; " + prev.first + " is greater than or equal to " + c);
						}
						groups.set(groups.size() - 1, new CharRange(prev.first, c));
						range = false;
					} else {
						groups.add(new CharRange(c, c));
					}
				}
			}
			return clean(groups, negate, caseInsensitive);
		}

	}
	
	private static List<CharRange> clean(List<CharRange> groups, boolean negate, boolean caseInsensitive) {
		if (!negate && !caseInsensitive && groups.size() <= 1) {
			return groups;
		}
		if (!(groups instanceof ArrayList)) {
			groups = new ArrayList<CharRange>(groups);
		}
		final int a = (int) 'a', z = (int) 'z', A = (int) 'A', Z = (int) 'Z';
		if (caseInsensitive) {
			for (CharRange cg : new ArrayList<CharRange>(groups)) {
				if (cg.first <= z && cg.last >= a && (cg.last < A || cg.first > Z)) {
					groups.add(new CharRange((char) ((A - a) + Math.max(a, cg.first)), (char) ((A - a) + Math.min(z, cg.last))));
				} else if (cg.first <= Z && cg.last >= A && (cg.last < a || cg.first > z)) {
					groups.add(new CharRange((char) ((a - A) + Math.max(A, cg.first)), (char) ((a - A) + Math.min(Z, cg.last))));
				}
			}
		}
		if (groups.size() > 1) {
			java.util.Collections.sort(groups);
			int i = 1;
			CharRange last = groups.get(0);
			while (i < groups.size()) {
				final CharRange next = groups.get(i);
				if (last.last >= next.first) {
					groups.remove(i);
					groups.set(i - 1, new CharRange(last.first, next.last));
				} else {							
					i++;
					last = next;
				}
			}
		}
		if (negate && !groups.isEmpty()) {
			final List<CharRange> r = new ArrayList<CharRange>(groups.size() + 1);
			CharRange last = new CharRange((char) 0, (char) 0);
			for (CharRange next : groups) {
				if (last.last + 1 < next.first - 1) {
					r.add(new CharRange((char) (last.last + 1), (char) (next.first - 1)));
				}
				last = next;
			}
			if ((last.last & 0xFF) + 1 < 65535) {
				r.add(new CharRange((char) (last.last + 1), (char) 65535));
			}
			groups = r;
		}
		return groups;
	}
	
	public static class CharRange implements Group<Char>, Comparable<CharRange> {
		
		private static final long serialVersionUID = 3604041604862420489L;		
		final char first;
		final char last;
		public CharRange(char start, char end) {
			this.first = start;
			this.last = end;
		}
		@Override
		public int compareTo(CharRange that) {
			return (int) this.first - (int) that.last;
		}
		public String toString() {
			if (first == last) {
				return print(first);
			} else {
				return "[" + print(first) + "-" + print(last) + "]";
			}
		}
		
		static String print(char c) {
			return c > 20 && c < 128 ? Character.toString(c) : Integer.toString(c);
		}
		boolean overlaps(CharRange that) {
			return this.first <= that.last && this.last >= that.first;
		}
	}

	public static final class Maplet extends CharRange implements NodeMapEntry<Char> {
		
		private static final long serialVersionUID = -4465551232339102662L;
		
		private Node<Char> next ;
		private final IdSet routes;
		private final IdCapture capture;
		public Maplet(Node<Char> next, IdSet routes, IdCapture capture, char start, char end) {
			super(start, end);
			this.next = next;
			this.capture = capture;
			this.routes = routes;
		}
		
		@Override
		public Node<Char> node() {
			return next;
		}

		@Override
		public IdCapture capture() {
			return capture;
		}

		@Override
		public IdSet routes() {
			return routes;
		}
		
	}

	static final class SpecialMaplet implements NodeMapEntry<Char> {
		
		private static final long serialVersionUID = -4465551232339102662L;
		
		private Node<Char> next ;
		private final IdSet routes;
		private final IdCapture capture;
		private final SpecialTransition transition;
		public SpecialMaplet(Node<Char> next, IdSet routes, IdCapture capture, SpecialTransition transition) {
			this.next = next;
			this.capture = capture;
			this.routes = routes;
			this.transition = transition;
		}
		
		@Override
		public Node<Char> node() {
			return next;
		}
		
		@Override
		public IdCapture capture() {
			return capture;
		}
		
		@Override
		public IdSet routes() {
			return routes;
		}
		
		@Override
		public String toString() {
			return transition.toString();
		}
		
	}
	
    private static int floor(Maplet[] a, char find, int len) {

        int i = -1;
        int j = len;
        
        // a[-1] ^= -infinity
        while (i < j - 1) {
        	
              // { a[i] <= v ^ a[j] > v }
        	
              final int m = (i + j) >>> 1;
              Maplet v = a[m];
              
              if (v.first <= find) {
                    i = m;
              } else {
                    j = m;
              }

              // { a[m] > v => a[j] > v => a[i] <= v ^ a[j] > v }
              // { a[m] <= v => a[i] <= v => a[i] <= v ^ a[j] > v }

        }

        return i ;

    }

    private static int floor(char[] a, char find) {    	 
    	
    	int i = -1;
    	int j = a.length;
    	
    	// a[-1] ^= -infinity
    	while (i < j - 1) {
    		
    		// { a[i] <= v ^ a[j] > v }
    		
    		final int m = (i + j) >>> 1;
              char v = a[m];
              
              if (v <= find) {
            	  i = m;
              } else {
            	  j = m;
              }
              
              // { a[m] > v => a[j] > v => a[i] <= v ^ a[j] > v }
              // { a[m] <= v => a[i] <= v => a[i] <= v ^ a[j] > v }
              
    	}
    	
    	return i ;
    	
    }
    
    private static char max(char a, char b) {
    	return a > b ? a : b;
    }

    private static char min(char a, char b) {
    	return a < b ? a : b;
    }

    public static final class CharIterator implements Iterator<Char> {

    	final CharSequence s;
    	int p;
    	
    	public CharIterator(CharSequence s) {
    		super();
    		this.s = s;
    	}

    	@Override
    	public boolean hasNext() {
    		return p < s.length();
    	}

    	@Override
    	public Char next() {
    		return Char.valueOf(s.charAt(p++));
    	}

    	@Override
    	public void remove() {
    	}
    	
    }

    public static final class CharList implements List<Char> {
    	
    	final CharSequence s;
    	
    	public CharList(CharSequence s) {
    		super();
    		this.s = s;
    	}

		@Override
		public boolean add(Char e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(int index, Char element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends Char> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(int index, Collection<? extends Char> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean contains(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Char get(int index) {
			return Char.valueOf(s.charAt(index));
		}

		@Override
		public int indexOf(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isEmpty() {			
			return s.length() == 0;
		}

		@Override
		public Iterator<Char> iterator() {
			return new CharIterator(s);
		}

		@Override
		public int lastIndexOf(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ListIterator<Char> listIterator() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ListIterator<Char> listIterator(int index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Char remove(int index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Char set(int index, Char element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return s.length();
		}

		@Override
		public List<Char> subList(int fromIndex, int toIndex) {
			return new CharList(s.subSequence(fromIndex, toIndex));
		}

		@Override
		public Object[] toArray() {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			throw new UnsupportedOperationException();
		}    	
    	
    }
    
}
