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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oidrelay.GetFromPostServlet;

@WebServlet(asyncSupported = true)
public class TestServlet extends GetFromPostServlet
{
    private static final long serialVersionUID = -4179102306415775743L;

    private static String escape(String s)
    { return s.replaceAll("&","&amp;").replaceAll("<","&lt;")
        .replaceAll(">","&gt;").replaceAll("\"","&quot;").replaceAll("'", "&apos;"); }

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
    public TestServlet() {
        super();
        tokenFactory = new PerUserTokenFactory("/u/");
        requestHandler = new HtmlOutputFormatter("/api");
    }
    
    protected class HtmlOutputFormatter implements RequestHandler {
        protected String apiUri;
        public HtmlOutputFormatter(String apiUri_) { apiUri = apiUri_; }

        public void formatPostResult(HttpServletResponse resp, boolean dispatched)
                throws IOException
        {
            ServletOutputStream out = resp.getOutputStream();
            resp.setCharacterEncoding("UTF-8");
            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>POST result</title></head>");
            out.println("<body>");
            String message = dispatched ? "Setting auth as accepted."
                                        : "Fatal error, token not found.";
            out.println("<p>"+message);
            out.println("</body></html>");
        }

        public String formatPostMessage() { return "OK: dummy result for now"; }

        public void formatReplyHeader(ServletResponse resp, String endpoint)
                throws IOException
        {
            resp.setCharacterEncoding("UTF-8");
            ServletOutputStream out = resp.getOutputStream();

            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>Requesting</title></head>");
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
            try {
                out.println("<a href='data:text/html;charset=utf-8,"+
                        URLEncoder.encode(iframe,"UTF-8").replaceAll("\\+", "%20")+"'>"+
                        "POST to "+escape(endpoint)+"</a>");
            } catch (UnsupportedEncodingException e) { e.printStackTrace(); }
        }

        public void formatReplyKeepalive(ServletResponse resp, int n)
                throws IOException
        {
            ServletOutputStream out = resp.getOutputStream();
            for (int i = 0; i < n; ++i)
                out.println("<p>...keep-alive"+i);
        }

        public void formatReplyComplete(ServletResponse resp, String message)
                throws IOException
        {
            ServletOutputStream out = resp.getOutputStream();
            out.println("<p>"+message);
            out.println("</body>");
            out.println("</html>");
        }

        public boolean validateReplyRequest(HttpServletRequest req,
                                            HttpServletResponse resp)
                throws IOException
        {
            String relUri = getRelUri(req);
            if (!apiUri.equals(relUri)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }
            String id = req.getParameter("id");
            if (id == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return false;
            }
            return true;
        }

    }

}
