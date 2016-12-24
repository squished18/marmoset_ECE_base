/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on April 6, 2005
 */
package edu.umd.cs.submitServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.codeCoverage.Cond;
import edu.umd.cs.marmoset.codeCoverage.CoverageStats;
import edu.umd.cs.marmoset.codeCoverage.FileWithCoverage;
import edu.umd.cs.marmoset.codeCoverage.Line;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.parser.JavaTokenScanner;
import edu.umd.cs.marmoset.parser.PlainTextTokenScanner;
import edu.umd.cs.marmoset.parser.Token;
import edu.umd.cs.marmoset.parser.TokenScanner;
import edu.umd.cs.marmoset.parser.TokenType;

/**
 * Display source code as HTML.
 * 
 * @author David Hovemeyer
 */
public class DisplaySourceCodeAsHTML {
	private static final Entity ENTITY_NBSP = new Entity("&nbsp;");
	private static final Entity ENTITY_LT = new Entity("&lt;");
	private static final Entity ENTITY_GT = new Entity("&gt;");
	private static final Entity ENTITY_AMP = new Entity("&amp;");
	
	private static final int DEFAULT_TAB_WIDTH = 4;
	
	private static class HighlightRange implements Comparable {
		int startLine, numLines;
		String style;
		int count;
		
		HighlightRange(int startLine, int numLines, String style, int count) {
			this.startLine = startLine;
			this.numLines = numLines;
			this.style = style;
			this.count = count;
		}

		public int compareTo(Object obj) {
			HighlightRange other = (HighlightRange) obj;
			return this.startLine - other.startLine;
		}
	}

	private static class Entity {
		String value;
		Entity(String value) {
			this.value = value;
		}
	}
	
	private BufferedReader reader;
	private TokenScanner scanner;
	protected PrintStream out;
	private int tabWidth = DEFAULT_TAB_WIDTH;
	private Map<Integer, HighlightRange> highlightStartMap = new HashMap<Integer, HighlightRange>();
	private Map<Integer, HighlightRange> highlightEndMap = new HashMap<Integer, HighlightRange>();
	private Map<TokenType, String> tokenStyleMap = new HashMap<TokenType, String>();
	private int displayStart, displayEnd;
	
	private int lineNo;
	private int col;
	private HighlightRange currentHighlightRange;
	private boolean anchoredLine;
	private int highlightCount;
	
    protected FileWithCoverage fileWithCoverage;
    /**
     * Sets the FileWithCoverage for this source file.  The coverage results will be used
     * to markup the source with additional information.
     * 
     * @param fileWithCoverage The code coverage results associated with this source file
     *      to be used when displaying the source file. 
     */
    public void setFileWithCoverage(FileWithCoverage fileWithCoverage) {
        this.fileWithCoverage = fileWithCoverage;
    }
	/**
	 * Constructor.
	 */
	public DisplaySourceCodeAsHTML() {
	}

	/**
	 * Set the InputStream to read the source code from.
	 * 
	 * @param inputStream the InputStream
	 */
	public void setInputStream(InputStream inputStream) {
		reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
	}
	
	/**
	 * Close the input stream.
	 * @throws IOException
	 */
	public void close()
	throws IOException
	{
		reader.close();
	}
	
	/**
	 * Set the PrintStream to write HTML output.
	 * 
	 * @param outputStream the PrintStream
	 */
	public void setOutputStream(PrintStream outputStream) {
		this.out = outputStream;
	}
	
	/**
	 * Set the tab width.
	 * Defaults to 4 if not set explicitly.
	 * 
	 * @param tabWidth the tab width
	 */
	public void setTabWidth(int tabWidth) {
		this.tabWidth = tabWidth;
	}
	
	/**
	 * Add a range of lines to be highlighted.
	 * Lines are numbered starting at 1 (which is the first line
	 * in the file).
	 * 
	 * @param startLine first line to highlight
	 * @param numLines  number of lines to highlight
	 * @param style     CSS style to use for the highlight
	 */
	public void addHighlightRange(int startLine, int numLines, String style) {
		HighlightRange highlight = new HighlightRange(startLine, numLines, style, highlightCount++);
		highlightStartMap.put(new Integer(startLine), highlight);
		highlightEndMap.put(new Integer(startLine + numLines), highlight);
	}

	/**
	 * Set a range of lines of the file to display.
	 * 
	 * @param displayStart first line to display (inclusive)
	 * @param displayEnd   last line to display (exclusive)
	 */
	public void setDisplayRange(int displayStart, int displayEnd) {
		this.displayStart = displayStart;
		this.displayEnd = displayEnd;
	}
	
	/**
	 * Set the token scanner to use for dividing the source code
	 * into lexical tokens.  For example, a JavaTokenScanner
	 * is useful for displaying Java source code.
	 * 
	 * @param scanner the TokenScanner
	 */
	public void setTokenScanner(TokenScanner scanner) {
		this.scanner = scanner;
	}
	
