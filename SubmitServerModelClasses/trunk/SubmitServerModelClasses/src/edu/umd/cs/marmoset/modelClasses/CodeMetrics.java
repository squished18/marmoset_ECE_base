/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Nov 28, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Visitor;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;
import org.apache.commons.httpclient.methods.MultipartPostMethod;

/**
 * CodeMetrics
 * @author jspacco
 */
public class CodeMetrics
{
    private String testRunPK;
    private String md5sumSourcefiles;
    private String md5sumClassfiles;
    private int codeSegmentSize;
    
    public static final String[] ATTRIBUTES_LIST = {
        "test_run_pk",
        "md5sum_sourcefiles",
        "md5sum_classfiles",
        "code_segment_size"
    };
    
    /** Name of this table in the database */
    public static final String TABLE_NAME = "code_metrics";
    
    /** Fully-qualified attributes for project_jarfiles table. */
    public static final String ATTRIBUTES = Queries.getAttributeList(TABLE_NAME, ATTRIBUTES_LIST);
    
    public CodeMetrics() {}
    
    public int fetchValues(ResultSet rs, int startingFrom)
    throws SQLException
    {
        setTestRunPK(rs.getString(startingFrom++));
        setMd5sumSourcefiles(rs.getString(startingFrom++));
        setMd5sumClassfiles(rs.getString(startingFrom++));
        setCodeSegmentSize(rs.getInt(startingFrom++));
        return startingFrom;
    }
    
    private int putValues(PreparedStatement stmt, int index)
    throws SQLException
    {
        stmt.setString(index++, getTestRunPK());
        stmt.setString(index++, getMd5sumSourcefiles());
        stmt.setString(index++, getMd5sumClassfiles());
        stmt.setInt(index++, getCodeSegmentSize());
        return index;
    }
    
    public void insert(Connection conn)
    throws SQLException
    {
        String insert = 
            " INSERT INTO " +TABLE_NAME+
            " VALUES ( ?, ?, ?, ? ) ";
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(insert);
            
            int index=1;
            index=putValues(stmt, index);
            
            stmt.executeUpdate();
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    /**
     * @return Returns the md5sumClassfiles.
     */
    public String getMd5sumClassfiles()
    {
        return md5sumClassfiles;
    }

    /**
     * @param md5sumClassfiles The md5sumClassfiles to set.
     */
    public void setMd5sumClassfiles(String md5sumClassfiles)
    {
        this.md5sumClassfiles = md5sumClassfiles;
    }

    /**
     * @return Returns the md5sumSourcefiles.
     */
    public String getMd5sumSourcefiles()
    {
        return md5sumSourcefiles;
    }

    /**
     * @param md5sumSourcefiles The md5sumSourcefiles to set.
     */
    public void setMd5sumSourcefiles(String md5sumSourcefiles)
    {
        this.md5sumSourcefiles = md5sumSourcefiles;
    }

    /**
     * @return Returns the codeSize.
     */
    public int getCodeSegmentSize()
    {
        return codeSegmentSize;
    }

    /**
     * @param codeSize The codeSize to set.
     */
    public void setCodeSegmentSize(int codeSize)
    {
        this.codeSegmentSize = codeSize;
    }
    
    public void mapIntoHttpHeader(MultipartPostMethod method)
    {
        method.addParameter("md5sumClassfiles", getMd5sumClassfiles());
        method.addParameter("md5sumSourcefiles", getMd5sumSourcefiles());
        method.addParameter("codeSegmentSize", Integer.toString(getCodeSegmentSize()));
    }
    /**
     * @return Returns the codeMetricsPK.
     */
    public String getTestRunPK()
    {
        return testRunPK;
    }
    /**
     * @param codeMetricsPK The codeMetricsPK to set.
     */
    public void setTestRunPK(String codeMetricsPK)
    {
        this.testRunPK = codeMetricsPK;
    }
    
    /**
     * Converts an array of bytes into a hexadecimal string.
     * TODO This method should go into a separate class of static utilities.
     * @param bytes the array of bytes
     * @return the hexadecimal string representation of the byte array
     */
    static String byteArrayToHexString(byte[] bytes)
    {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++){
          int v = bytes[i] & 0xff;
          if (v < 16) {
            sb.append('0');
          }
          sb.append(Integer.toHexString(v));
        }
        return sb.toString().toLowerCase();
    }

