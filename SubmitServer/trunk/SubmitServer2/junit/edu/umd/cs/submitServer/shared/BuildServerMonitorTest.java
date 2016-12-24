package edu.umd.cs.submitServer.shared;

import java.sql.Timestamp;

import edu.umd.cs.submitServer.shared.BuildServerMonitor;
import junit.framework.TestCase;

public class BuildServerMonitorTest extends TestCase
{
    private BuildServerMonitor buildServerMonitor=BuildServerMonitor.getInstance();
    public void testCouncurrentModificationException() throws Exception
    {
        buildServerMonitor.setTimeout(1000);
        buildServerMonitor.logReceiptOfBuildServerMessage("bs1.foo.cs.umd.edu", new Timestamp(1000));
        buildServerMonitor.logReceiptOfBuildServerMessage("bs1.foo.cs.umd.edu", new Timestamp(2000));
        buildServerMonitor.logReceiptOfBuildServerMessage("bs1.foo.cs.umd.edu", new Timestamp(3000));
        buildServerMonitor.logReceiptOfBuildServerMessage("bs2.foo.cs.umd.edu", new Timestamp(1000));
        buildServerMonitor.logReceiptOfBuildServerMessage("bs2.foo.cs.umd.edu", new Timestamp(2000));
        buildServerMonitor.logReceiptOfBuildServerMessage("bs2.foo.cs.umd.edu", new Timestamp(3000));
        int originalSize=buildServerMonitor.getRecentBuildServerMessageMap().size();
        //System.out.println(buildServerMonitor.getRecentBuildServerMessageMap());
        assertTrue(originalSize>0);

        buildServerMonitor.cleanBuildServerCache(20000);
        //buildServerMonitor.toString();
    }
    
    public void testCacheTimeout()
    throws Exception
    {
        //BuildServerMonitor buildServerMonitor = BuildServerMonitor.getInstance();
        buildServerMonitor.setTimeout(1000);
        buildServerMonitor.logReceiptOfBuildServerMessage("bs1.foo.cs.umd.edu", new Timestamp(1000));
        buildServerMonitor.logReceiptOfBuildServerMessage("bs1.foo.cs.umd.edu", new Timestamp(2000));
        buildServerMonitor.logReceiptOfBuildServerMessage("bs1.foo.cs.umd.edu", new Timestamp(3000));
        int originalSize=buildServerMonitor.getRecentBuildServerMessageMap().size();
        //System.out.println(buildServerMonitor.getRecentBuildServerMessageMap());
        assertTrue(originalSize>0);

        //buildServerMonitor.cleanBuildServerCache(20000);
        buildServerMonitor.toString();
        int newSize=buildServerMonitor.getRecentBuildServerMessageMap().size();
        //System.out.println(buildServerMonitor.getRecentBuildServerMessageMap());
        
        assertTrue(newSize==0);
        System.out.println(buildServerMonitor.toString());
    }
    
    public void testDontCacheTimeout() throws Exception {
        //BuildServerMonitor buildServerMonitor = BuildServerMonitor.getInstance();
        buildServerMonitor.setTimeout(1000);
        buildServerMonitor.logReceiptOfBuildServerMessage("bs1.foo.cs.umd.edu", new Timestamp(1000));
        int originalSize=buildServerMonitor.getRecentBuildServerMessageMap().size();
        assertTrue(originalSize>0);
        
        buildServerMonitor.cleanBuildServerCache(1500);
        int newSize=buildServerMonitor.getRecentBuildServerMessageMap().size();
        assertTrue(newSize==originalSize);
    }
    
    public void testTimeout2()
    throws Exception
    {
        assertFalse(BuildServerMonitor.timeout(1000, 60000, 60*1000));
    }
    
    public void testDontTimeout() throws Exception {
        assertTrue(BuildServerMonitor.timeout(1000, 62000, 60*1000));
    }
}
