package org.jjoost.text.pattern;

import java.util.Arrays;
import java.util.List;

import org.jjoost.text.pattern.CharScheme.Char;
import org.jjoost.text.pattern.Parse.ParseException;

import junit.framework.TestCase;

public class Test extends TestCase {

	static final Node<Char> TERMINAL = CharScheme.get().terminal();
	
	public void testSimple() throws ParseException {
//		final BuildRegex<Char> expr = Char.lit("abcde");
//		final Node<Char> node = expr.toNodeGraph(TERMINAL);
		final StringMatcher<String, Integer> node = matcher("abcde");
		assertNoMatch(node, "abcdeabcde");
		assertMatch(node, "abcde");
	}
	
	public void testFiniteSimpleRepetition() throws ParseException {
		String last = "";
		for (int i = 1 ; i != 5 ; i++) {
			final String cur = last + "abcde";
//			final BuildRegex<Char> expr = Char.lit("abcde").rep(i);
//			final Node<Char> node = expr.toNodeGraph(TERMINAL);
			final StringMatcher<String, Integer> node = matcher("(abcde){" + i + "}");
			assertNoMatch(node, last);
			assertMatch(node, cur);
			last = cur;
		}
	}
	
	public void testInfiniteSimpleRepetition() throws ParseException {
//		final BuildRegex<Char> expr = Char.lit("abcde").rep(0);
//		final Node<Char> node = expr.toNodeGraph(TERMINAL);
		final StringMatcher<String, Integer> node = matcher("(abcde)*");
		String next = "";
		for (int i = 0 ; i != 5 ; i++) {
			assertMatch(node, next);
			next += "abcde";
		}
	}
	
	public void testInfiniteRepetitionComposition() throws ParseException {
//		final BuildRegex<Char> expr = Char.lit("ab").rep(0).then(Char.lit("cd").rep(0));
//		final Node<Char> node = expr.toNodeGraph(TERMINAL);
		final StringMatcher<String, Integer> node = matcher("(ab)*(cd)*");
		assertMatch(node, "");
		assertMatch(node, "ab");
		assertMatch(node, "ababab");
		assertMatch(node, "cdcdcd");
		assertMatch(node, "abcd");
		assertMatch(node, "abababcdcdcd");
		assertNoMatch(node, "acd");
		assertNoMatch(node, "bcd");
		assertNoMatch(node, "ac");
		assertNoMatch(node, "ad");
		assertNoMatch(node, "cdab");
	}
	
	public void testSimpleOption() throws ParseException {
//		final BuildRegex<Char> expr = Char.lit("ab").opt();
//		final Node<Char> node = expr.toNodeGraph(TERMINAL);
		final StringMatcher<String, Integer> node = matcher("(ab)?");
		assertMatch(node, "");
		assertMatch(node, "ab");
	}
	
	public void testSimpleOptionComposition() throws ParseException {
//		final BuildRegex<Char> expr = Char.lit("ab").opt().then(Char.lit("cd"));
//		final Node<Char> node = expr.toNodeGraph(TERMINAL);
		final StringMatcher<String, Integer> node = matcher("(ab)?cd");
		assertNoMatch(node, "");
		assertMatch(node, "abcd");
		assertMatch(node, "cd");
		assertNoMatch(node, "ab");
	}
	
	public void testInfiniteSequenceWithOptionComposition() throws ParseException {
//		final BuildRegex<Char> expr = Char.lit("ab").opt().then(Char.lit("cd")).rep(0).then(Char.lit("ef"));
//		final Node<Char> node = expr.toNodeGraph(TERMINAL);
		final StringMatcher<String, Integer> node = matcher("((ab)?cd)*ef");
		assertNoMatch(node, "");
		assertMatch(node, "ef");
		assertNoMatch(node, "abef");
		assertMatch(node, "cdef");
		assertNoMatch(node, "ababab");
		assertNoMatch(node, "abcd");
		assertMatch(node, "cdcdcdcdef");
		assertNoMatch(node, "cdcdcdcd");
		assertMatch(node, "abcdabcdcdcdabcdabcdef");
		assertNoMatch(node, "abcdabcdcdcdabcdabcd");
	}
	
	public void testMultiMatch() throws ParseException {
		final StringMatcher<String, Integer> pat1 = matcher("abce?gh?", 0);
		final StringMatcher<String, Integer> pat2 = matcher("abcf?g", 1);
		final StringMatcher<String, Integer> node = pat1.merge(pat2);
		assertMatches(node, "abcg", 0, 1);
		assertMatches(node, "abceg", 0);
		assertMatches(node, "abcfg", 1);
		assertMatches(node, "abcgh", 0);
	}
	
	public void testOption() throws ParseException {
		final StringMatcher<String, Integer> matcher = matcher("(best|top|worst|bottom)\\s+picks?", 0);
		assertMatches(matcher, "worst picks", 0);
		assertMatches(matcher, "worst");
	}
	