	/**
	 * Set a CSS class to use for tokens of the given type.
	 * This is useful for implementing token-based code highlighting.
	 * 
	 * @param tokenType the type of token (e.g., keyword, string literal, etc.)
	 * @param style     the CSS class to use for the token type
	 */
	public void setTokenStyle(TokenType tokenType, String style) {
		tokenStyleMap.put(tokenType, style);
	}

	
	/**
	 * Set the default set of token styles.
	 */
	public void setDefaultTokenStyles() {
		setTokenStyle(TokenType.KEYWORD, "codekeyword");
		setTokenStyle(TokenType.SINGLE_LINE_COMMENT, "codecomment");
		setTokenStyle(TokenType.MULTI_LINE_COMMENT, "codecomment");
		setTokenStyle(TokenType.STRING_LITERAL, "codestring");
		setTokenStyle(TokenType.LITERAL, "codeliteral");
	}

	/**
	 * Convert input Java source to HTML.
	 * 
	 * @throws IOException
	 */
	public void convert() throws IOException {
		if (scanner == null) {
			scanner = new PlainTextTokenScanner();
		}
		
		lineNo = 1;
		
		beginCode();
		beginLine();
		
		// FIXME: Why is this implemented this way rather than as an adapter?
		LinkedList<Token> tokenQueue = new LinkedList<Token>();
		processToken: for(;;) {
			while (tokenQueue.isEmpty()) {
				Token token = scanner.scan(reader);
				if (token == null) {
					break processToken;
				}
				enqueueToken(tokenQueue, token);
			}

			
			Token token = tokenQueue.removeFirst();
			
			boolean displayThisLine = inDisplayRange();

			if (token.getType() == TokenType.NEWLINE) {
                		newLine(displayThisLine);
				continue;
			}
			
			if (!displayThisLine)
				continue;
			
			String style = tokenStyleMap.get(token.getType());

			if (style != null) {
			    out.print("<span class=\"");
				out.print(style);
				out.print("\">");
			}
			
			displayToken(token.getLexeme());
			
			if (style != null) {
				out.print("</span>");
			}
            
            
		}
		
		endLine();
		
		endCode();
	}

	private void enqueueToken(LinkedList<Token> tokenQueue, Token token) {

		if (token.getType() == TokenType.NEWLINE) {
			tokenQueue.addLast(token);
		} else {
			// Break up multiple-line tokens by inserting explicit NEWLINE tokens.
			
			String lexeme = token.getLexeme();
			int nl;
			while ((nl = lexeme.indexOf('\n')) >= 0) {
				tokenQueue.addLast(new Token(token.getType(), lexeme.substring(0, nl)));
				tokenQueue.addLast(new Token(TokenType.NEWLINE, "\n"));
				lexeme = lexeme.substring(nl + 1);
			}
			if (lexeme.length() > 0) {
				tokenQueue.addLast(new Token(token.getType(), lexeme));
			}
			
		}

	}

	private void newLine(boolean displayThisLine)
    throws IOException
    {
		col = 0;
		++lineNo;
		if (displayThisLine) {
			endLine();
			beginLine();
		}
	}
	
	private boolean inDisplayRange() {
		if (displayStart == 0)
			return true;
		else
			return lineNo >= displayStart && lineNo < displayEnd;
	}

	/**
	 * Begin the HTML code to display the source code.
	 */
	protected void beginCode() {
	    
//      Display code coverage stats if we have code coverage information avaialble.
        if (fileWithCoverage != null) {
            CoverageStats coverageStats = fileWithCoverage.getCoverageStats();
            out.println("<table>");
            
            out.println("<th>Source file</th>");
            out.println("<th>statements</th>");
            out.println("<th>conditionals</th>");
            out.println("<th>methods</th>");
            out.println("<th>total</th>");
            
            out.println("<tr>");
            out.println("<td>" +fileWithCoverage.getShortFileName()+"</td>");
            out.println(coverageStats.getHTMLTableRow());
            out.println("</tr>");
            
            out.println("</table>");
            out.println("<p>");
        }
	    
		out.println("<table class=\"codetable\" width=\"100%\">");
        
        // Tarantula: Coverage information split up by test case
        if (testOutcomeCollection != null) {
            out.print("<th>#<th>C");
            out.print(TestOutcomeCollection.formattedTestHeader(testOutcomeCollection));
            out.println("<th>score<th>line");
        }
	}

	/**
	 * End the HTML code to display the source code.
	 */
	protected void endCode() {
		out.println("</table>");
	}

