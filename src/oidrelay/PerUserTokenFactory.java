package oidrelay;
import oidrelay.GetFromPostServlet.TokenFactory;
import oidrelay.GetFromPostServlet.TokenWithEndpoint;

import java.util.Random;
import javax.xml.bind.DatatypeConverter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Out version of the TokenFactory scopes requests by user id. Each GET
 * connection must come with an associated authentication (eg. HTTP
 * authentication such that HttpServletRequest.getRemoteUser() returns something
 * identifying a user). The data POSTed to us then are scoped per-user.
 * 
 * The idea is that a user might authenticate a web-service to POST data to
 * sub-URLs of the scoped endpoint, happy in the knowledge that the data sent
 * by the web service will only ever be disclosed to pending GET requests that
 * were authenticated to receive that per-user data.
 */
class PerUserTokenFactory implements TokenFactory {
    public static final int idLen = 16;
    private String urlToMap;
    public PerUserTokenFactory(String urlToMap_)
    {
        urlToMap = urlToMap_;
        if (!urlToMap.endsWith("/")) urlToMap = urlToMap+"/";
        if (!urlToMap.startsWith("/")) urlToMap = "/"+urlToMap;
    }

    public TokenWithEndpoint mintToken(HttpServletRequest req)
    throws ServletException
    {
        // Enforce the fact that URL mappings are set up so that API calls can
        // only possibly come in if the user is authenticated
        if (req.getRemoteUser() == null)
            throw new ServletException("User must be authenticated");

        String user = req.getRemoteUser();

        byte[] id = new byte[idLen];
        new Random().nextBytes(id);
        StringBuilder sb = new StringBuilder();
        for (byte b : id) sb.append(String.format("%02x", b));
        String strId = sb.toString();

        return new TokenWithEndpoint(user+"/"+strId,
                                     urlToMap+user+"/return-for-"+strId,
                                     urlToMap+user+"/");
    }

    public TokenWithEndpoint extractToken(HttpServletRequest req)
    {
        String ctxPath = req.getContextPath();
        String reqURI = req.getRequestURI();
        reqURI = ctxPath.length() <= reqURI.length() ?
                    reqURI.substring(ctxPath.length()) : null;
        String user = null;
        String idStr = null;
        byte[] id = null;
        if (reqURI != null) {
            String[] parts = reqURI.split("/");
            String[] mapParts = urlToMap.split("/");
            if (parts.length == 3 + (mapParts.length-2) &&
                "".equals(parts[0]) && reqURI.startsWith(urlToMap))
            {
                user = parts[parts.length-2];
                idStr = parts[parts.length-1];
                if (idStr.startsWith("return-for-")) {
                    idStr = idStr.substring("return-for-".length(),
                                            idStr.length());
                    try {
                        id = DatatypeConverter.parseHexBinary(idStr);
                        if (id.length != idLen)
                            id = null;
                    } catch (IllegalArgumentException e) { }
                }
            }
        }
        if (user == null || id == null) {
            return null;
        }
        return new TokenWithEndpoint(user+"/"+idStr,
                                     urlToMap+user+"/return-for-"+idStr,
                                     urlToMap+user+"/");
    }
}