	public void testProblemCases() throws ParseException {
		StringMatcher<String, Integer> pat1 = matcher("([0-9,]*[.]?[0-9]+)", 0), pat2, pat3;
		assertMatches(pat1, "100.00", 0);
		assertMatches(pat1, ".00", 0);
		pat1 = matcher("a*b*c", 1);
		pat2 = matcher("[abc]*c", 2);
		pat3 = pat1.merge(pat2);
		assertMatches(pat3, "aaabc", 1, 2);
		assertMatches(pat3, "acccc", 2);
		pat1 = matcher(".*te.*st1", 1);
		pat2 = matcher(".*TEST2", 2);
		pat3 = pat1.merge(pat2);
		assertMatches(pat3, "abctest1", 1);
		assertMatches(pat3, "abcTEST2", 2);
	}
	
	public void testReplace() throws ParseException {
		assertReplace("(abc)(fgh)", "[1]de[2]", "abcfgh", "abcdefgh");
		assertReplace("(a((b)(c))).*", "([1])([1,1])([1,1,1])([1,1,2])", "abcfgh", "(abc)(bc)(b)(c)");
		assertReplace("abcdef", "[0]g", "abcdef", "abcdefg");
	}

	public void testMerge() throws ParseException {
		StringMatcher<String, Integer> m = matcher("ab", true, 2);
		System.out.println(m.node);
		m = m.merge(matcher("ab", false, 3));
		m = m.merge(matcher("abc", false, 4));
		m = m.merge(matcher("abc", true, 5));
		assertMatches(m, "abc", 4, 5);
		assertMatches(m, "Ab", 2);
		assertMatches(m, "AB", 2);
		assertMatches(m, "aB", 2);
		assertMatches(m, "ab", 2, 3);
		assertMatches(m, "AbC", 5);
		m = matcher("a*b*", true, 2);
		m = m.merge(matcher("a*c*b*", true, 3));
		m = m.merge(matcher("aaa*c*b*", true, 4));
		assertMatches(m, "abc");
		assertMatches(m, "aaAabbbBbccCcCc");
		assertMatches(m, "acb", 3);
		assertMatches(m, "accCccbBbbb", 3);
		assertMatches(m, "a", 2, 3);
		assertMatches(m, "aaaaAaa", 2, 3, 4);
		assertMatches(m, "ab", 2, 3);
		assertMatches(m, "abbBbbBbb", 2, 3);
		assertMatches(m, "aaacb", 3, 4);
		assertMatches(m, "aAaAaAcCcCcCbBBbBb", 3, 4);
		assertMatches(m, "aaaab", 2, 3, 4);
		assertMatches(m, "aAaAaAabBbBbb", 2, 3, 4);
		m = matcher("(abc|def)*", true, 2);
		m = m.merge(matcher("a(bc|def)*", true, 3));
		m = m.merge(matcher("(abc|hij){2}", false, 4));
		assertMatches(m, "abcdefabcabc", 2);
		assertMatches(m, "abcabc", 2, 4);
		assertMatches(m, "abcdef", 2, 3);
		assertMatches(m, "abcdefbc", 3);
		assertMatches(m, "abchij", 4);
		StringReplacer r = new StringReplacer("([Aa])([Bb])", "[2][1]");
		r = r.merge(new StringReplacer("([Aa]{2})([Bb])", "[1][2]"));
		r = r.merge(new StringReplacer("(cat)(dog)", "[1]"));
		assertEquals(Arrays.asList("aab"), r.match("aab"));		
		assertEquals(Arrays.asList("ba"), r.match("ab"));		
		assertEquals(Arrays.asList("cat"), r.match("catdog"));
	}
	
//	public void testSlow() {
//		assertReplace(".*Bloomb.*", "Bloomberg", "aa  Bloomberg 24", "Bloomberg");
//	}
//	
	public void testComplex() throws ParseException {
//		assertReplace("(a|(b)?(c))*", "[1,1][1,2]", "bcc", "bc");
//		assertReplace("(ab)*(([abcd])|(c)?(d))*", "[1][2][2,1][2,2][2,3]", "abcdcdab", "abccd");
		assertReplace("[abc]|(a)|(b)", "[1]", "a", "a");
		assertReplace("([abc]|(a)|(b))*", "[1,1][1,2]", "abcabc", "ab");
		assertReplace("(a+)? ?", "[1]", "aaa", "aaa");
		assertReplace("(a+)? ?(a+)? ?", "[1]", "aaa aaa", "aaa");
		assertReplace("(a+)? ?(a+)? ?(a+)? ?(a+)? ?", "[1]", "aaa aaa aaa aaa", "aaa");
		assertReplace("(a+ )?", "[1]", "aaa ", "aaa ");
		assertReplace("(a+ )?(a+ )?(a+ )?(a+ )?", "[1]", "aaa aaa aaa aaa ", "aaa ");
		assertReplace("([A-Za-z\\-']+ )([A-Za-z\\-']+ )?([A-Za-z\\-']+ )?([A-Za-z\\-']+ )?", "[1]", "abc def ghi jkl ", "abc ");
		assertReplace("(cd)*", "[1]", "cdcdcd", "cd");
		assertReplace("a*(a|b)a*", "[1]", "aaaaaabaaaaa", "b");
		assertReplace("[a]*a[a]*ab", "1", "aaaaab", "1");
		assertReplace("[a-c]*(b)c", "d", "abcabcbc", "d");
		assertReplace("b*|bb*", "", "bbbbb", "");
//		assertReplace("[ac]+!([ab]+)", "[1]", "acabab", "abab"); // currently ! is disabled
		assertReplace("[\na-zA-Z]*(E|A|As).*", "[1]", "abcdEurope\nabcd", "");
//		assertReplace("E*(E)aE*", "[1]", "EEEEEaE", "E"); // this isn't easy to capture using DFAs - dubious should even try to support it
		assertReplace("(a)(b)(cd)*(def)*", "[1][2][3][4]", "abcdcdcdcddefdefdef", "abcddef");
//		assertReplace("((bc)*!((a)|(d)))*", "[1,2,1][1,2,2]", "bcbcadbcad", "ad"); // currently ! is disabled
//		assertReplace("((c)(d))*^([bc]*!((a)|(d)))*", "[1]", "cdcdcd", "cd"); // currently ! is disabled
	}
	public void testStackMap() {
		final StackMap<Integer, Integer> m = new ObjStackMap<Integer, Integer>();
		for (int j = 1 ; j != 6 ; j++) {
			for (int i = 0 ; i != 10 ; i++) {
				m.push(i, i * j);
			}
		}
		for (int j = 1 ; j != 6 ; j++) {
			for (int i = 0 ; i != 10 ; i++) {
				assertEquals(Integer.valueOf((6 - j) * (9 - i)), m.peek(9 - i));
				m.pop(9 - i);
			}
		}
	}
	
//	public void testCharBlock() throws ParseException {
//		StringSeqReplacer r = StringSeqReplacer.build()
//			.add("A", "[abc]*")
//			.add("B", "ab")
//			.add("C", "bc")
//			.match("(A|(B)?(C))*")
//			.replaceWith("[1,1][1,2]")
//			.done();
//		System.out.println(r.node);
//		assertEquals(Arrays.asList("bc"), r.match("bc", "ca").get(0));
//		r = StringSeqReplacer.build()
//		.add("analystname", "a")
//		.add("analystcontact", "b")
//		.add("subtitle", "a")
//		.add("text", "c")
//		.add(".", "[abc]")
//		.match("([analystname][analystcontact])*(.|[subtitle]?[text])*")
//		.replaceWith("[1][2]")
//		.done();
//		System.out.println(r.node);
//		assertMatches(Arrays.asList("bc"), r.match("a", "b", "a", "b", "a", "c", "c", "a", "b"));
//	}
//	
	void assertNoMatches(List<String[]> matches) {
		assertEquals(0, matches.size());
	}
	
