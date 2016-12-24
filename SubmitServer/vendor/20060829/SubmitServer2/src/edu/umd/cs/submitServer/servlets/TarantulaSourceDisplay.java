package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.parser.JavaTokenScanner;
import edu.umd.cs.submitServer.DisplaySourceCodeAsHTML;

public class TarantulaSourceDisplay extends SubmitServerServlet
{

    /**
     * The doGet method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to get.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        Connection conn=null;
        try {
            conn=getConnection();
            
            response.setContentType("text/html");
            PrintStream out = new PrintStream(response.getOutputStream());
            
            Submission submission = (Submission)request.getAttribute("submission");
            TestOutcomeCollection collection = (TestOutcomeCollection)request.getAttribute("testOutcomeCollection");
            String filename = request.getParameter("sourceFileName");
            
            printSubmissionAsHTML(out,filename,"all",collection,submission,conn);
            
            out.flush();
            out.close();
            
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }
        
    }

    public static void printSubmissionAsHTML(PrintStream out,
        String filename,
        String testType,
        TestOutcomeCollection collection,
        Submission submission,
        Connection conn)
    throws SQLException, IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(submission.downloadArchive(conn));
        ZipInputStream zipIn = new ZipInputStream(bais);
        Map<String,CodeCoverageResults> map = collection.getCoverageResultsMap();
        DisplaySourceCodeAsHTML src2html = new DisplaySourceCodeAsHTML();
        while (true) {
            ZipEntry entry = zipIn.getNextEntry();
            if (entry==null) break;
            if (entry.isDirectory()) continue;
            if (!entry.getName().endsWith(".java")) continue;
            if (!entry.getName().matches(filename)) continue;
            
            src2html.setInputStream(zipIn);
            src2html.setOutputStream(out);
            src2html.setTokenScanner(new JavaTokenScanner());
            src2html.setDefaultTokenStyles();
            
            src2html.setFileWithCoverage(map.get(TestOutcome.CARDINAL).getFileWithCoverage(new File(entry.getName()).getName()));
            src2html.setTestOutcomeCollection(collection);
            
            out.write("<html><head><title>Tarantula Coverage</title></head><body>\n".getBytes());
            out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/styles.css\">\n".getBytes());
            //out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"/export/home/jspacco/workspace/SubmitServer2/WebRoot/styles.css\">\n".getBytes());
            
            src2html.convert();
            
            out.write("</body></html>\n".getBytes());
            out.flush();
            break;
        }
    }
}
