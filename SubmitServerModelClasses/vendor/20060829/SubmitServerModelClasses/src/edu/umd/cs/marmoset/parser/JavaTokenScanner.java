/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on April 7, 2005
 */
package edu.umd.cs.marmoset.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Token scanner for Java code highlighting.
 * 
 * @author David Hovemeyer
 */
public class JavaTokenScanner implements TokenScanner {
	private static final Map<Character, TokenType> singleCharacterTokenMap = new HashMap<Character, TokenType>();
	static {
		singleCharacterTokenMap.put(new Character(','), TokenType.PUNCTUATION);
		singleCharacterTokenMap.put(new Character(':'), TokenType.PUNCTUATION);
		singleCharacterTokenMap.put(new Character('?'), TokenType.PUNCTUATION);
		singleCharacterTokenMap.put(new Character(';'), TokenType.PUNCTUATION);
		singleCharacterTokenMap.put(new Character('~'), TokenType.OPERATOR);
		singleCharacterTokenMap.put(new Character('('), TokenType.PAREN);
		singleCharacterTokenMap.put(new Character(')'), TokenType.PAREN);
		singleCharacterTokenMap.put(new Character('['), TokenType.PAREN);
		singleCharacterTokenMap.put(new Character(']'), TokenType.PAREN);
		singleCharacterTokenMap.put(new Character('{'), TokenType.PAREN);
		singleCharacterTokenMap.put(new Character('}'), TokenType.PAREN);
		singleCharacterTokenMap.put(new Character('\n'), TokenType.NEWLINE);
	}
	
	private static final Set<String> keywordSet = new HashSet<String>();
	static {
		keywordSet.add("abstract");
		keywordSet.add("boolean");
		keywordSet.add("break");
		keywordSet.add("byte");
		keywordSet.add("case");
		keywordSet.add("catch");
		keywordSet.add("char");
		keywordSet.add("class");
		keywordSet.add("const");
		keywordSet.add("continue");
		keywordSet.add("default");
		keywordSet.add("do");
		keywordSet.add("double");
		keywordSet.add("else");
		keywordSet.add("extends");
		keywordSet.add("final");
		keywordSet.add("finally");
		keywordSet.add("float");
		keywordSet.add("for");
		keywordSet.add("goto");
		keywordSet.add("if");
		keywordSet.add("implements");
		keywordSet.add("import");
		keywordSet.add("instanceof");
		keywordSet.add("int");
		keywordSet.add("interface");
		keywordSet.add("long");
		keywordSet.add("native");
		keywordSet.add("new");
		keywordSet.add("package");
		keywordSet.add("private");
		keywordSet.add("protected");
		keywordSet.add("public");
		keywordSet.add("return");
		keywordSet.add("short");
		keywordSet.add("static");
		keywordSet.add("strictfp");
		keywordSet.add("super");
		keywordSet.add("switch");
		keywordSet.add("synchronized");
		keywordSet.add("this");
		keywordSet.add("throw");
		keywordSet.add("throws");
		keywordSet.add("transient");
		keywordSet.add("try");
		keywordSet.add("void");
		keywordSet.add("volatile");
		keywordSet.add("while");
	}
	
	private static final Set<String> specialLiteralSet = new HashSet<String>();
	static {
		specialLiteralSet.add("true");
		specialLiteralSet.add("false");
		specialLiteralSet.add("null");
	}
	
	private static final Map<String, Integer> timesPermittedMap = new HashMap<String, Integer>();
	static {
		timesPermittedMap.put("+", new Integer(2));
		timesPermittedMap.put("-", new Integer(2));
		timesPermittedMap.put("=", new Integer(2));
		timesPermittedMap.put("<", new Integer(3));
		timesPermittedMap.put(">", new Integer(3));
	}
	