	void assertMatches(List<String> expect, List<List<String>> matches) {
		assertEquals(1, matches.size());
		assertEquals(expect, matches.get(0));
	}
	
	void assertMatches(StringMatcher<String, Integer> matcher, String str, Integer ... ids) {
		assertEquals("Failed to match " + str, Arrays.asList(ids), matcher.match(str));
	}
	
	void assertMatch(StringMatcher<String, Integer> matcher, String str) {
		assertTrue("Failed to match " + str, !matcher.match(str).isEmpty());
	}
	
	void assertNoMatch(StringMatcher<String, Integer> matcher, String str) {
		assertFalse("Matched when should have failed " + str, !matcher.match(str).isEmpty());
	}

	void assertReplace(String regexp, String replacement, String input, String expect) throws ParseException {
		final StringReplacer replacer = new StringReplacer(regexp, replacement);
		final List<String> results = replacer.match(input);
		assertEquals("Replace failed", Arrays.asList(expect), results);
	}
	
	private static StringMatcher<String, Integer> matcher(String regexp) throws ParseException {
		return matcher(regexp, false);
	}
	private static StringMatcher<String, Integer> matcher(String regexp, boolean ignoreCase) throws ParseException {
		return matcher(regexp, ignoreCase, 0);
	}
	private static StringMatcher<String, Integer> matcher(String regexp, final int i) throws ParseException {
		return matcher(regexp, false, i);
	}
	private static StringMatcher<String, Integer> matcher(String regexp, boolean ignoreCase, final int i) throws ParseException {
		return new StringMatcher<String, Integer>(regexp, ignoreCase, new MatchAction<String, Integer>() {
			@Override
			public Integer matched(String input, Found captured) {
				return i;
			}			
		});
	}
	
}
