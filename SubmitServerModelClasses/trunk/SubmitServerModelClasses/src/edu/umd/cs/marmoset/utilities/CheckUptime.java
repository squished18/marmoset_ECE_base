/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Nov 15, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

/**
 * CheckUptime
 * @author jspacco
 */
public class CheckUptime
{

    /**
     * @param args
     */
    public static void main(String[] args)
    throws IOException
    {
        InetAddress address = InetAddress.getByName("localhost");
        if (args.length > 0) {
            address = InetAddress.getByName(args[0]);
        }
        int port=9999;
        if (args.length > 1) {
            port=Integer.parseInt(args[1]);
        }
        
        Socket s=new Socket(address,port);
        BufferedReader reader=new BufferedReader(new InputStreamReader(s.getInputStream()));
        while(true) {
            String line=reader.readLine();
            if (line==null) break;
            System.out.println(line);
        }
    }
}
