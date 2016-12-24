/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Dec 2, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

/**
 * TextFileReader: Simple class for reading lines out of a text file using an iterator.
 * 
 * XXX Should this extend java.util.Reader or not?
 * @author jspacco
 */
public class TextFileReader implements Iterable<String> 
{
    private BufferedReader reader;
    
    public void close() throws IOException
    {
        reader.close();
    }
    
    public TextFileReader(String filename) throws IOException
    {
        reader=new BufferedReader(new FileReader(filename));
    }
    
    public TextFileReader(InputStream is) throws IOException
    {
        reader=new BufferedReader(new InputStreamReader(is));
    }
    
    public TextFileReader(Reader reader) throws IOException
    {
        this.reader=new BufferedReader(reader);
    }
    
    public Iterator<String> iterator()
    {
        return new Iterator<String>() {
            private String line=null;

            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext()
            {
                try {
                    line=reader.readLine();
                    
                    return line!=null;
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            public String next()
            {
                return line;
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove()
            {
                throw new IllegalStateException("Cannot remove elements from a TextFileReader through its iterator()");
            }
            
        };
    }
}
