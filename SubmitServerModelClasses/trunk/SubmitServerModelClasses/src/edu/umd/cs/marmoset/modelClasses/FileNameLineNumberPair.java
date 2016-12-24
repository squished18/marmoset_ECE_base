/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Jul 5, 2005
 *
 */
package edu.umd.cs.marmoset.modelClasses;


public class FileNameLineNumberPair
{
    private final String fileName;
    private final int lineNumber;
    
    public FileNameLineNumberPair(String fileName, int lineNumber) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }
    public FileNameLineNumberPair(String fileName, String lineNumber) {
        this(fileName, Integer.parseInt(lineNumber));
    }
    public FileNameLineNumberPair(String fileName) {
        this(fileName, -1);
    }
    public String getFileName() {
        return fileName;
    }
    public int getLineNumber() {
        return lineNumber;
    }
    
    public final static FileNameLineNumberPair EMPTY = new FileNameLineNumberPair("NO FILE", -1);
}