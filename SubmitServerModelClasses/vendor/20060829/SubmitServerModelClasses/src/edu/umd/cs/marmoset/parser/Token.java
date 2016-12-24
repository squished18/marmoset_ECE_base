/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on April 12, 2005
 */
package edu.umd.cs.marmoset.parser;

/**
 * A single token.
 * 
 * @see TokenScanner
 * @author David Hovemeyer
 */
public class Token {
	private TokenType type;
	private String lexeme;

	/**
	 * Constructor.
	 * 
	 * @param type   the token type
	 * @param lexeme the lexeme (token text)
	 */
	public Token(TokenType type, String lexeme) {
		this.type = type;
		this.lexeme = lexeme;
	}
	
	/**
	 * Get the token type.
	 */
	public TokenType getType() {
		return type;
	}

	/**
	 * Get the lexeme (token text).
	 */
	public String getLexeme() {
		return lexeme;
	}
	
	public String toString() {
		return type + ": " + lexeme;
	}
}
