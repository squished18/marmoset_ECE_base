/*
 * Created on Feb 26, 2005
 *  
 */
package edu.umd.cs.marmoset.modelClasses;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ExceptionData implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    public static byte[] toBytes(ExceptionData ed) 
    throws IOException
    {
    	ObjectOutputStream out=null;
    	try {
            if (ed == null) return null;
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bytes);
            out.writeObject(ed);
            out.close();
            return bytes.toByteArray();
        } finally {
        	if (out!=null) out.close();
        }
    }

    public static ExceptionData fromInputStream(InputStream is)
    throws ClassNotFoundException, IOException
    {
    	ObjectInputStream ois=null;
    	try {
            ois = new ObjectInputStream(is);
            ExceptionData result = (ExceptionData) ois.readObject();
            return result;
        } finally {
        	if (ois!=null) ois.close();
        }
    }
    
    public static byte[] toBytes(Throwable e)
    throws IOException
    {
        return toBytes(new ExceptionData(e));
    }

    public static ExceptionData fromBytes(byte[] bytes)
    throws IOException, ClassNotFoundException
    {
    	ObjectInputStream in=null;
    	try {
            if (bytes == null) return null;
            in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            ExceptionData result = (ExceptionData) in.readObject();
            in.close();
            return result;
        } finally {
        	if (in!=null) in.close();
        }
    }

    final String exceptionClassName;

    final String exceptionMessage;

    final ExceptionData cause;

    final StackTraceElement[] stackTrace;

    public ExceptionData(Throwable e) {
        exceptionClassName = e.getClass().getName();
        exceptionMessage = e.getMessage();
        stackTrace = e.getStackTrace();
        if (e.getCause() == null)
            cause = null;
        else
            cause = new ExceptionData(e.getCause());
    }

    public String getClassName() {
        return exceptionClassName;
    }

    public String getMessage() {
        return exceptionMessage;
    }

    public ExceptionData getCause() {
        return cause;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }
}