/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on April 7, 2005
 */
package edu.umd.cs.marmoset.parser;

/**
 * Source code token types.
 * 
 * @see edu.umd.cs.submitServer.Token
 * @see edu.umd.cs.submitServer.TokenScanner
 * @author David Hovemeyer
 */
public class TokenType {
	private String name;
	
	private TokenType(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
	public static final TokenType KEYWORD = new TokenType("KEYWORD");
	public static final TokenType IDENTIFIER = new TokenType("IDENTIFIER");
	public static final TokenType SINGLE_LINE_COMMENT = new TokenType("SINGLE_LINE_COMMENT");
	public static final TokenType MULTI_LINE_COMMENT = new TokenType("MULTI_LINE_COMMENT");
	public static final TokenType STRING_LITERAL = new TokenType("STRING_LITERAL");
	public static final TokenType LITERAL = new TokenType("LITERAL");
	public static final TokenType OPERATOR = new TokenType("OPERATOR");
	public static final TokenType PUNCTUATION = new TokenType("PUNCTUATION");
	public static final TokenType PAREN = new TokenType("PAREN");
	public static final TokenType HORIZONTAL_WHITESPACE = new TokenType("HORIZONTAL_WHITESPACE");
	public static final TokenType NEWLINE = new TokenType("NEWLINE");
	public static final TokenType UNKNOWN  = new TokenType("UNKNOWN");
}
