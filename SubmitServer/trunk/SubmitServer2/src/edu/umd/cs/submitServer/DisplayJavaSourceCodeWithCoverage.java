/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Nov 5, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.io.IOException;

import edu.umd.cs.marmoset.codeCoverage.CoverageStats;
import edu.umd.cs.marmoset.codeCoverage.FileWithCoverage;
import edu.umd.cs.marmoset.parser.JavaTokenScanner;

/**
 * DisplayJavaSourceCodeWithCoverage
 * @author jspacco
 */
public class DisplayJavaSourceCodeWithCoverage extends DisplaySourceCodeAsHTML
{
    public DisplayJavaSourceCodeWithCoverage(FileWithCoverage fileWithCoverage) {
        setTokenScanner(new JavaTokenScanner());
        this.fileWithCoverage=fileWithCoverage;
    }
    
    @Override
    protected void beginCode()
    {
//      Display code coverage stats if we have code coverage information avaialble.
        CoverageStats coverageStats = fileWithCoverage.getCoverageStats();
        out.println("<table>");
        out.println("<tr>");
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

        super.beginCode();
    }

    
    
}
