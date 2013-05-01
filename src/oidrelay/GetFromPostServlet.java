/*
Copyright (c) 2013 Nicholas Wilson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package oidrelay;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
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
 * pattern of a servlet which accepts both GET and POST connections, and each
 * GET connection is assigned a unique token sent to the client, which then
 * waits until the server sends one final reply. The server completes the
 * request upon receiving a POST connection which contains the same token.
 *
 * The design uses a small thread pool to service requests. This may allow a
 * little more concurrency than simply doing it all in one thread and seems more
 * idiomatic Java. To work around the problems of blocking writes using the
 * servlet API, we have an auxiliary thread that runs timers to interrupt writes
 * if they are suspected of blocking (50ms seems a good guess).
 *
 * Supposedly, the actions and format of the any output is farmed out through
 * some interfaces TokenFactory and RequestHandler, so this servlet simply
 * implements the pattern without specifying a URL structure or particular
 * application. The package includes two examples providing specific behaviour,
 * one for test purposes and another implementing logic to act as an OpenId
 * relay node. In reality, the callbacks are not very generic and the servlet
 * will probably need tweaking to fit any other applications.
 *
 * @author ncw
 *
 */
@WebServlet(asyncSupported = true)
public class GetFromPostServlet extends HttpServlet
{
    private static final long serialVersionUID = -2711207450029928660L;

