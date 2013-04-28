import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;
import java.util.LinkedList;
import javax.servlet.*;
import javax.servlet.http.*;

public class RelayServlet extends HttpServlet
{
	private static final long serialVersionUID = -2711207450029928660L;
    private AtomicInteger threadCounter = new AtomicInteger(0);
    protected void enteringServiceMethod() { threadCounter.incrementAndGet(); }
    protected void leavingServiceMethod() {
    	synchronized (threadCounter) {
			threadCounter.decrementAndGet();
			threadCounter.notify();
    	}
    }
    // shuttingDown is used to signal the end of requests, both by the servlet
    // manager when it's aborting everything, and when a new response comes in.
    // Every thread is woken when each response comes in (broadcast).
    private AtomicInteger shuttingDown = new AtomicInteger(0);
	
	public void service(ServletRequest req, ServletResponse resp)
		throws ServletException,IOException
	{
	    enteringServiceMethod();
	    try {
	        super.service(req, resp);
	    } finally {
	        leavingServiceMethod();
	    }
	}
	
	public void destroy()
	{
	    synchronized (shuttingDown) {
			shuttingDown.set(1);
			shuttingDown.notifyAll();
		}
        synchronized (threadCounter) {
		    while (threadCounter.get() > 0) {
		    	try {
					threadCounter.wait(500);
				} catch (InterruptedException e) { }
		    }
        }
	}
	
	public class UserToken {
		public String user;
		byte[] id;
		boolean haveResponse = false;
		String message;
		public UserToken(String user_) {
			user = user_;
			id = new byte[16];
			new Random().nextBytes(id);
		}
		public String getEndpoint() {
		    StringBuilder sb = new StringBuilder();
		    sb.append("/"+user+"/return-for-");
		    for (byte b : id) sb.append(String.format("%02x", b));
		    return sb.toString();
		}
	}
	private LinkedList<UserToken> tokens = new LinkedList<UserToken>();

	/**
	 * The GET request is for the URL /api, the endpoint of a server wishing
	 * to perform relayed OpenID authentication. A request for authentication
	 * is expressed as "GET /api?id=<id>", and the response is a chunked reply.
	 * The lines are:
	 *   1. A line consisting of a URL, which indicates the OP endpoint (one)
	 *   2. Lines "keep-alive..." (zero or more)
	 *   3. A line "OK <string>" or "FAILED <string>".
	 * The request must be made with a username set (for example, using BASIC or
	 * DIGEST authentication, as configured).
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
		String ctxPath = req.getContextPath();
		String reqURI = req.getRequestURI();
		reqURI = ctxPath.length() <= reqURI.length() ?
		           reqURI.substring(ctxPath.length()) : null;
		// req.getPathInfo() is apparently broken in jetty
		if (!"/api".equals(reqURI)) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (!"HTTP/1.1".equals(req.getProtocol())) {
			// We absolutely require chunked encoding, which is only optional in
			// HTTP/1.0, so do the simplest check.
			resp.sendError(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED);
			return;
		}
        String id = req.getParameter("id");
        if (id == null) {
        	resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        	return;
        }
		
		// Enforce web.xml is set up so that API calls can only come from a user
        assert req.getRemoteUser() != null;
        UserToken tok = new UserToken(req.getRemoteUser());
        synchronized (tokens) {
        	tokens.addLast(tok);
        }
        String endpoint;
        {
        	StringBuffer sb = req.getRequestURL();
        	sb.replace(sb.length()-reqURI.length(), sb.length(), tok.getEndpoint());
        	endpoint = sb.toString();
        }
        
        try {
	        PrintWriter out = resp.getWriter();
	        //resp.setContentType("text/plain");
	        resp.setCharacterEncoding("UTF-8");
	
	        out.println("<!DOCTYPE html>");
	        out.println("<html>");
	        out.println("<body>");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<!-- juuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuk -->");
	        out.println("<p>" + endpoint);
	        out.flush();
	        
	        String message;
	        while (true) {
		        synchronized (shuttingDown) {
		        	try {
		        		shuttingDown.wait(500);
		        	} catch (InterruptedException e) { }
		        	if (shuttingDown.get() == 1) {
		        		message = "FAILED: server shutting down";
		        		break;
		        	}
		        }
		        synchronized (tokens) {
		        	if (tok.haveResponse) {
		        		message = tok.message;
		        		break;
		        	}
		        }
		        out.println("<p>...keep-alive");
		        out.flush();
	        }
	
	        out.println("<p>"+message);
	        out.println("</body>");
	        out.println("</html>");
	        out.flush();
        } finally {
        	synchronized (tokens) {
        		tokens.remove(tok);
        	}
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
    	/*
		String ctxPath = req.getContextPath();
		String reqURI = req.getRequestURI();
		reqURI = ctxPath.length() <= reqURI.length() ?
		           reqURI.substring(ctxPath.length()) : null;
		// req.getPathInfo() is apparently broken in jetty
		if (!"/api".equals(reqURI)) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (!"HTTP/1.1".equals(req.getProtocol())) {
			// We absolutely require chunked encoding, which is only optional in
			// HTTP/1.0, so do the simplest check.
			resp.sendError(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED);
			return;
		}
        String id = req.getParameter("id");
        if (id == null) {
        	resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        	return;
        }
		

        out.println("<html>");
        out.println("<body>");
        out.println("You entered \"" + field + "\" into the text box.");
        out.println("</body>");
        out.println("</html>");
        */
    }
}