	/**
	 * Begin a line of source code.
	 */
	protected void beginLine() throws IOException {
	    // Current range comes to an end?
		if (currentHighlightRange != null) {
			HighlightRange endRange =
				highlightEndMap.get(new Integer(lineNo));
			if (endRange != null) {
				currentHighlightRange = null;
			}
		}

		// New range begins?
		HighlightRange startRange =
			highlightStartMap.get(new Integer(lineNo));
		if (startRange != null) {
			currentHighlightRange = startRange;
			anchoredLine = true;
		}
		
		String style=null;
		if (currentHighlightRange != null)
		    style = currentHighlightRange.style;
		
		// right align the line number count
		// also drop an anchor at each line
		out.print("<tr><td class=\"linenumber\"><a title=\"" +lineNo+ "\"><tt>" +lineNo+ "</tt></a></td>");
		
        // Callback for adding coverage information to the generated page
        style = coverageCallback(style);
		
        out.print("<td");
		if (style != null) {
            // XXX HACK ALERT: Trying to set either a style and a class.
            if (style.contains("style="))
                out.print(" " +style);
            else
                out.print(" class=\"" + style + "\"");
		}
		out.print("><tt>");
		if (anchoredLine) {
			out.print("<a name=\"");
			out.print("codehighlight");
			out.print(currentHighlightRange.count);
			out.print("\">");
		}
	}
    /**
     * @param style
     * @return
     * @throws IOException
     */
	private String coverageCallback(String style) throws IOException
	{
		if (fileWithCoverage != null) {
			Line line = fileWithCoverage.getCoverage(lineNo);
			if (line == null)
				out.print("<td/>");
			else {
				String codeClass;
				
				// XXX: It should be ok to use the standard code class even for an anchored line
				if (false && anchoredLine)
					codeClass = "";
				else if (line.isCovered() ^ isExcludingCodeCoverage)
					codeClass = "class=\"codecoveredcount\"";
				else {
					codeClass = "class=\"codeuncoveredcount\"";
					if (!anchoredLine) style = "codeuncovered";
				}
				
				String body = "";
				if (line instanceof Cond) {
					Cond c = (Cond) line;
					body = "<tt>" + c.getTrueCount()+"/"+c.getFalseCount()+"</tt>";
				} else 
					body = "<tt>" + line.getCount()+"</tt>";
				
				out.print("<td align=\"right\" " + codeClass + ">" + body + "</tt></td>");
				
				//Display coverage information split up by testOutcome.
				style = tarantulaCallback(style);
				
			}
		}
		return style;
	}
    /**
     * @throws IOException
     */
    private String tarantulaCallback(String style) throws IOException
    {
        if (testOutcomeCollection != null) {
            
            // FIXME: Should handle any selected test type, not only all cardinal test types
            Iterable<TestOutcome> iterable= testOutcomeCollection.getIterableForCardinalTestTypes();
            
            boolean isLineExecutable=false;
            
            int totalTests=0;
            int passed=0;
            int totalPassed=0;
            int failed=0;
            int totalFailed=0;
            int countByOutcome=0;
            for (TestOutcome outcome : iterable) {
                totalTests++;
                FileWithCoverage coverageForGivenOutcome = outcome.getCodeCoverageResults().getFileWithCoverage(fileWithCoverage.getShortFileName());

                countByOutcome = coverageForGivenOutcome.getStmtCoverageCount(lineNo);
                if (outcome.getOutcome().equals(TestOutcome.PASSED))
                    totalPassed++;
                else
                    totalFailed++;
                
                if (countByOutcome > 0) {
                    isLineExecutable=true;
                    if (outcome.getOutcome().equals(TestOutcome.PASSED)) {
                        // passed outcome
                        out.print("<td class=\"passed\">"+countByOutcome+"</td>");
                        passed++;
                    } else {
                        out.print("<td class=\"failed\">"+countByOutcome+"</td>");
                        failed++;
                    }
                } else if (countByOutcome < 0){
                    out.print("<td></td>");
                } else {
                    out.print("<td></td>");
                }
            }
            if (isLineExecutable) {
                double passedPct = totalPassed>0?(double)passed / (double)totalPassed:0.0;
                double failedPct = totalFailed>0?(double)failed / (double)totalFailed:0.0;
                double intensity = Math.max(passedPct, failedPct);
                
                double score=passedPct/(passedPct+failedPct);
//                System.out.println(fileWithCoverage.getShortFileName() +
//                    ", lineNumber = "+lineNo+
//                    ", executable? "+isLineExecutable+
//                    ", countByOutcome = "+countByOutcome+
//                    ", totalPassed = "+totalPassed+
//                    ", totalFailed = "+totalFailed+
//                    ", passed = " +passed+
//                    ", failed = " +failed+
//                    ", passedPct = "+passedPct+
//                    ", failedPct = "+failedPct+
//                    ", score = "+score);
                style=" style=\"background-color:#" +scaleColor(score, intensity)+"\"";
                out.print("<td bgcolor=\"#"+scaleColor(score, intensity)+"\">" +format.format(score)+ "</td>");
            } else {
                out.print("<td></td>");
            }
        }
        return style;
    }
    
