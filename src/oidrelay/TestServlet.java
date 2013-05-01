package oidrelay;
import javax.servlet.annotation.WebServlet;

import oidrelay.GetFromPostServlet;

@WebServlet(asyncSupported = true)
public class TestServlet extends GetFromPostServlet
{
    private static final long serialVersionUID = -4179102306415775743L;

    public TestServlet() {
        super();
        tokenFactory = new PerUserTokenFactory("/u/");
    }
    

	/*
	 * The GET request is for the URL /api, the endpoint of a server wishing
	 * to perform relayed OpenID authentication. A request for authentication
	 * is expressed as "GET /api?id=<id>", and the response is a chunked reply.
	 * The lines are:
	 *   1. A line consisting of a URL, which indicates the OP endpoint (one)
	 *   2. Lines "keep-alive..." (zero or more)
	 *   3. A line "OK: <string>" or "FAILED: <string>".
	 * The request must be made with a username set (for example, using BASIC or
	 * DIGEST authentication, as configured).
	public void sendId() {
		// TODO Auto-generated method stub
		
	}
    public void sendKeepalive() {
		// TODO Auto-generated method stub
		
	}
     */
    
}
