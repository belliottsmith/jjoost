package org.jjoost.text.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class Parse<S> {

	public static final class ParseException extends Exception {

		private static final long serialVersionUID = 8219181376052412546L;

		public ParseException() {
			super();
		}

		public ParseException(String message, Throwable cause) {
			super(message, cause);
		}

		public ParseException(String message) {
			super(message);
		}

		public ParseException(Throwable cause) {
			super(cause);
		}
	}
	
	public abstract NodeScheme<S> scheme();

	private static boolean startVar(char c) {
		return c == '{' | c == '[';
	}
	
	private static int varType(char c) {
		return c == '{' ? -1 : 2;
	}
	
	private static boolean endVar(char c) {
		return c == '}' | c == ']';
	}
	
	private static boolean isSpecial(char c) {
		return c == '*' | c == '?' | c == '|' | c == '+';
	}

	protected static final class Accumulator {
		private BuildRegex[][] stack = new BuildRegex[10][];
		private int[] len = new int[10];
		private int[] capturepos = new int[9];
		private boolean[] selecting = new boolean[10];
		private int depth;
		{
			stack[0] = new BuildRegex[1];
		}
		public void add(BuildRegex r) {
			final int pos = len[depth]++;
			if (pos == stack[depth].length) {
				stack[depth] = Arrays.copyOf(stack[depth], pos << 1);				
			}
			stack[depth][pos] = r;
		}
		private void descend() {
			depth += 1;
			if (depth == stack.length) {
				stack = Arrays.copyOf(stack, stack.length << 1);
				len = Arrays.copyOf(len, stack.length);
				capturepos = Arrays.copyOf(len, stack.length - 1);
				selecting = Arrays.copyOf(selecting, stack.length);
			}
			selecting[depth] = false;
			len[depth] = 0;
			capturepos[depth - 1] = 1;
			if (stack[depth] == null) {
				stack[depth] = new BuildRegex[6];
			}
		}
		private void ascend(Capture def) {
			int c = len[depth];
			depth -= 1;
			if (c > 1) {
				add(new BuildSequence(Arrays.copyOf(stack[depth + 1], c)));
			} else if (c > 0) {
				add(stack[depth + 1][0]);
			}
			int captureid = def.labelid(capturepos, depth);
			if (captureid >= 0) {
				replaceLast(new BuildCapture(captureid, last()));
			}
			if (depth > 0) {
				capturepos[depth - 1] += 1;
			}
		}
		private boolean extendSelect() {
//			if (stack[depth][0] instanceof BuildSelect) {
			if (selecting[depth]) {
				final BuildSelect prev = (BuildSelect) stack[depth][0];
				final BuildRegex[] next = Arrays.copyOf(prev.exprs, prev.exprs.length + 1);
				if (length() > 1) {
					next[next.length - 1] = new BuildSequence(Arrays.copyOfRange(stack[depth], 1, length()));
				} else {
					next[next.length - 1] = new BuildEmpty();
				}
				stack[depth][0] = new BuildSelect(next);
				len[depth] = 1;
				return true;
			}
			return false;
		}
		private void select() {
			if (!extendSelect()) {
				selecting[depth] = true;
				if (length() > 0) {
					stack[depth][0] = new BuildSelect(new BuildSequence(row()));
				} else {
					stack[depth][0] = new BuildSelect(new BuildEmpty());
				}
				len[depth] = 1;
			}
		}
		public int depth() {
			return depth;
		}
		public int length() {
			return len[depth];
		}
		public BuildRegex last() {
			if (depth < 0 || len[depth] == 0) {
				throw new IndexOutOfBoundsException();
			}
			return stack[depth][len[depth] - 1];
		}
		public BuildRegex[] row() {
			return Arrays.copyOf(stack[depth], len[depth]);
		}
		public void replaceLast(BuildRegex repl) {
			stack[depth][len[depth] - 1] = repl;
		}
	}
	
	public BuildRegex<S> parse(String input) {
		return parse(input, new Capture(new int[0][]));
	}
	
	public BuildRegex<S> parse(String input, Capture capture) {
		final Accumulator accum = new Accumulator();
		accum.descend();
		int i = 0;
		boolean escaping = false;
		while (i != input.length()) {
			
			final char c = input.charAt(i);
			if (escaping) {				
				parseToken(1, input.substring(i - 1, i + 1), accum);
				escaping = false;
			} else if (startVar(c)) {
				int j = i + 1;
				while (j != input.length()) {
					final char c2 = input.charAt(j);
					if (escaping) {
						escaping = false;
					} else if (endVar(c2)) {
						parseToken(varType(c), input.substring(i + 1, j), accum);
						break;
					} else if (c2 == '\\') {
						escaping = true;
					}
					j++;
				}
				escaping = false;
				if (j == input.length()) {
					throw new IllegalArgumentException("Unmatched parantheses at index " + i + ": " + c);
				}
				i = j + 1;
				continue;
			} else if (isSpecial(c)) {
				switch (c) {
				case '*':
					if (input.length() > i + 1 && input.charAt(i + 1) == '!') {
						i++;
						accum.replaceLast(new BuildInfiniteExclusiveLimitingRepeat<S>(accum.last()));
					} else if (input.length() > i + 1 && input.charAt(i + 1) == '^') {
						i++;
						accum.replaceLast(new BuildInfiniteExclusiveExtendingRepeat<S>(accum.last()));
					} else {
						accum.replaceLast(new BuildInfiniteRepeat<S>(accum.last()));
					}
					break;
				case '+':
					if (input.length() > i + 1 && input.charAt(i + 1) == '!') {
						i++;
						accum.add(new BuildInfiniteExclusiveLimitingRepeat<S>(accum.last()));
					} else if (input.length() > i + 1 && input.charAt(i + 1) == '^') {
						i++;
						accum.add(new BuildInfiniteExclusiveExtendingRepeat<S>(accum.last()));
					} else {
						accum.add(new BuildInfiniteRepeat<S>(accum.last()));
					}
					break;
				case '?':
					accum.replaceLast(new BuildOption<S>(accum.last()));
					break;
				case '|':
					accum.select();
					break;
				default:
					throw new IllegalStateException();
				}
			} else if (c == '(') {
				accum.descend();
			} else if (c == ')') {
				accum.extendSelect();
				accum.ascend(capture);
			} else if (c == '\\') {
				escaping = true;
			} else {
				parseToken(1, input.substring(i, i + 1), accum);
			}

			i++;
		}
		
		accum.extendSelect();
		accum.ascend(capture);		
		return accum.last();
	}
	
	public Node<S> compile(String input) throws ParseException {
		try {
			return compile(input, new Capture(new int[0][]));
		} catch (Exception e) {
			throw new ParseException("Could not parse input \"" + input + "\"", e);
		}
	}
	public Node<S> compile(String input, Capture capture) throws ParseException {
		try {
			return parse(input, capture).toNodeGraph(new NodeRef<S>(scheme().terminal()), IdCapture.empty());
		} catch (Exception e) {
			throw new ParseException("Could not parse input \"" + input + "\"", e);
		}
	}

	protected void parseToken(int type, String var, Accumulator accum) {
		if (type == -1) {
			// TODO: make parsing robust to bad input
			int range = var.indexOf('-');
			int select = var.indexOf(',');
			if (range < 0 && select < 0) {
				accum.replaceLast(new BuildFiniteRepeat<S>(Integer.parseInt(var), accum.last()));
			} else {
				int i = 0;
				int[] opts = new int[20];
				int c = 0;
				while (true) {
					if (range > 0 && (select < 0 || range < select)) {
						final int minj = Integer.parseInt(var.substring(i, range));
						final int maxj = Integer.parseInt(var.substring(range + 1, select > 0 ? select : var.length()));
						for (int j = minj ; j <= maxj ; j++) {
							if (c == opts.length) {
								opts = Arrays.copyOf(opts, c << 1);
							}
							opts[c++] = j;
						}
						if (select < 0) {
							break;
						}
						i = select + 1;
						select = var.indexOf(',', i);
						range = var.indexOf('-', i);
					} else if (select > 0) {
						if (c == opts.length) {
							opts = Arrays.copyOf(opts, c << 1);
						}
						opts[c++] = Integer.parseInt(var.substring(i, select));
						i = select + 1;
						select = var.indexOf(',', i);
					} else {
						if (c == opts.length) {
							opts = Arrays.copyOf(opts, c << 1);
						}
						opts[c++] = Integer.parseInt(var.substring(i));
						break;
					}
				}
				Arrays.sort(opts, 0, c);
				final BuildRegex<S> last = accum.last();
				final List<BuildRegex<S>> selection = new ArrayList<BuildRegex<S>>();
				for (i = 0 ; i != c ; i++) {
					if (i > 0 && opts[i - 1] == opts[i]) {
						continue;
					}
					if (opts[i] == 0) {
						selection.add(last);					
					} else {
						selection.add(new BuildFiniteRepeat<S>(opts[i], last));					
					}
				}
				accum.replaceLast(new BuildSelect<S>(selection.toArray(new BuildRegex[0])));
			}
		} else {
			throw new IllegalStateException("Must override parseToken and implement parsing of types > 0");
		}
	}
	
//	private static final StringReplacer ESCAPE = new StringReplacer("[\\\\^${}\\[\\]*?|+\\-]", "\\[0]");
	// TODO : use StringReplacer rather than Pattern; need to support replaceAll() rather than just match()
	private static final Pattern ESCAPE = Pattern.compile("[\\\\^${}\\[\\]*?|+\\-]");
	public static String escape(String input) {
		return ESCAPE.matcher(input).replaceAll("\\\\0");
	}

}