    /**
     * Given a score from 0.0 to 1.0, computes an RGB color where 0.0 is completely
     * red and 1.0 is completely green, and 0.5 is completley yellow.
     * TODO Handle brightness intensities. 
     * @param score
     * @return
     */
    public static String scaleColor(double score, double intensity) {
        int range = 511;
        int halfRange = 256;
        
        int hue = (int)(range * score);
        
        int red=0;
        int green=0;
        if (hue < halfRange) {
            // hue <= 255
            red=255;
            green=hue;
            
        } else if (hue==halfRange){
            green=255;
            red=255;
        } else {
            // hue > 256
            hue = hue-halfRange;
            green=255;
            red=halfRange-hue;
        }
        
        green = (int)((double)green * intensity);
        if (green > 255)
            green=255;
        red = (int)((double)red * intensity);
        if (red > 255)
            red=255;
        
        String greenStr=Long.toHexString((long)green);
        if (greenStr.length()==1)
            greenStr="0"+greenStr;
        
        String redStr=Long.toHexString((long)red);
        if (redStr.length()==1)
            redStr="0"+redStr;
        return redStr+greenStr+"00";
    }

    private static final NumberFormat format = new DecimalFormat("0.00");
	/**
	 * End a line of source code.
	 */
	protected void endLine() {
		if (col == 0) {
			displayEntity(ENTITY_NBSP);
		}
		if (anchoredLine) {
			out.print("</a>");
			anchoredLine = false;
		}
		out.println("</tt></td></tr>");
	}

	private void displayToken(String lexeme) throws IOException {
		for (int i = 0; i < lexeme.length(); ++i) {
			char c = lexeme.charAt(i);
			
			switch (c) {
			case ' ':
				displayEntity(ENTITY_NBSP); break;
			case '\t':
				displayTab(); break;
			case '<':
				displayEntity(ENTITY_LT); break;
			case '>':
				displayEntity(ENTITY_GT); break;
			case '&':
				displayEntity(ENTITY_AMP); break;
			default:
				out.print((char) c); ++col; break;	
			}
		}
	}

	private void displayEntity(Entity entity) {
		out.print(entity.value);
		++col;
	}

	private void displayTab() {
		int nSpace = tabWidth - (col % tabWidth);
		while (nSpace-- > 0) {
			out.print(ENTITY_NBSP.value);
			++col;
		}
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length < 1 || args.length > 4) {
			System.err.println("Usage: " + DisplaySourceCodeAsHTML.class.getName() +
					" <java source file> [<coverage.xml>] [<highlight start>-<num highlight lines>] [<n context lines>]");
			System.exit(1);
		}
		
		System.out.println("<html><head><title>Test</title>");
		if (new File("styles.css").exists()) {
			System.out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\">");
		}
		System.out.println("</head><body>");
		DisplaySourceCodeAsHTML src2html = null;
		try {
			src2html = new DisplaySourceCodeAsHTML();
			src2html.setInputStream(new FileInputStream(args[0]));
			src2html.setOutputStream(System.out);
			if (args.length >= 2) {
				// XML file containing code coverage results
				CodeCoverageResults codeCoverageResults = CodeCoverageResults.parseFile(args[1]);
				// trim out the full name of the source file
				String trimmedSourceFileName = args[0];
				int lastSlash = trimmedSourceFileName.lastIndexOf('/');
				trimmedSourceFileName = trimmedSourceFileName.substring(lastSlash+1);
				
				src2html.setFileWithCoverage(codeCoverageResults.getFileWithCoverage(trimmedSourceFileName));
				
				if (args.length >=4) {
					int startLine = Integer.parseInt(args[2]);
					int numLines = Integer.parseInt(args[3]); 
					src2html.addHighlightRange(startLine, numLines, "codehighlight");
					if (args.length >= 5) {
						int numContextLines = Integer.parseInt(args[4]);
						src2html.setDisplayRange(
								Math.max(1, startLine - numContextLines),
								startLine + numLines + numContextLines);
					}
				}
			}
			if (args[0].endsWith(".java"))
				src2html.setTokenScanner(new JavaTokenScanner());
			src2html.setDefaultTokenStyles();
			src2html.convert();
			System.out.println("</body></html>");
		} finally {
			if (src2html != null) src2html.close();
		}
	}

    private TestOutcomeCollection testOutcomeCollection;
    public void setTestOutcomeCollection(TestOutcomeCollection testOutcomeCollection) {
        this.testOutcomeCollection = testOutcomeCollection;
    }
    
    private boolean isExcludingCodeCoverage = false;
    
	public void setExcludingCodeCoverage(boolean b) {
		isExcludingCodeCoverage = b;
	}
}
