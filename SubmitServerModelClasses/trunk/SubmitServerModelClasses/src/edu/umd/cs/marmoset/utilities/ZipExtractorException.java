/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Dec 5, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.utilities;

/**
 * ZipExtractorException
 * @author jspacco
 */
public class ZipExtractorException extends Exception
{
    public ZipExtractorException() {
        super();
    }
    public ZipExtractorException(String s) {
        super(s);
    }
    public ZipExtractorException(String s, Throwable t) {
        super(s,t);
    }
    public ZipExtractorException(Exception e) {
        super(e);
    }
}
