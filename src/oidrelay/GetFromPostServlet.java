package oidrelay;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A GetFromPostServlet is a relatively simple servlet that implements the
 * pattern of a servlet which accepts both GET and POST connections. Each GET
 * connection is assigned a unique token sent to the client, which then waits
 * until the server sends one final reply containing the outcome of a POST
 * connection which contains the same token.
 * 
 * The design uses a small thread pool to service requests and a helper thread
 * for running timers. This may allow a little more concurrency than simply
 * doing it all in one thread and seems more idiomatic Java.
 * 
 * @author ncw
 *
 */
@WebServlet(asyncSupported = true)
public class GetFromPostServlet extends HttpServlet
{
    private static final long serialVersionUID = -2711207450029928660L;

    /*
    public interface RequestHandler {
        public void sendKeepalive();
        public void sendId();
    }
    */

    static public class TokenWithEndpoint {
        TokenWithEndpoint(String t, String eT, String eS)
        { token = t; endpointToken = eT; endpointScope = eS; }
        final public String token;
        final public String endpointToken;
        final public String endpointScope;
    }
    static public interface TokenFactory {
        /** Used to create a token for a GET request. */
        public TokenWithEndpoint mintToken(HttpServletRequest req)
        throws ServletException;
        /** Used to extract a token from the POST request parameters.
         *  @return null if the URL is not mapped to the token factory.
         */
        public TokenWithEndpoint extractToken(HttpServletRequest req);
    }

    //protected RequestHandler requestHandler = null;
    protected TokenFactory tokenFactory = null;

    // GenericServlet method
    @Override
    public void destroy()
    {
        pool.closeTasks();
        pool.shutdown();
    }

    protected class Job implements Runnable {
        final protected AsyncContext async;
        final protected TokenWithEndpoint token;
        protected JobPool pool;
        protected String message = null;
        protected boolean firstRun = true;
        protected Future<?> task = null;
        public TokenWithEndpoint getToken() { return token; }
        public Job(AsyncContext async_, TokenWithEndpoint token_, JobPool po_) {
            async = async_;
            token = token_;
            pool = po_;
        }

        @Override
        public synchronized void run() {
            try {
                ServletResponse resp = async.getResponse();
                PrintWriter out = resp.getWriter();
                boolean onFirstRun = firstRun;
                if (onFirstRun) {
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
                        "<form method='POST' action=\""+escape(token.endpointToken)+"\">"+
                        "<input type='submit' value='Go to auth'></form></body></html>";
                    try {
                        out.println("<a href='data:text/html;charset=utf-8,"+
                                URLEncoder.encode(iframe,"UTF-8").replaceAll("\\+", "%20")+"'>"+
                                "POST to "+escape(token.endpointToken)+"</a>");
                    } catch (UnsupportedEncodingException e) { e.printStackTrace(); }
                    firstRun = false;
                }
                if (pool == null && message == null)
                    message = "FAIL: server shutdown";
                
                if (out.checkError() && message == null)
                    message = "FAILED: client went away";
                
                if (message != null) {
                    out.println("<p>"+message);
                    out.println("</body>");
                    out.println("</html>");
                    out.flush();
                    
                } else if (!onFirstRun) {
                    out.println("<p>...keep-alive");
                    out.flush();
                } else {
                    out.flush();
                }
            } catch (IOException e) {
                /* handle in finally */
            } finally {
                if (firstRun || message != null) {
                    async.complete();
                    if (pool != null)
                        pool.completeJob(this);
                }
                // whatever we were doing, we did it so can be scheduled again
                task = null;
            }

        }
        
        public synchronized void addToPool(ScheduledThreadPoolExecutor p) {
            if (task == null)
                task = p.submit(this);
        }

        public synchronized void orphan() { pool = null; }
        public synchronized void setMessage(String m) { message = m; }
    }
    protected class JobPool extends ScheduledThreadPoolExecutor {
        JobPool(int corePoolSize) { super(corePoolSize); }
        ScheduledFuture<?> keepaliveTimer = null;
        final protected HashMap<String,Job> jobs = new HashMap<String,Job>();

        public synchronized void closeTasks() {
            for (Job job : jobs.values()) {
                job.orphan();
                job.addToPool(this);
            }
            jobs.clear();
        }

        public synchronized void doKeepalive() {
            for (Job job : jobs.values()) {
                job.addToPool(this);
            }
        }
        
        public synchronized boolean sendResult(String token, String message) {
            Job job = jobs.get(token);
            if (job != null) {
                job.setMessage(message);
                job.addToPool(this);
            }
            return job != null;
        }

        public synchronized void addJob(Job job) {
            jobs.put(job.getToken().token, job);
            job.addToPool(this);
            if (keepaliveTimer == null) {
                keepaliveTimer = this.scheduleWithFixedDelay(new Runnable() {
                    public void run() { JobPool.this.doKeepalive(); }
                }, 5, 5, TimeUnit.SECONDS);
            }
        }
        
        public synchronized void completeJob(Job job) {
            jobs.remove(job.getToken().token);
            if (jobs.size() == 0) {
                keepaliveTimer.cancel(false);
                keepaliveTimer = null;
            }
        }
    }
    final protected JobPool pool = new JobPool(5);


    private static String escape(String s)
    { return s.replaceAll("&","&amp;").replaceAll("<","&lt;")
        .replaceAll(">","&gt;").replaceAll("\"","&quot;").replaceAll("'", "&apos;"); }
    
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

        TokenWithEndpoint tok = tokenFactory.mintToken(req);

        AsyncContext ac = req.startAsync(req, resp);
        ac.setTimeout(0);
        pool.addJob(new Job(ac, tok, pool));
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        TokenWithEndpoint tok = tokenFactory.extractToken(req);
        if (tok == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        boolean found = pool.sendResult(tok.token, "OK: dummy result for now");

        PrintWriter out = resp.getWriter();
        resp.setCharacterEncoding("UTF-8");
        out.println("<html>");
        out.println("<body>");
        out.println("<p>id=\""+escape(tok.token)+"\"");
        
        if (found) {
            out.println("<p>Setting auth as accepted.");
        } else {
            out.println("<p>Fatal error, token not found.");
        }
        out.println("</body>");
        out.println("</html>");
    }
}
