/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on April 8, 2005
 */
package edu.umd.cs.marmoset.parser;

import java.io.IOException;
import java.io.Reader;

/**
 * TokenScanner which just returns lines of text,
 * classified as TokenType.UNKNOWN.  This is useful for just
 * displaying plain text files.
 * 
 * @author David Hovemeyer
 */
public class PlainTextTokenScanner implements TokenScanner {
	private String lexeme;
	private boolean needNewline;
	// added by the Script Ninja
	private boolean added_new_line_at_end_of_file = false;

	public Token scan(Reader reader) throws IOException {
		if (needNewline) {
			lexeme = "\n";
			needNewline = false;
			return new Token(TokenType.NEWLINE, "\n");
		}
		
		lexeme = null;
		
		StringBuffer line = new StringBuffer();
		for (;;) {
			
			if (added_new_line_at_end_of_file)
				return null;	
			int c = reader.read();
			if (c < 0) {
				// added by the Script Ninja
				c = '\n';
				added_new_line_at_end_of_file=true;	
			}
			if (c == '\n')
				break;
			line.append((char) c);
		}
		lexeme = line.toString();
		needNewline = true;
		return new Token(TokenType.UNKNOWN, lexeme);
	}

	public String getLexeme() {
		return lexeme;
	}

}
