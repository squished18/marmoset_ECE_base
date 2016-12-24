/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on May 26, 2005
 *
 */
package edu.umd.cs.marmoset.modelClasses;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * @author jspacco
 * TODO replace with jakarta-commons-IO
 */
public class IO
{

    /**
     * Copy all data from an input stream to an output stream.
     * @param in the InputStream
     * @param out the OutputStream
     * @throws IOException if an IO error occurs
     */
    public static void copyStream(InputStream in, OutputStream out) throws IOException {
    	copyStream(in, out, Integer.MAX_VALUE);
    }

    /**
     * Copy all data from an input stream to an output stream.
     * @param in the InputStream
     * @param out the OutputStream
     * @param length the maximum number of bytes to copy
     * @throws IOException if an IO error occurs
     */
    public static void copyStream(InputStream in, OutputStream out, int length)
    		throws IOException {
    	
    	byte[] buf = new byte[4096];
    	
    	for (;;) {
    		int readlen = Math.min(length, buf.length);
    		int n = in.read(buf, 0, readlen);
    		if (n < 0)
    			break;
    		out.write(buf, 0, n);
    		length -= n;
    	}
    }

    public static Process execAndDumpToStringBuffer(String cmd[], final StringBuffer out, final StringBuffer err) throws IOException
    {
        final Process proc = Runtime.getRuntime().exec(cmd);
        
        Thread t = new Thread()
        { 
            public void run()
            {
            	BufferedReader reader=null;
            	try {
            		reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    String line;
                    while ( (line = reader.readLine()) != null )
                    {
                        out.append(line +"\n");
                    }
                }
                catch (IOException e) {
                    System.err.println("Exception getting output stream from proc process:" + e);
                } finally {
                	try {
                		if (reader != null) reader.close();
                	} catch (IOException ignore) {
                		// ignore
                	}
                }
            }
        };
        t.start();
        
        Thread t2 = new Thread()
        { 
            public void run()
            {
            	BufferedReader reader=null;
            	try { 
                    reader= new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                    String line;
                    while ( (line = reader.readLine()) != null )
                    {
                        err.append(line +"\n");
                    }
                }
                catch (IOException e) {
                    System.err.println("Exception getting output stream from proc process:" + e);
                } finally {
                	try {
                		if (reader!=null) reader.close();
                	} catch (IOException ignore) {
                		// ignore
                	}
                }
                
                
            }
        };
        t2.start();
        
        return proc;
    }
    
    public static void closeInputStreamAndIgnoreIOException(InputStream in)
    {
    	try {
    		if (in != null) in.close();
    	} catch (IOException ignore) {
    		// ignore
    	}
    }
    
    public static void closeOutputStreamAndIgnoreIOException(OutputStream out)
    {
    	try {
    		if (out != null) out.close();
    	} catch (IOException ignore) {
    		// ignore
    	}
    }
    
    public static void closeReaderAndIgnoreIOException(Reader reader)
    {
    	try {
    		if (reader != null) reader.close();
    	} catch (IOException ignore) {
    		// ignore
    	}
    }
    
    public static void closeWriterAndIgnoreIOException(Writer writer)
    {
    	try {
    		if (writer != null) writer.close();
    	} catch (IOException ignore) {
    		// ignore
    	}
    }
    public static void closeAndIgnoreIOException(Closeable closeable)
    {
    	try {
    		if (closeable != null) closeable.close();
    	} catch (IOException ignore) {
    		// ignore
    	}
    }
}
