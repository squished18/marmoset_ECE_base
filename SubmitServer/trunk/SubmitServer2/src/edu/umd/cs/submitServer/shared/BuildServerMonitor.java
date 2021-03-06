/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Oct 26, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * BuildServerMonitor
 * @author jspacco
 */
public class BuildServerMonitor
{
    private static int MAX_TO_CACHE=1;
    
    /**
     * Private constructor prevents other classes from instantiating me directly.
     */
    private BuildServerMonitor() {
    }
    
    private static BuildServerMonitor instance=null;
    public static synchronized BuildServerMonitor getInstance() {
        if (instance==null) {
            instance=new BuildServerMonitor();
        }
        return instance;
    }
    
    public synchronized void clear() {
        recentBuildServerMessageMap.clear();
        courseSemesterMap.clear();
    }
    
    /** Cache the last MAX_TO_CACHE messages from the buildServer */
    private Map<String,List<Timestamp>> recentBuildServerMessageMap = Collections.synchronizedMap(new TreeMap<String,List<Timestamp>>());
    
    private Map<String,String> courseSemesterMap = new HashMap<String,String>();
    private Map<String,Timestamp> timedOutBuildServerMap=new HashMap<String,Timestamp>();

    public synchronized void logRequestSubmission(String hostname, String semester, String courses)
    {
        logReceiptOfBuildServerMessage(hostname);
        courseSemesterMap.put(hostname, semester +": "+ courses);
    }
    
    public synchronized void logReceiptOfBuildServerMessage(String hostname)
    {
        logReceiptOfBuildServerMessage(hostname,new Timestamp(System.currentTimeMillis()));
    }
    
    public synchronized void logReceiptOfBuildServerMessage(String hostname, Timestamp ts)
    {
        List<Timestamp> list = recentBuildServerMessageMap.get(hostname);
        if (list==null) {
            list=new LinkedList<Timestamp>(){
                    @Override
                    public boolean add(Timestamp o)
                    {
                        // Add a new item to the cache.
                        if (size() >= MAX_TO_CACHE)
                            removeLast();
                        super.addFirst(o);
                        return true;
                    }
                    
                };
        }
        list.add(ts);
        recentBuildServerMessageMap.put(hostname, list);
        timedOutBuildServerMap.remove(hostname);
    }
    
    public synchronized String toString() {
        long now = System.currentTimeMillis();
        
        StringBuffer buf=new StringBuffer();
        
        cleanBuildServerCache();


	//**** DISPLAY HANGING BUILDSERVERS

        buf.append("\n\nThe following buildservers may be hanging, please check their logs and/or host loadaverage:\n\n");
        
	int hangers=0;
        for (Map.Entry<String,Timestamp> entry : timedOutBuildServerMap.entrySet()) {
            buf.append("Haven't heard from " +entry.getKey()+ 
                " in " +((now-entry.getValue().getTime())/(60*1000))+
                " minutes at " +entry.getValue()+"\n");
	   hangers++;
        }

	if ( hangers == 0 ) buf.append("--None--\n");


	//**** DISPLAY BUILDSERVER COUNTS

	SortedSet HostNameKeySet = new TreeSet();	
	HostNameKeySet.addAll(getHostnameMap().keySet());
	
	//todo: Sort the set that is returned by getHostnameMap
        buf.append("\n\nBuildServer Counts:\n\n");

	String current_hostname_prefix="";
	String previous_hostname_prefix="";
	int count = 0;
	String catalog = "cs";
	int catalog_offset = 5;
        for (Object hostname : HostNameKeySet) {

		if ( hostname.toString().indexOf("math") > -1 ) {
			catalog = "math";
			catalog_offset = 7;
		} else { 
			catalog = "cs";			
			catalog_offset = 5;
		}

		current_hostname_prefix = hostname.toString().substring(hostname.toString().indexOf(catalog),hostname.toString().indexOf(catalog)+catalog_offset);

	    	if (count > 0) {
			if ( ! current_hostname_prefix.equals(previous_hostname_prefix) ) 
			   buf.append("\n");
	    	}

            	buf.append(hostname.toString() + " has " +getHostnameMap().get(hostname) +" working buildservers.\n"); 
			//: " +uptime(hostname));
	
	    	previous_hostname_prefix=current_hostname_prefix;

	    	count++;

	}

	//**** DISPLAY BUILDSERVER POLL STATS
	
        buf.append("\n\nBuildServer poll stats:\n\n");

        for (String buildServerName : recentBuildServerMessageMap.keySet()) {
            buf.append(buildServerName + "; last "+MAX_TO_CACHE+ " messages received: " );
            for (Timestamp ts : recentBuildServerMessageMap.get(buildServerName)) {
                buf.append(((now-ts.getTime())/1000/60)+ " minutes ago at " +ts+ 
				" (" +courseSemesterMap.get(buildServerName) + ")\n\n");
            }
        }


        return buf.toString();        
    }
    
