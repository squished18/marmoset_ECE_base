/**
 * 
 */
package edu.umd.cs.submitServer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jspacco
 *
 */
public final class SubmitServerUtilities
{
	public static String extractURL(HttpServletRequest request)
	{
		String parameters = request.getQueryString();
        if (parameters == null)
            parameters = "";
        else
            parameters = "?" + parameters;
        return request.getContextPath() + request.getRequestURI() + parameters;
	}
    
    /**
     * Uses reflection to find the void (no-arg) constructor for a given class
     * and invoke it to create a new instance of the object.
     * @param className The class of the object to be instantiated.
     * @return An fresh instance of an object of type className.
     * @throws ServletException There are 5 exceptions that can happen when trying to
     *  find and invoke a constructor based on the name of the class; this method 
     *  wraps any of these exceptions with ServletException and then throws the
     *  ServletException.
     */
    public static Object createNewInstance(String className)
    throws ServletException
    {
        try {
            Class clazz=Class.forName(className);
            Constructor constructor=clazz.getConstructor(new Class[0]);
            return constructor.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ServletException(e);
        } catch (NoSuchMethodException e) {
            throw new ServletException(e);
        } catch (IllegalAccessException e) {
            throw new ServletException(e);
        } catch (InvocationTargetException e) {
            throw new ServletException(e);
        } catch (InstantiationException e) {
            throw new ServletException(e);
        }
    }
    
    /**
     * Should this button of an HTML set of radio buttons be checked?
     * @param currentVal
     * @param selectVal
     * @return "checked" if it should be checked, "" (empty string) otherwise
     */
    public static String checked(String currentVal, String selectVal) {
        return currentVal.equals(selectVal)?"checked":"";
    }
    
    /**
     * Should this &lt;option&gt; of an HTML &lt;select&gt; pull-down menu be the default selected?
     * @param currentVal
     * @param selectVal
     * @return "selected" if it should be selected, "" (empty string) otherwise
     */
    public static String selected(String currentVal, String selectVal) {
        return currentVal.equals(selectVal)?"selected":"";
    }
}
