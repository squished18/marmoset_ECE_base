/*
 * Created on Aug 31, 2004
 */
package edu.umd.cs.submitServer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.CopyUtils;

/**
 * Facade for bulk I/O operations.
 * 
 * @author David Hovemeyer
 */
public abstract class IOUtilities {
	/**
	 * Send a file to the HTTP client.
	 * Caller should already have set the content-type and content-length
	 * in the response.
	 * 
	 * @param response the HTTP response used to communicate with client
	 * @param projectFile the file to send
	 * @param length the maximum number of bytes to send
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void sendFileToClient(HttpServletResponse response, File projectFile, int length)
			throws FileNotFoundException, IOException {
		OutputStream out = response.getOutputStream();

		// Copy project file to client
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(projectFile));
			byte[] buf = new byte[4096];
			int n;

			while (length > 0) {
				int readlen = Math.min(length, buf.length);
				n = in.read(buf, 0, readlen);
				if (n < 0)
					break;
				out.write(buf, 0, n);
				length -= n;
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignore) {
					// Ignore
				}
			}
		}
	}
	/**
     * Sends the bytes in the byte array to the client on the OutputStream
     * of the given response object.
     * @param response
     * @param bytes
     * @throws IOException
     */
    public static void sendBytesToClient(
            byte[] bytes,
            HttpServletResponse response,
            String contentType)
    throws IOException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        


        // Inform client of content type and length
        response.setContentLength(bytes.length);
        response.setContentType(contentType);

        
        OutputStream out = response.getOutputStream();
        CopyUtils.copy(bais, out);
        out.flush();
        out.close();
        bais.close();
    }

}
