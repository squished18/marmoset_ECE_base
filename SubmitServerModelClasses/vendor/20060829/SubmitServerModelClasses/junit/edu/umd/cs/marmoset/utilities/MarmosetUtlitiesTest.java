package edu.umd.cs.marmoset.utilities;

import junit.framework.TestCase;

public class MarmosetUtlitiesTest extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MarmosetUtlitiesTest.class);
    }
    
    private void compareStackTraces(StackTraceElement t1, StackTraceElement t2)
    {
        assertEquals(t1.getClassName(), t2.getClassName());
        assertEquals(t1.getMethodName(), t2.getMethodName());
        assertEquals(t1.getFileName(), t2.getFileName());
        assertEquals(t1.getLineNumber(), t2.getLineNumber());
    }
    
    private void createAndParseStackTrace(String declaringClass, String methodName, String fileName, int lineNumber)
    {
        StackTraceElement t=new StackTraceElement(declaringClass,methodName,fileName,lineNumber);
        System.out.println(t.toString());
        StackTraceElement trace=MarmosetUtilities.parseStackTrace(t.toString());
        compareStackTraces(t, trace);
    }
    
    public void testParseNormalStackTrace()
    throws Exception
    {
        createAndParseStackTrace("utilities.Utilities","doDijkstra", "Utilities.java",146);
    }
    public void testParseStackTraceWithUnknownSource()
    throws Exception
    {
        createAndParseStackTrace("java.security.AccessControlContext","checkPermission",null,-1);
    }
    public void testParseStackTraceForConstructors()
    throws Exception
    {
        createAndParseStackTrace("cs132.p1.Sudoku","<init>","Sudoku.java",51);
    }

}