    public static boolean timeout(long oldTime, long newTime, long timeout)
    {
        return oldTime + timeout < newTime;
    }
    
    /** 
     * Number of millis without a message from a buildserver we'll wait before
     * considering the buildserver to have shut down. Necessary since I periodically
     * put the bug cluster in and out of action.
     */
    private long timeout=(1000*60*60*2);
    public synchronized void setTimeout(long timeout) {
        this.timeout=timeout;
    }

    public synchronized void cleanBuildServerCache()
    {
        cleanBuildServerCache(System.currentTimeMillis());
    }
    
    public synchronized void cleanBuildServerCache(long now)
    {
          
        for (Iterator<Map.Entry<String,List<Timestamp>>> i =  recentBuildServerMessageMap.entrySet().iterator(); i.hasNext() ;) {
        	Map.Entry<String,List<Timestamp>> entry = i.next();
            Timestamp ts = entry.getValue().get(0);
            if (timeout(ts.getTime(), now, timeout)) {           
                timedOutBuildServerMap.put(entry.getKey(), ts);
                i.remove(); // recentBuildServerMessageMap.remove(entry.getKey());
            }
        }
        

    }
  

    //Todo: this function needs some fixing to work with UWDRCSCS's student.cs environment
    private static String uptime(String hn)
    {
	//We attached the course name to the beginning of the 
	//hostname at UW, eg: cs241.cpu22.student.cs (on cpu22.student.cs.uwaterloo.ca)
	String hostname = hn.substring(6) + ".uwaterloo.ca"; 

        try {
            Socket s=new Socket(InetAddress.getByName(hostname),9999);
            BufferedReader reader=new BufferedReader(new InputStreamReader(s.getInputStream()));
            StringBuffer buf=new StringBuffer();
            while(true) {
                String line=reader.readLine();
                if (line==null) break;
                buf.append(line+"\n");
            }
            return buf.toString();
        } catch (UnknownHostException e) {
            return "Host " +hostname+ " is unknown!\n";
        } catch (IOException e) {
            return "Cannot connect to host " +hostname+"\n";
        }        
    }
    
    /**
     * A buildserver is named by its buildserver number and the hostname of the machine
     * that it's running on, which looks like this:\
     * <p>
     * bs1.marmoset3.umiacs.umd.edu
     * <p>
     * Thus it's necessary to extract just the hostname and count the number of buildservers
     * running on that host, which is what this method does.
     * @return A map from the hostname to the number of buildservers running on that host.
     */
    private Map<String,Integer> getHostnameMap()
    {
        Map<String,Integer> map=new HashMap<String,Integer>();
        for (String buildServerName : recentBuildServerMessageMap.keySet()) {
	    String hostname = "";
	    String catalog = "";
	    if (buildServerName.indexOf("math") > -1 ) {
		catalog = "math";
	    } else {
		catalog = "cs";
            }
            hostname=buildServerName.substring(buildServerName.indexOf(catalog));
            if (!map.containsKey(hostname)) 
                map.put(hostname, new Integer(1));
            else
                map.put(hostname, new Integer(map.get(hostname).intValue()+1));
        }
        return map;
    }
    private Map<String, String> getCourseSemesterMap() {
        return courseSemesterMap;
    }
    public synchronized Map<String, List<Timestamp>> getRecentBuildServerMessageMap() {
        return recentBuildServerMessageMap;
    }
}
