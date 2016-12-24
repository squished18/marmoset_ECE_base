package edu.umd.cs.marmoset.utilities;

import java.sql.Connection;

import edu.umd.cs.marmoset.utilities.DatabaseUtilities;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;

public class ZipFix
{

    /**
     * @param args
     */
    public static void main(String[] args)
    throws Exception
    {
        Connection conn=null;
        try {
            System.out.println(DatabaseUtilities.getDbProps());
            conn=DatabaseUtilities.getConnection("jdbc:mysql://submit.cs.umd.edu/submitserver");

            String submissionPK="44599";
            String filename=System.getenv("HOME") +"/APROJECTS/351/p2/dloe-fixed-Project2-44599.zip";

            
            MarmosetUtilities.fixSubmissionZipfile(conn, submissionPK, filename);
            
        } finally {
            DatabaseUtilities.releaseConnection(conn);
        }
    }

}
