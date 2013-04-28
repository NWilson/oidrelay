import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Random;
import java.net.URLEncoder;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.bind.DatatypeConverter;

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
    
    private static String escape(String s)
    { return s.replaceAll("&","&amp;").replaceAll("<","&lt;")
    	.replaceAll(">","&gt;").replaceAll("\"","&quot;").replaceAll("'", "&apos;"); }
	
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
		protected String user;
		protected byte[] id;
		public static final int idLen = 16;
		boolean haveResponse = false;
		String message;
		public UserToken(String user_) {
			user = user_;
			id = new byte[idLen];
			new Random().nextBytes(id);
		}
		public UserToken(String user_, byte[] id_) {
			user = user_;
			id = id_;
		}
		public String getEndpoint() {
		    StringBuilder sb = new StringBuilder();
		    sb.append("/u/"+user+"/return-for-");
		    for (byte b : id) sb.append(String.format("%02x", b));
		    return sb.toString();
		}
		boolean	equals(UserToken ut) {
			return getEndpoint().equals(ut.getEndpoint());
		}
	}
	private ArrayList<UserToken> tokens = new ArrayList<UserToken>();

	/**
	 * The GET request is for the URL /api, the endpoint of a server wishing
	 * to perform relayed OpenID authentication. A request for authentication
	 * is expressed as "GET /api?id=<id>", and the response is a chunked reply.
	 * The lines are:
	 *   1. A line consisting of a URL, which indicates the OP endpoint (one)
	 *   2. Lines "keep-alive..." (zero or more)
	 *   3. A line "OK: <string>" or "FAILED: <string>".
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
        	tokens.add(tok);
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
	        // Some junk to get browsers to do incremental rendering straight away
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
	        String iframe = "<!DOCTYPE html><html><body>"+
	            "<form method='POST' action=\""+escape(endpoint)+"\">"+
	            "<input type='submit' value='Go to auth'></form></body></html>";
	        out.println("<a href='data:text/html;charset=utf-8,"+
	        		URLEncoder.encode(iframe,"UTF-8").replaceAll("\\+", "%20")+"'>"+
	        		"POST to "+escape(endpoint)+"</a>");
	        out.flush();
	        
	        long until = System.currentTimeMillis() + 10*60*1000;
	        String message;
	        while (true) {
		        synchronized (shuttingDown) {
		        	try {
		        		shuttingDown.wait(2000);
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
		        if (out.checkError()) {
		        	message = "FAILED: client went away";
		        	break;
		        }
		        if (System.currentTimeMillis() > until) {
		        	message = "FAILED: took too long to call back";
		        	break;
		        }
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
        
		String ctxPath = req.getContextPath();
		String reqURI = req.getRequestURI();
		reqURI = ctxPath.length() <= reqURI.length() ?
		           reqURI.substring(ctxPath.length()) : null;
		String user = null;
		byte[] id = null;
		if (reqURI != null) {
			String[] parts = reqURI.split("/");
			if (parts.length == 4 &&
			    "".equals(parts[0]) && "u".equals(parts[1]))
			{
				user = parts[2];
				String idStr = parts[3];
			    if (idStr.startsWith("return-for-")) {
					String s = idStr.substring("return-for-".length(),
							                   idStr.length());
					try {
						id = DatatypeConverter.parseHexBinary(s);
						if (id.length != UserToken.idLen)
							id = null;
					} catch (IllegalArgumentException e) { }
			    }
			}
		}
		if (user == null || id == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

        PrintWriter out = resp.getWriter();
        resp.setCharacterEncoding("UTF-8");
        out.println("<html>");
        out.println("<body>");
        out.println("<p>Parsed user=\""+escape(user)+"\", id=\""+escape(id.toString())+"\"");

        boolean alert = false;
        synchronized (tokens) {
        	UserToken tok_ = new UserToken(user, id);
        	boolean found = false;
        	for (UserToken i: tokens) {
        	    out.println("<p>Stashed token: \""+i.getEndpoint()+"\"");
        	    if (tok_.equals(i)) {
        	    	found = true;
	        		i.haveResponse = true;
	        		i.message = "OK: dummy result for now";
	        		alert = true;
	        		out.println("<p>Setting auth as accepted.");
        	    }
        	}
    	    //out.println("<p>Searching token: "+tok_.getEndpoint());
        	//int i = tokens.indexOf(tok_);
        	if (!found)
        		out.println("<p>Fatal error, token not found.");
        }
        if (alert) {
			synchronized (shuttingDown) {
				shuttingDown.notifyAll();
			}
        }

        out.println("</body>");
        out.println("</html>");
    }
}
