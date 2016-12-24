/**
 * Copyright (C) 2006, University of Maryland
 * All Rights Reserved
 * Created on Jul 22, 2006
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.utilities;


public class ExceptionKey implements Comparable
{
    /**
     * Returns true if the two exceptionKeys are in the same class and method.
     *  Essentially, this ignores the line numbers.
     * @param o The other ExceptionKey.
     * @return True if the two exceptionKeys are the same class and method; false otherwise.
     */
    public int compareTo(Object o)
    {
        ExceptionKey other=(ExceptionKey)o;
        // If the filenames are different, then these are different objects
        int fileNameComparison=stackTraceElement.getFileName().compareTo(other.stackTraceElement.getFileName());
        if (fileNameComparison!=0)
            return fileNameComparison;
        // If the classnames are different, then these are different objects
        int classComparison=stackTraceElement.getClassName().compareTo(other.stackTraceElement.getClassName());
        if (classComparison!=0)
            return classComparison;
        // Otherwise, just check that the method names are the same; i.e. this will
        // ignore the lineNumbers
        return stackTraceElement.getMethodName().compareTo(other.stackTraceElement.getMethodName());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return compareTo(obj)==0;
    }
    
    @Override
    public int hashCode()
    {
        return MarmosetUtilities.hashString(stackTraceElement.getFileName() +
            stackTraceElement.getClassName() +
            stackTraceElement.getMethodName());
    }
    
    private StackTraceElement stackTraceElement;
    public ExceptionKey(String s) {
        stackTraceElement=MarmosetUtilities.parseStackTrace(s);
    }
    public ExceptionKey(StackTraceElement stackTraceElement) {
        this.stackTraceElement=stackTraceElement;
    }
    
    public String toString() {
        return stackTraceElement.toString();
    }
    public StackTraceElement getStackTraceElement() {
        return stackTraceElement;
    }
}