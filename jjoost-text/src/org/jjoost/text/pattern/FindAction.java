package org.jjoost.text.pattern;

public class FindAction {

	static enum Type {
		TERMINATE, 
		TERMINATE_AFTER_THIS_PREFIX, 
		CONTINUE_ALL, 
		SKIP_MATCHED_CHARS_NOW,
		// same as SKIP_MATCHED_CHARS_NOW, except finishes matching any that match the same prefix of chars as the match just found (i.e. any matches starting at the same offset as the current match)
		SKIP_MATCHED_CHARS_AFTER_THIS_PREFIX,
		SKIP_N_CHARS_NOW,
		SKIP_N_CHARS_AFTER_THIS_PREFIX
		;
	}

	private static final FindAction TERMINATE = new FindAction(Type.TERMINATE, -1);
	private static final FindAction TERMINATE_AFTER_THIS_PREFIX = new FindAction(Type.TERMINATE_AFTER_THIS_PREFIX, -1);
	private static final FindAction CONTINUE_ALL = new FindAction(Type.CONTINUE_ALL, -1);
	private static final FindAction SKIP_MATCHED_CHARS_NOW = new FindAction(Type.SKIP_MATCHED_CHARS_NOW, -1);
	private static final FindAction SKIP_MATCHED_CHARS_AFTER_THIS_PREFIX = new FindAction(Type.SKIP_MATCHED_CHARS_AFTER_THIS_PREFIX, -1);
	
	public static FindAction terminate() { return TERMINATE; }
	public static FindAction terminateAfterThisPrefix() { return TERMINATE_AFTER_THIS_PREFIX; }
	public static FindAction continueAll() { return CONTINUE_ALL; }
	public static FindAction skipMatchedCharsNow() { return SKIP_MATCHED_CHARS_NOW; }
	public static FindAction skipMatchedCharsAfterThisPrefix() { return SKIP_MATCHED_CHARS_AFTER_THIS_PREFIX; }
	public static FindAction skipCharsNow(int chars) { return new FindAction(Type.SKIP_N_CHARS_NOW, chars); }
	public static FindAction skipCharsAfterThisPrefix(int chars) { return new FindAction(Type.SKIP_N_CHARS_AFTER_THIS_PREFIX, chars); }
	
	final Type type;
	final int skipChars;
	
	private FindAction(Type type, int skipChars) {
		super();
		this.type = type;
		this.skipChars = skipChars;
	}

}