	private StringBuffer lexeme;
	private int nextChar = Integer.MAX_VALUE;
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.submitServer.TokenScanner#scan(java.io.Reader)
	 */
	public Token scan(Reader reader) throws IOException {
		lexeme = new StringBuffer();
		TokenType type = readToken(reader);
		if (type == null)
			return null;
		return new Token(type, lexeme.toString());
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.submitServer.TokenScanner#getLexeme()
	 */
	public String getLexeme() {
		return lexeme.toString();
	}
	
	private int read(Reader reader) throws IOException {
		int result;
		if (nextChar != Integer.MAX_VALUE) {
			result = nextChar;
			nextChar = Integer.MAX_VALUE;
		} else {
			result = reader.read();
		}
		return result;
	}
	
	private void putback(int c) {
		if (nextChar != Integer.MAX_VALUE)
			throw new IllegalStateException();
		nextChar = c;
	}
	
	private void consume(char c) {
		lexeme.append(c);
	}

	private TokenType readToken(Reader reader) throws IOException {
		int first = read(reader);
		if (first < 0) {
			return null;
		}
		consume((char)first);

		TokenType currentTokenType = singleCharacterTokenMap.get(new Character((char) first));
		if (currentTokenType != null) {
			return currentTokenType;
		}
		
		if (Character.isJavaIdentifierStart((char) first)) {
			return scanIdentifierOrKeyword((char) first, reader);
		} else if (first == '.') {
			// Requires some special handling because it may be the
			// beginning of a floating point literal
			int next = read(reader);
			if (next >= 0 && isJavaDigit((char) next)) {
				consume((char) next);
				return scanNumericLiteral(reader, (char) first, N_POINT);
			} else {
				putback(next);
				return TokenType.PUNCTUATION;
			}
		} else if (isJavaSpace(first)) {
			int next;
			while ((next = read(reader)) >= 0) {
				if (isJavaSpace(next)) {
					consume((char)next);
				} else {
					putback(next);
					break;
				}
			}
			return TokenType.HORIZONTAL_WHITESPACE;
		} else if (first == '/') {
			int next = read(reader);
			if (next == '/') {
				consume((char)next);
				return scanSingleLineComment(reader);
			} else if (next == '*') {
				consume((char)next);
				return scanMultiLineComment(reader);
			} else {
				putback(next);
				return scanOperator(reader, first);
			}
		} else {
			switch (first) {
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				return scanNumericLiteral(reader, (char) first, N_START);
			case '!': case '~':
			case '+': case '-':
			case '*': case '%':
			case '|': case '&': case '^':
			case '>': case '<': case '=':
				return scanOperator(reader, first);
			case '\'':
				return scanStringLiteral(reader, '\'');
			case '"':
				return scanStringLiteral(reader, '"');
			default:
				return TokenType.UNKNOWN;
			}
		}
	}

	private TokenType scanSingleLineComment(Reader reader) throws IOException {
		int c;
		while ((c = read(reader)) >= 0) {
			if (c == '\n') {
				putback(c);
				break;
			} else {
				consume((char)c);
			}
		}
		
		return TokenType.SINGLE_LINE_COMMENT;
	}
	
	private static final int M_SCAN = 0;
	private static final int M_STAR = 1;

	private TokenType scanMultiLineComment(Reader reader) throws IOException {
		int c = 0;
		boolean done = false;
		int state = M_SCAN;
		while (!done && (c = read(reader)) >= 0) {
			switch (state) {
			case M_SCAN:
				if (c == '*') {
					consume(((char)c));
					state = M_STAR;
				} else {
					consume((char)c);
				}
				break;
				
			case M_STAR:
				if (c == '/') {
					consume((char)c);
					done = true;
				} else {
					consume((char)c);
					state = (c == '*') ? M_STAR : M_SCAN;
				}
				break;
				
			default:
				throw new IllegalStateException();
			}
		}
		
		if (lexeme.length() == 0)
			return null;
		else
			return TokenType.MULTI_LINE_COMMENT;
	}

	private boolean isJavaSpace(int c) {
		return c == ' ' || c == '\t' || c == '\f'; 
	}

	private TokenType scanIdentifierOrKeyword(char first, Reader reader) throws IOException {
		// FIXME: handle unicode escapes somehow
		
		int c;
		while ((c = read(reader)) >= 0) {
			if (Character.isJavaIdentifierPart((char) c)) {
				consume((char)c);
			} else {
				putback(c);
				break;
			}
		}
		
		String value = lexeme.toString();
		if (keywordSet.contains(value))
			return TokenType.KEYWORD;
		else if (specialLiteralSet.contains(value))
			return TokenType.LITERAL;
		else
			return TokenType.IDENTIFIER;
	}
	
	private static final int N_START = 0;
	private static final int N_POINT = 1;
	private static final int N_HEX = 2;
	private static final int N_EXPONENT = 3;
	private static final int N_EXPONENT_SIGN = 4;

	private TokenType scanNumericLiteral(Reader reader, char first, int startState) throws IOException {
		int state = startState;
		boolean done = false;
		
		int c;
		while (!done && (c = read(reader)) >= 0) {
			switch (state) {
			case N_START:
				if (c == '.') {
					consume((char)c);
					state = N_POINT;
				} else if (first == '0' && lexeme.length() == 1 && isHexSignifier(c)) {
					consume((char)c);
					state = N_HEX;
				} else if (isJavaDigit(c)) {
					consume((char)c);
				} else if (isExponentSignifier(c)) {
					consume((char)c);
					state = N_EXPONENT;
				} else if (isIntTypeSuffix(c) || isFloatTypeSuffix(c)) {
					consume((char)c);
					done = true;
				} else {
					putback(c);
					done = true;
				}
				break;
				
			case N_POINT:
				if (isExponentSignifier(c)) {
					consume((char)c);
					state = N_EXPONENT;
				} else if (isFloatTypeSuffix(c)) {
					// FIXME: should exclude things like ".D"
					consume((char)c);
					done = true;
				} else {
					putback(c);
					done = true;
				}
				break;
				
			case N_HEX:
				if (isHexDigit(c)) {
					consume((char)c);
				} else if (isIntTypeSuffix(c)) {
					// FIXME: should exclude things like "0xL"
					consume((char)c);
					done = true;
				} else {
					putback(c);
					done = true;
				}
				break;
				
			case N_EXPONENT:
				if (c == '+' || c == '-' || isJavaDigit(c)) {
					consume((char)c);
					state = N_EXPONENT_SIGN;
				} else {
					// Invalid token?
					putback(c);
					done = true;
				}
				break;
				
			case N_EXPONENT_SIGN:
				if (isJavaDigit(c)) {
					consume((char)c);
				} else if (isFloatTypeSuffix(c)) {
					consume((char)c);
					done = true;
				} else {
					putback(c);
					done = true;
				}
				break;
				
			default:
				throw new IllegalStateException();
			}
		}
		
		return TokenType.LITERAL;
	}

	private boolean isFloatTypeSuffix(int c) {
		return c == 'f' || c == 'F' || c == 'd' || c == 'D';
	}

	private boolean isHexDigit(int c) {
		return isJavaDigit(c) || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
	}

	private boolean isExponentSignifier(int c) {
		return c == 'e' || c == 'E';
	}

	private boolean isIntTypeSuffix(int c) {
		return c == 'L' || c == 'l'; 
	}

	private boolean isJavaDigit(int c) {
		return c >= '0' && c <= '9';
	}

	private boolean isHexSignifier(int c) {
		return c == 'x' || c == 'X';
	}
	
	private static final Integer ONE = new Integer(1);

	/*
	 * This is perhaps overly clever.
	 * But, it should recognize exactly the set of Java operators.
	 * (Except '?' and ':', which we treat as punctuation.)
	 */
	private TokenType scanOperator(Reader reader, int first) throws IOException {
		Integer timesPermitted = timesPermittedMap.get(new Character((char)first));
		if (timesPermitted == null) {
			timesPermitted = ONE;
		}
		
		int c;
		while ((c = read(reader)) >= 0) {
			if (c == first) {
				if (lexeme.length() < timesPermitted.intValue()) {
					consume((char)c);
				} else {
					putback(c);
					break;
				}
			} else {
				if (mayEndWithEq(first) && c == '=') {
					consume((char)c);
				} else {
					putback(c);
				}
				break;
			}
		}
		
		return TokenType.OPERATOR;
	}

	private boolean mayEndWithEq(int first) {
		if (first == '~')
			return false;
		if ((first == '+' || first == '-') && lexeme.length() > 1)
			// Don't allow things like "++="
			return false;
		return true;
	}
	
	private static final int S_NORMAL = 0;
	private static final int S_BACKSLASH = 1;
	private static final int S_UNICODE_START = 2;
	private static final int S_UNICODE = 3;
	
	private TokenType scanStringLiteral(Reader reader, char terminator) throws IOException {
		int state = S_NORMAL;
		boolean done = false;
		int c;
		while (!done && (c = read(reader)) >= 0) {
			switch (state) {
			case S_NORMAL:
				if (c == terminator) {
					consume((char)c);
					done = true;
				} else if (c == '\\') {
					consume((char)c);
					state = S_BACKSLASH;
				} else {
					consume((char)c);
				}
				break;
				
			case S_BACKSLASH:
				if (c == 'u') {
					consume((char)c);
					state = S_UNICODE_START;
				} else {
					consume((char)c);
					state = S_NORMAL;
				}
				break;
				
			case S_UNICODE_START:
				if (c == 'u') {
					consume((char)c);
				} else if (isHexDigit(c)) {
					consume((char)c);
					state = S_UNICODE;
				} else {
					consume((char)c);
					state = S_NORMAL;
				}
				break;
				
			case S_UNICODE:
				if (isHexDigit(c)) {
					consume((char)c);
				} else {
					putback(c);
					state = S_NORMAL;
				}
				break;
				
			default:
				throw new IllegalStateException();
			}
		}
		
		return TokenType.STRING_LITERAL;
	}
	
	public static void main(String[] args) throws Exception {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(System.in, Charset.forName("UTF-8")));
			TokenScanner scanner = new JavaTokenScanner();
			Token token;
			while ((token = scanner.scan(reader)) != null) {
				System.out.println(token);
			}
		} finally {
			if (reader != null) reader.close();
		}
	}
}
