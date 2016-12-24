/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Jan 18, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author jspacco
 * Contains a jar archive contained in a row of a database.
 */
public abstract class Archive
{
    private Archive() { }
    
    /** List of columns in this table */
    final static String[] ATTRIBUTES_LIST = {
            "archive_pk", "archive"
    };
    
    public static byte[] downloadBytesFromArchive(String tableName, String archivePK, Connection conn)
    throws SQLException
    {
        String attributes = Queries.getAttributeList(tableName, ATTRIBUTES_LIST);
        String query = 
            " SELECT " +attributes+ 
            " FROM " +tableName+
            " WHERE archive_pk = ? ";
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, archivePK);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next())
            {
                return rs.getBytes(2);
            }
            throw new SQLException("cannot find archive in table " +tableName+ " with PK " +archivePK);
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
     static String uploadBytesToArchive(String tableName, byte[] bytes, Connection conn)
    throws SQLException
    {
        String insert = 
        " INSERT INTO " +tableName+
        " (archive_pk, archive) " +
        " VALUES " +
        " (DEFAULT, ?) ";
        
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(insert);
            stmt.setBytes(1, bytes);
            
            stmt.executeUpdate();
            
            return Queries.lastInsertId(conn);
        } finally {
            Queries.closeStatement(stmt);
        }
    }
     
     /**
      * Update the bytes in the archive.  This method is only necessary for example
      * when students upload a zip archive that Java is unable to unzip (the
      * zip standard, by the way, isn't really much of a standard and students
      * find ways to upload zip archives that don't work correctly under Java but
      * work with command-line zip utilities, or work with ZipFile but not 
      * ZipInputStream).  In general, you should <b>not</b> be replacing archives,
      * the whole point to the system is that we're <b>keeping</b> everything.
      * @param tableName
      * @param archivePK
      * @param cachedArchive
      * @param conn
      * @throws SQLException
      */
     static void updateBytesInArchive(String tableName, String archivePK, byte[] cachedArchive, Connection conn)
     throws SQLException
     {
         String sql =
             " UPDATE " +tableName+
             " SET archive = ? " +
             " WHERE archive_pk = ? ";
         PreparedStatement stmt=null;
         try {
             stmt=conn.prepareStatement(sql);
             stmt.setBytes(1,cachedArchive);
             stmt.setString(2,archivePK);
             stmt.executeUpdate();
         } finally {
             Queries.closeStatement(stmt);
         }
            
     }
}