    static String md5sumAsHexString(List<File> fileList)
    throws NoSuchAlgorithmException, IOException
    {
        return byteArrayToHexString(md5sum(fileList));
    }
    
    public void setMd5sumClassfiles(List<File> fileList)
    throws NoSuchAlgorithmException, IOException
    {
        setMd5sumClassfiles(md5sumAsHexString(fileList));
    }
    
    public void setMd5sumSourcefiles(List<File> fileList)
    throws NoSuchAlgorithmException, IOException
    {
        setMd5sumSourcefiles(md5sumAsHexString(fileList));
    }

    /**
     * Computes the MD5SUM of a list of files.
     * TODO This method should go into a separate class of static utilities.
     * @param fileList the list of files
     * @return the MD5SUM (as a hexadecimal String) of the md5sum of a given list of files.
     * @throws NoSuchAlgorithmException thrown when the md5sum algorithm is not available
     * @throws FileNotFoundException if any of the files in the list cannot be found
     * @throws IOException if any of the files in the list cannot be read
     */
    static byte[] md5sum(List<File> fileList)
    throws NoSuchAlgorithmException, IOException
    {
        MessageDigest md5 = MessageDigest.getInstance("md5");
        for (Iterator<File> ii=fileList.iterator(); ii.hasNext();)
        {
            File file = ii.next();
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[2048];
            int numRead;
            while ((numRead = fis.read(bytes)) != -1)
            {
                md5.update(bytes, 0, numRead);
            }
            fis.close();
        }
        return md5.digest();
    }

    private static class Tracker
    {
        private int size;
        /**
         * @return Returns the size.
         */
        public int getSize() {
            return size;
        }
        /**
         * @param size The size to set.
         */
        public void setSize(int size){
            this.size = size;
        }
        public void addSize(int size) {
            this.size += size;
        }
    }
    
    public void setCodeSegmentSize(File dir, List<File> list, String classpath)
    throws ClassNotFoundException
    {
        Repository.clearCache();
        SyntheticRepository repos = SyntheticRepository.getInstance(new ClassPath(classpath));
        Repository.setRepository(repos);
        int size=0;
        for (File file : list) {
            String classname=file.getAbsolutePath().replace(dir.getAbsolutePath()+"/","");
            classname = classname.replace(".class","");
            classname = classname.replace('/','.');
            //if (classname.contains("package-info")) continue;
            size += CodeMetrics.sizeOfCodeSegment(classname);
        }
        setCodeSegmentSize(size);
    }

    /**
     * @param classname
     * @return
     * @throws ClassNotFoundException
     */
    static int sizeOfCodeSegment(String classname)
    throws ClassNotFoundException
    {
        JavaClass javaClass = Repository.lookupClass(classname);
        final Tracker tracker=new Tracker();
        Visitor v = new EmptyVisitor() {

            /* (non-Javadoc)
             * @see org.apache.bcel.classfile.EmptyVisitor#visitMethod(org.apache.bcel.classfile.Method)
             */
            @Override
            public void visitMethod(Method method)
            {
                super.visitMethod(method);
                //System.out.println("Visit method: " +method.getName());
                //tracker.addSize(method.getCode().getLength());
            }
            
            public void visitCode(Code code)
            {
                super.visitCode(code);
                tracker.addSize(code.getLength());
            }
        };
        javaClass.accept(new DescendingVisitor(javaClass,v));
        return tracker.getSize();
    }
    
    public String toString() {
        return "md5sum of classfiles: " +getMd5sumClassfiles()+"\n"+
            "\tmd5sum of sourcefiles: " +getMd5sumSourcefiles()+"\n"+
            "\tsize of code segments: " +getCodeSegmentSize();
    }
}
