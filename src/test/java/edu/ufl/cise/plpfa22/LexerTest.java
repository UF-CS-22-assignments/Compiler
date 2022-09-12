/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.plpfa22.IToken.Kind;

class LexerTest {

	/*** Useful functions ***/
	ILexer getLexer(String input) {
		return CompilerComponentFactory.getLexer(input);
	}

	// makes it easy to turn output on and off (and less typing than
	// System.out.println)
	static final boolean VERBOSE = true;

	void show(Object obj) {
		if (VERBOSE) {
			System.out.println(obj);
		}
	}

	// check that this token has the expected text.
	void checkText(IToken t, String expectedText) {
		assertArrayEquals(expectedText.toCharArray(), t.getText());
	}

	// check that this token has the expected location.
	void checkLocation(IToken t, int expectedLine, int expectedColumn) {
		assertEquals(new IToken.SourceLocation(expectedLine, expectedColumn), t.getSourceLocation());
	}

	// check that this token has the expected kind
	void checkToken(IToken t, Kind expectedKind) {
		assertEquals(expectedKind, t.getKind());
	}

	// check that the token has the expected kind and position
	void checkToken(IToken t, Kind expectedKind, int expectedLine, int expectedColumn) {
		assertEquals(expectedKind, t.getKind());
		this.checkLocation(t, expectedLine, expectedColumn);
	}

	// check if the token has the expected kind, location, and text.
	void checkToken(IToken t, Kind expectedKind, int expectedLine, int expectedColumn, String expectedText) {
		assertEquals(expectedKind, t.getKind());
		this.checkLocation(t, expectedLine, expectedColumn);
		assertArrayEquals(expectedText.toCharArray(), t.getText());
	}

	// check that this token is an IDENT and has the expected name
	void checkIdent(IToken t, String expectedName) {
		assertEquals(Kind.IDENT, t.getKind());
		assertEquals(expectedName, String.valueOf(t.getText()));
	}

	// check that this token is an IDENT, has the expected name, and has the
	// expected position
	void checkIdent(IToken t, String expectedName, int expectedLine, int expectedColumn) {
		checkIdent(t, expectedName);
		this.checkLocation(t, expectedLine, expectedColumn);
	}

	// check that this token is a boolean_lit, has the expected name and position
	void checkBoolean(IToken t, boolean expectedBooleanValue, int expectedLine, int expectedColumn) {
		assertEquals(Kind.BOOLEAN_LIT, t.getKind());
		if (expectedBooleanValue) {
			this.checkText(t, "TRUE");
		} else {
			this.checkText(t, "FALSE");
		}
		assertEquals(expectedBooleanValue, t.getBooleanValue());
		this.checkLocation(t, expectedLine, expectedColumn);
	}

	// check that this token is an NUM_LIT with expected int value
	void checkInt(IToken t, int expectedValue) {
		assertEquals(Kind.NUM_LIT, t.getKind());
		assertEquals(expectedValue, t.getIntValue());
	}

	// check that this token is an NUM_LIT with expected int value and position
	void checkInt(IToken t, int expectedValue, int expectedLine, int expectedColumn) {
		checkInt(t, expectedValue);
		this.checkLocation(t, expectedLine, expectedColumn);
	}

	void checkStringValue(IToken t, String expectedString) {
		assertEquals(expectedString, t.getStringValue());
	}

	// check that this token is a string and has expected value and position
	void checkString(IToken t, String expectText, String expectStringValue, int expectedLine, int expectedColumn) {
		this.checkToken(t, Kind.STRING_LIT);
		this.checkText(t, expectText);
		this.checkStringValue(t, expectStringValue);
		this.checkLocation(t, expectedLine, expectedColumn);
	}

	// check that this token is the EOF token
	void checkEOF(IToken t) {
		checkToken(t, Kind.EOF);
	}

	/*** Tests ****/

	// The lexer should add an EOF token to the end.
	@Test
	void testEmpty() throws LexicalException {
		String input = "";
		show(input);
		ILexer lexer = getLexer(input);
		show(lexer);
		checkEOF(lexer.next());
	}

	// A couple of single character tokens
	@Test
	void testSingleChar0() throws LexicalException {
		String input = """
				+
				-
				""";
		show(input);
		ILexer lexer = getLexer(input);
		// TODO: test the text of the token
		checkToken(lexer.next(), Kind.PLUS, 1, 1, "+");
		checkToken(lexer.next(), Kind.MINUS, 2, 1, "-");
		checkEOF(lexer.next());
	}

