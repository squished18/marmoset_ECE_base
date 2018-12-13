package ca.uwaterloo.cs.submitServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/*
 * This class wraps HttpServletRequest for the purpose of allowing the
 * DummyAuthFilter to set the remote user from inside Tomcat. Normally
 * this can only be done from outside via a connector, for example. We
 * use the HttpServletRequestWrapper to allow us to create a class
 * that operates as a regular HttpServletRequest, but with our
 * overrides.
 *
 * See ca/uwaterloo/cs/submitServer/filters/DummyAuthFilter for the
 * filter implementation.
 *
 * @author jtparkin
 *
 */

public class DummyAuthWrapper extends HttpServletRequestWrapper
{
    private String user;

    public DummyAuthWrapper(HttpServletRequest req)
    {
	super(req);
    }

    @Override
    public String getRemoteUser()
    {
	return user;
    }

    public void setRemoteUser(String s)
    {
        user = s;
        return;
    }
}
