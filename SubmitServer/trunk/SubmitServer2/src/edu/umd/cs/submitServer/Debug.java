/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 9, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer;

import java.io.IOException;
import java.io.PrintStream;

/**
 * @author jspacco
 *
 */
public class Debug
{
    public static final PrintStream err = System.err;
    public static final PrintStream out= System.out;
    
    public static final void println(String s)
    {
        err.println(s);
    }
    
    public static final void error(String s)
    {
        err.println(s);
    }
    
    public static final void warn(String s)
    {
        err.println(s);
    }

    /**
     * @param string
     * @param e
     */
    public static void exception(String string, IOException e)
    {
        err.println(string +" "+e.toString());
    }

}
