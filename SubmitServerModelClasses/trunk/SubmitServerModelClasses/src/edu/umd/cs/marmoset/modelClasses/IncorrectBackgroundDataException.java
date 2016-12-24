/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Dec 7, 2004
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;


/**
 * @author jspacco
 *
 */
public class IncorrectBackgroundDataException extends Exception
{
    private BackgroundData backgroundData;
    
    public IncorrectBackgroundDataException(
            BackgroundData backgroundData,
            String message)
    {
        super(message);
        this.backgroundData = backgroundData;
    }
    
    /**
     * void constructor. 
     */
    public IncorrectBackgroundDataException()
    {
        super();
    }

    /**
     * @param message
     */
    public IncorrectBackgroundDataException(String message)
    {
        super(message);
    }
}