    public interface RequestHandler {
        public void formatPostResult(HttpServletResponse resp, boolean dispatched)
                throws IOException;
        public String formatPostMessage();
        public void formatReplyHeader(ServletResponse resp, String endpoint)
                throws IOException;
        public void formatReplyKeepalive(ServletResponse resp, int n)
                throws IOException;
        public void formatReplyComplete(ServletResponse resp, String message)
                throws IOException;
        public boolean validateReplyRequest(HttpServletRequest req,
                                            HttpServletResponse resp)
                throws IOException;
    }

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
        public TokenWithEndpoint extractToken(String reqURI);
    }

    protected RequestHandler requestHandler = null;
    protected TokenFactory tokenFactory = null;
    protected int ioTimeoutInterval = 50;
    protected int keepaliveInterval = 10;
    protected int maxKeepalive = 5*60*1000/keepaliveInterval;
    protected int threadPoolCore = 5;

    // GenericServlet method
    @Override
    public void destroy()
    {
        pool.closeTasks();
        pool.shutdown();
        super.destroy();
    }

    protected class Job implements Runnable {
        final protected AsyncContext async;
        final protected TokenWithEndpoint token;
        protected JobPool pool;
        protected String baseURL;
        protected String message = null;
        protected Future<?> task = null;
        protected int numRuns = 0;
        public TokenWithEndpoint getToken() { return token; }
        public Job(AsyncContext async_, TokenWithEndpoint token_,
                   JobPool pool_, String baseURL_) {
            async = async_;
            token = token_;
            pool = pool_;
            baseURL = baseURL_;
        }

        class JobPoolPtr { public JobPool p; JobPoolPtr(JobPool p_) { p = p_; } }
        // run2() returns a value via the JobPoolPtr even if it exits with an
        // uncaught exception
        protected synchronized void run2(JobPoolPtr ptr) {
            // Return this on the stack so we can report back to the JobPool in
            // a thread-safe way
            ptr.p = null;
            boolean commit = false;
            Future<?> ioInterrupter = null;
            try {
                if (pool != null)
                    try {
                        ioInterrupter = pool.scheduleCancel(task, ioTimeoutInterval);
                    } catch (RejectedExecutionException e) {
                        // If we can't schedule a interrupter (or pool is null)
                        // be lenient and just carry on (client write will
                        // time out after 30s).
                    }
                ServletResponse resp = async.getResponse();
                boolean onFirstRun = (numRuns == 0);
                if (onFirstRun)
                    requestHandler.formatReplyHeader(resp,
                                                     baseURL+token.endpointToken);
                ++numRuns;
                if (pool == null && message == null)
                    message = "FAILED: server shutdown";
                if (numRuns > maxKeepalive && message == null)
                    message = "FAILED: request timed out";

                if (message != null) {
                    requestHandler.formatReplyComplete(resp, message);
                    commit = true;
                } else if (!onFirstRun) {
                    requestHandler.formatReplyKeepalive(resp, numRuns);
                }
                resp.getOutputStream().flush();
            } catch (IOException e) {
                // handle in "finally"
                commit = true;
            } finally {
                if (ioInterrupter != null) {
                    ioInterrupter.cancel(true);
                    while (true) {
                        try {
                            ioInterrupter.get();
                        } catch (InterruptedException e) {
                            continue;
                        } catch (CancellationException e) {
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                if (commit) {
                    async.complete();
                    ptr.p = pool;
                }
                // whatever we were doing, we did it so can be scheduled again
                task = null;
            }
        }

        public void run() {
            // A synchronized method on Job must never wait on the JobPool as
            // this would cause deadlock. We run the job, then report back the
            // state at any time afterwards.
            JobPoolPtr lp = new JobPoolPtr(null);
            try {
                run2(lp);
            } finally {
                if (lp.p != null)
                    lp.p.completeJob(this);
            }
        }
        
        public synchronized void addToPool(JobPool p) {
            if (task == null)
                task = p.submit(this);
        }

        public synchronized void orphan() { pool = null; }
        public synchronized void setMessage(String m) { message = m; }
    }

    protected class JobPool {
        final protected ScheduledThreadPoolExecutor scheduler;
        final protected ScheduledThreadPoolExecutor interruptor;
        JobPool(int corePoolSize) {
            scheduler = new ScheduledThreadPoolExecutor(corePoolSize);
            interruptor = new ScheduledThreadPoolExecutor(1);
         }
        ScheduledFuture<?> keepaliveTimer = null;
        final protected HashMap<String,Job> jobs = new HashMap<String,Job>();

        public Future<?> submit(Runnable task)
        { // XXX throw something fat if we exceed a certain queue size
          return scheduler.submit(task); }
        
        public Future<?> scheduleCancel(final Future<?> task, int timeout) {
            return interruptor.schedule(new Runnable() {
                public void run() {
                    task.cancel(true);
                }
            }, timeout, TimeUnit.MILLISECONDS);
        }

        public void shutdown()
        { interruptor.shutdown(); scheduler.shutdown(); }

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
                keepaliveTimer = scheduler.scheduleWithFixedDelay(new Runnable() {
                    public void run() { JobPool.this.doKeepalive(); }
                }, 1000, keepaliveInterval, TimeUnit.MILLISECONDS);
            }
        }

        public synchronized void completeJob(Job job) {
            jobs.remove(job.getToken().token);
            if (jobs.size() == 0 && keepaliveTimer != null) {
                keepaliveTimer.cancel(false);
                keepaliveTimer = null;
            }
        }
    }
    final protected JobPool pool = new JobPool(threadPoolCore);

    // (req.getPathInfo() is apparently broken in various containers...)
    /** @return "/path-under-servlet" */
    protected static String getRelUri(HttpServletRequest req)
    { String ctxPath = req.getContextPath();
      String reqURI = req.getRequestURI();
      return ctxPath.length() <= reqURI.length() ?
              reqURI.substring(ctxPath.length()) : null; }
    /** @return "http://name/servlet-context" */
    protected static String getBaseUrl(HttpServletRequest req)
    { StringBuffer baseURL = req.getRequestURL();
      baseURL.delete(baseURL.length()-getRelUri(req).length(), baseURL.length());
      return baseURL.toString(); }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        if (!"HTTP/1.1".equals(req.getProtocol())) {
            // We absolutely require chunked encoding, which is only optional in
            // HTTP/1.0, so do the simplest check.
            resp.sendError(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED);
            return;
        }
        if (!requestHandler.validateReplyRequest(req, resp))
            return;

        TokenWithEndpoint tok = tokenFactory.mintToken(req);

        AsyncContext ac = req.startAsync(req, resp);
        ac.setTimeout(0);
        pool.addJob(new Job(ac, tok, pool, getBaseUrl(req)));
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        TokenWithEndpoint tok = tokenFactory.extractToken(getRelUri(req));
        if (tok == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        boolean found = pool.sendResult(tok.token,
                                        requestHandler.formatPostMessage());
        if (!found) resp.setStatus(HttpServletResponse.SC_GONE);

        requestHandler.formatPostResult(resp, found);
    }
}
