/*
 * Created on Mar 17, 2005
 *
 */
package edu.umd.cs.marmoset.utilities;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * @author jspacco
 *
 */
public class ParseWebXml
{
    public static final String FILTER_NAME= "filter-name";
    public static final String FILTER_URL_PATTERN= "url-pattern";
    public static final String FILTER_CLASS= "filter-class";
    
    public static final String SERVLET_NAME= "servlet-name";
    public static final String SERVLET_CLASS = "servlet-class";
    public static final String SERVLET_URL_PATTERN = "url-pattern";
    
    private static final Set ignoredDirs = new HashSet();
    
    static {
        ignoredDirs.add("classes");
        ignoredDirs.add("META-INF");
        ignoredDirs.add("WEB-INF");
        ignoredDirs.add("CVS");
    }
                                
    
    public ParseWebXml() {}
    
    private final Map servletMap = new HashMap();
    private final List filterList = new LinkedList();
    
    public static void crappyXPathMethod(String webXmlFileName)
    throws FileNotFoundException, DocumentException
    {
        File file = new File(webXmlFileName);
        
        FileInputStream fis = new FileInputStream(file);
        SAXReader reader = new SAXReader();
        Document document = reader.read(fis);
        List list = document.selectNodes("//web-app[@*]");
        //System.out.println("list.size() " +list.size());
        for (Iterator ii=list.iterator(); ii.hasNext();)
        {
            Element elt = (Element)ii.next();
            Node n = elt.selectSingleNode("//web-app/servlet/name");
            //System.out.println("name: " +n.getText());
        }
    }
    
    public static ParseWebXml parse(String webXmlFileName)
    throws FileNotFoundException, DocumentException
    {
        File file = new File(webXmlFileName);
        
        FileInputStream fis = new FileInputStream(file);
        SAXReader reader = new SAXReader();
        Document document = reader.read(fis);
        
        ParseWebXml webXml = new ParseWebXml();
        
        Element root = document.getRootElement();

        for ( Iterator ii=root.elementIterator( "servlet-mapping" ); ii.hasNext();)
        {
            Element elt = (Element)ii.next();
            //System.out.print("name: " +elt.getName());
            
            String urlPattern=null;
            String servletName=null;
            for (int jj=0; jj < elt.nodeCount(); jj++)
            {
                Node node = elt.node(jj);
                if (node.getName() == null)
                    continue;
                if (node.getName().equals(SERVLET_NAME)) {
                    servletName = node.getText().trim();
                    if (webXml.tryToMapServlet(servletName, urlPattern))
                        break;
                } else if (node.getName().equals(SERVLET_URL_PATTERN)) {
                    urlPattern = node.getText().trim();
                    if (webXml.tryToMapServlet(servletName, urlPattern))
                        break;
                }
            }
            //System.out.println(" is mapped thusly: " +servletName +" => "+ urlPattern);
        }
        
        for (Iterator ii=root.elementIterator( "filter-mapping"); ii.hasNext();)
        {
            Element elt = (Element)ii.next();
            //System.out.print("name: " +elt.getName());
            
            String filterName=null;
            String urlPattern=null;
            for (int jj=0; jj < elt.nodeCount(); jj++)
            {
                Node node = elt.node(jj);
                if (node.getName() == null)
                    continue;
                if (node.getName().equals(FILTER_NAME)) {
                    filterName = node.getText().trim();
                    if (webXml.tryToCreateFilter(filterName, urlPattern))
                        break;
                } else if (node.getName().equals(FILTER_URL_PATTERN)) {
                    urlPattern = node.getText().trim();
                    if (webXml.tryToCreateFilter(filterName, urlPattern))
                        break;
                }
            }
            //System.out.println(" is mapped thusly: " +filterName+ " => "+ urlPattern);
                    
        }
        
        return webXml;
    }
    
    private Map urlFilters = new HashMap();
    public void addFilter(String filterName, String relativePath)
    {
        List list=null;
        if (urlFilters.containsKey(relativePath))
            list = (List)urlFilters.get(relativePath);
        else
            list = new LinkedList();
        list.add(filterName);
        urlFilters.put(relativePath, list);
    }
    
    /**
     * @param webRootPath
     */
    public void parseWebRoot(final String webRootPath)
    {
        File file = new File(webRootPath);
        
        if (!file.isDirectory()) {
            throw new IllegalStateException(file.getAbsolutePath() +" is not a directory!");
        }
        
        // bit of a hack
        // I'm using a pseudo-visitor pattern here to visit all the files
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    if (ignoredDirs.contains(file.getName()))
                        return false;
                    file.listFiles(this);
                    return false;
                }
                String relativePath = stripLeadingPath(webRootPath, file);
                //System.out.println("relativePath: " +relativePath);
                for (Iterator ii=filterList.iterator(); ii.hasNext();)
                {
                    Filter filter = (Filter)ii.next();
                    if (relativePath.matches(filter.regexp))
                    {
                        addFilter(filter.filterName, relativePath);
                    }
                }
                return false;
            }
        };
        file.listFiles(fileFilter);
        
        for (Iterator ii=servletMap.values().iterator(); ii.hasNext();) {
            String urlPattern = (String)ii.next();
            for (Iterator jj=filterList.iterator(); jj.hasNext();) {
                Filter filter = (Filter)jj.next();
                if (urlPattern.matches(filter.regexp)) {
                    addFilter(filter.filterName, urlPattern);
                }
            }
        }
        
    }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (Iterator ii=urlFilters.keySet().iterator(); ii.hasNext();)
        {
            String url = (String)ii.next();
            buf.append(url +": \n");
            List list = (List)urlFilters.get(url);
            for (Iterator jj=list.iterator(); jj.hasNext();)
            {
                buf.append("\t" +jj.next()+ "\n");
            }
        }
        return buf.toString();
    }
    
    static String stripLeadingPath(String leadingPath, File file)
    {
        //System.out.println("leadingPath: " +leadingPath);

        String fullFilePath = file.getAbsolutePath();

        //System.out.println("fullFilePath: " +fullFilePath);
        
        // remove a trailing slash if this is a directory        
        return fullFilePath.replaceAll(leadingPath, "");
    }
    
    public static void main(String args[])
    throws Exception
    {
        String webXmlFile = "/export/home/jspacco/workspace/SubmitServer2/WebRoot/WEB-INF/web.xml";
    	ParseWebXml webXml = ParseWebXml.parse(webXmlFile);
    	
    	webXml.parseWebRoot("/export/home/jspacco/workspace/SubmitServer2/WebRoot");
    	
    	System.out.println(webXml);
    }
    
    private boolean tryToMapServlet(String servletName, String urlPattern)
    {
        if (servletName == null || urlPattern == null)
            return false;
        servletMap.put(servletName, urlPattern);
        return true;
    }
    
    private boolean tryToCreateFilter(String filterName, String urlPattern)
    {
        if (filterName == null || urlPattern == null)
            return false;
        filterList.add(new Filter(filterName, urlPattern));
        return true;
    }
    
    private static class Filter
    {
        final String filterName;
        final String urlPattern;
        final String regexp;
        
        public Filter(String filterName, String urlPattern)
        {
            this.filterName = filterName;
            this.urlPattern = urlPattern;
            this.regexp = urlPattern.replaceAll("\\*", ".*");
        }
    }
}