	@Test
	void testSingleChar1() throws LexicalException {
		String input = """
				+- *
				;;;
				(() )  #/ /
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.PLUS, 1, 1, "+");
		checkToken(lexer.next(), Kind.MINUS, 1, 2, "-");
		checkToken(lexer.next(), Kind.TIMES, 1, 4, "*");
		checkToken(lexer.next(), Kind.SEMI, 2, 1, ";");
		checkToken(lexer.next(), Kind.SEMI, 2, 2, ";");
		checkToken(lexer.next(), Kind.SEMI, 2, 3, ";");
		checkToken(lexer.next(), Kind.LPAREN, 3, 1, "(");
		checkToken(lexer.next(), Kind.LPAREN, 3, 2, "(");
		checkToken(lexer.next(), Kind.RPAREN, 3, 3, ")");
		checkToken(lexer.next(), Kind.RPAREN, 3, 5, ")");
		checkToken(lexer.next(), Kind.NEQ, 3, 8, "#");
		checkToken(lexer.next(), Kind.DIV, 3, 9, "/");
		checkToken(lexer.next(), Kind.DIV, 3, 11, "/");

		checkEOF(lexer.next());
	}

	@Test
	void testSingleChar2() throws LexicalException {
		String input = """
				.,;()+-*/%?!:==#<<=>>=
				""";
		show(input);
		ILexer lexer = getLexer(input);
		// TODO: test the text of the token
		checkToken(lexer.next(), Kind.DOT, 1, 1, ".");
		checkToken(lexer.next(), Kind.COMMA, 1, 2, ",");
		checkToken(lexer.next(), Kind.SEMI, 1, 3, ";");
		checkToken(lexer.next(), Kind.LPAREN, 1, 4, "(");
		checkToken(lexer.next(), Kind.RPAREN, 1, 5, ")");
		checkToken(lexer.next(), Kind.PLUS, 1, 6, "+");
		checkToken(lexer.next(), Kind.MINUS, 1, 7, "-");
		checkToken(lexer.next(), Kind.TIMES, 1, 8, "*");
		checkToken(lexer.next(), Kind.DIV, 1, 9, "/");
		checkToken(lexer.next(), Kind.MOD, 1, 10, "%");
		checkToken(lexer.next(), Kind.QUESTION, 1, 11, "?");
		checkToken(lexer.next(), Kind.BANG, 1, 12, "!");
		checkToken(lexer.next(), Kind.ASSIGN, 1, 13, ":=");
		checkToken(lexer.next(), Kind.EQ, 1, 15, "=");
		checkToken(lexer.next(), Kind.NEQ, 1, 16, "#");
		checkToken(lexer.next(), Kind.LT, 1, 17, "<");
		checkToken(lexer.next(), Kind.LE, 1, 18, "<=");
		checkToken(lexer.next(), Kind.GT, 1, 20, ">");
		checkToken(lexer.next(), Kind.GE, 1, 21, ">=");

		checkEOF(lexer.next());
	}

	@Test
	void testSingleInt() throws LexicalException {
		String input = "1";
		show(input);
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(), 1);
		checkEOF(lexer.next());
	}

	@Test
	void testSingleLineInt() throws LexicalException {
		String input = "1023456789";
		show(input);
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(), 1023456789);
		checkEOF(lexer.next());
	}

	@Test
	void testNewLineInt() throws LexicalException {
		String input = """
				1
				0
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(), 1);
		checkInt(lexer.next(), 0);
		checkEOF(lexer.next());
	}

	// comments should be skipped
	@Test
	void testComment0() throws LexicalException {
		// Note that the quotes around "This is a string" are passed to the lexer.
		String input = """
				"This is a string"
				// this is a comment
				*
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.STRING_LIT, 1, 1);
		checkToken(lexer.next(), Kind.TIMES, 3, 1);
		checkEOF(lexer.next());
	}

	// Several identifiers to test positions
	@Test
	public void testIdent0Comment() throws LexicalException {
		// notice that the characters before ghi is a \t and a space ' '
		String input = """
				abc
				// haha
				// hello! how are you?
				// I'm fine thank you and you?
				  def
					 ghi

				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "abc", 1, 1);
		checkIdent(lexer.next(), "def", 5, 3);
		checkIdent(lexer.next(), "ghi", 6, 3);
		checkEOF(lexer.next());
	}

	// Example for testing input with an illegal character
	@Test
	void testError0() throws LexicalException {
		String input = """
				abc
				@
				""";
		show(input);
		ILexer lexer = getLexer(input);
		// this check should succeed
		checkIdent(lexer.next(), "abc");
		// this is expected to throw an exception since @ is not a legal
		// character unless it is part of a string or comment
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
	}

	// Several identifiers to test positions
	@Test
	public void testIdent0() throws LexicalException {
		// notice that the characters before ghi is 5 spaces ' '
		String input = """
				abc
				  def
				     ghi

				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "abc", 1, 1);
		checkIdent(lexer.next(), "def", 2, 3);
		checkIdent(lexer.next(), "ghi", 3, 6);
		checkEOF(lexer.next());
	}

	@Test
	public void testIdenAndReserved() throws LexicalException {
		String input = """
				AbcTRUE
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "AbcTRUE", 1, 1);
		checkEOF(lexer.next());
	}

	@Test
	public void testReservedWords() throws LexicalException {
		String input = """
				TRUE CONST
				PROCEDURE
				DO    CALL
				FALSE
				a123 456b
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkBoolean(lexer.next(), true, 1, 1);
		checkToken(lexer.next(), Kind.KW_CONST, 1, 6);
		checkToken(lexer.next(), Kind.KW_PROCEDURE, 2, 1);
		checkToken(lexer.next(), Kind.KW_DO, 3, 1);
		checkToken(lexer.next(), Kind.KW_CALL, 3, 7);
		checkBoolean(lexer.next(), false, 4, 1);
		checkIdent(lexer.next(), "a123", 5, 1);
		checkInt(lexer.next(), 456, 5, 6);
		checkIdent(lexer.next(), "b", 5, 9);
		checkEOF(lexer.next());
	}

	@Test
	public void testIdenInt() throws LexicalException {
		String input = """
				a123 456b
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "a123", 1, 1);
		checkInt(lexer.next(), 456, 1, 6);
		checkIdent(lexer.next(), "b", 1, 9);
		checkEOF(lexer.next());
	}

	// Example showing how to handle number that are too big.
	@Test
	public void testIntTooBig() throws LexicalException {
		String input = """
				42
				99999999999999999999999999999999999999999999999999999999999999999999999
				""";
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(), 42);
		Exception e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	@Test
	public void testEscapeSequences0() throws LexicalException {
		String input = "\"\\b \\t \\n \\f \\r \"";
		show(input);
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		String expectedStringValue = "\b \t \n \f \r ";
		assertEquals(expectedStringValue, val);
		String text = String.valueOf(t.getText());
		String expectedText = "\"\\b \\t \\n \\f \\r \"";
		assertEquals(expectedText, text);
	}

	@Test
	public void testEscapeSequences1() throws LexicalException {
		String input = "   \" ...  \\\"  \\\'  \\\\  \"";
		show(input);
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		String expectedStringValue = " ...  \"  \'  \\  ";
		assertEquals(expectedStringValue, val);
		String text = String.valueOf(t.getText());
		String expectedText = "\" ...  \\\"  \\\'  \\\\  \""; // almost the same as input, but white space is omitted
		assertEquals(expectedText, text);
	}

	@Test
	public void testSimpleString() throws LexicalException {
		String input = """
				"string"
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkString(lexer.next(), "\"string\"", "string", 1, 1);
		checkEOF(lexer.next());
	}

	@Test
	public void testStringWithEscapeSequences() throws LexicalException {
		String input = """
				"Escape Sequences\\t"
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkString(lexer.next(), "\"Escape Sequences\\t\"", "Escape Sequences\t", 1, 1);
		checkEOF(lexer.next());
	}

	@Test
	public void testStringWithNewLine() throws LexicalException {
		String input = """
				"ha
				halo\\nha"
				abc
				""";

		show(input);
		ILexer lexer = getLexer(input);
		checkString(lexer.next(), "\"ha\nhalo\\nha\"", "ha\nhalo\nha", 1, 1);
		checkIdent(lexer.next(), "abc", 3, 1);
		checkEOF(lexer.next());
	}

	@Test
	public void testSimpleGetStringValue() throws LexicalException {
		String input = """
				"a b c"
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkString(lexer.next(), "\"a b c\"", "a b c", 1, 1);
	}

	@Test
	public void testGetStringValue() throws LexicalException {
		String input = """
				"a b\\nc"
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkString(lexer.next(), "\"a b\\nc\"", "a b\nc", 1, 1);
	}
}
