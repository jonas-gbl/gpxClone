/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by CollabNet <http://www.collab.net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of CollabNet.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLABNET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of CollabNet.
 */ 
package org.tigris.noodle;

// Don't import .* because we are replacing HttpURLConnection.
//
import java.net.URL;
import java.net.MalformedURLException;

// Replacement for HttpURLConnection.
//
import HTTPClient.*;

import java.io.*;
import java.util.*;

// Java Servlet Classes
//
import javax.servlet.*;
import javax.servlet.http.*;
import org.tigris.noodle.NoodleConstants.Authentication;

/**
 * Proxy a request for Noodle.
 *
 * This is the core class for Noodle; it does the actual proxying of
 * the request.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:stack@collab.net">St.Ack</a>
 * @author <a href="mailto:edk@collab.net">Ed Korthof</a>
 * @version $Id: ProxyModule.java,v 1.8 2003/07/20 23:45:46 dlr Exp $
 */
public class ProxyModule
{
    /** 
     * Debug flag. Set to 'true' to see debug logging.
     */
    private static final boolean DEBUG = false;

    /**
     * The singleton.
     */
    private static ProxyModule defaultProxyModule = new ProxyModule();

    /** 
     * Server to connect to. 
     */
    protected String serverName = "localhost";

    /** 
     * Port to connect to. 
     */
    protected int serverPort = 80;
    
    protected Authentication authenticationType = Authentication.NONE;
    protected String username="";
    protected String password="";
    protected String realm="";

    /**
     * Get the singleton -- the default proxy module.
     */
    public static ProxyModule getInstance()
    {
        return defaultProxyModule;
    }

    /**
     * Default constructor is a no-op -- we have ivar initializers up above.
     */
    public ProxyModule()
    {
    }

    /**
     * Proxy a request.
     *
     * This is the meat of everything. It does the proxy of the request 
     * including applying the pre/post filters when necessary.
     *
     * @param noodleData Noodle Data.  Has what we need to make the request
     *        and its in here that we put the the request results.
     *
     * @exception Exception 
     *
     * @see #proxyRequest(NoodleData, String)
     * @deprecated use getInstance(), instanciate a new ProxyModule, or
     * instanciate a ProxyModule subclass and use the instance method.
     */
    public static final void proxyRequest(NoodleData noodleData)
        throws Exception
    {
        getInstance().proxyRequest(noodleData, null);
    }

    /**
     * Proxy a request.
     *
     * This is the meat of everything. It does the proxy of the request 
     * including applying the pre/post filters when necessary.
     * 
     * @param noodleData Noodle Data.  Has what we need to make the request
     *        and its in here that we put the the request results.
     * @param inSavedOffQuery req.getQueryString can return a null query 
     *        string in circumstance where not logged in to Helm. If the 
     *        caller has squirreled away the original query, pass it here.  
     *        May be null.
     * 
     * @exception Exception
     */
    public final void proxyRequest(NoodleData noodleData,
                                   String inSavedOffQuery)
        throws Exception
    {
        HttpServletRequest req = noodleData.getClientRequest();

        //The best use of Noodle; try to find the page in an HTTP
        //attribute.
        String page = (String)req.getAttribute(NoodleConstants.PAGE_ATTRIBUTE);
/*
        String query = getQueryString(req, inSavedOffQuery);
        if (page == null)
        {            
            //They don't have a setup that puts the page into a request
            //attribute, so try to get it from a 'page' value in the query.
            page = getValueFromQueryString(NoodleConstants.PAGE_VAR,
                                           query);

            //Set the 'real' query data to be the sanitized version.
            query = stripQueryString(query, NoodleConstants.PAGE_VAR);
        }        
        noodleData.setQueryData(query);
*/
        page = noodleData.postProcessPageName(page);
        if (page == null)
        {
            throw new Exception("No page specified to proxy!");
        }
        URL url = getURL(page);
        noodleData.setURL(url);
        StringBuffer proxyURI = new StringBuffer(url.getFile());
        /*
        if(query.length() > 0)
        {
            proxyURI.append('?');
            proxyURI.append(query);
        }
        */
        
        if(noodleData.getQueryData().length() > 0)
        {
            proxyURI.append('?');
            proxyURI.append(noodleData.getQueryData());
        }        
        
        // Proxy.
        // 
        if (DEBUG)
        {
            System.out.println("Accessing " + url);
            System.out.println("Accessing " + proxyURI.toString());
        }
        HTTPResponse proxyResponse = proxyRequestMethod(req, url, 
            proxyURI.toString(), noodleData.getHeadersToSendAsNVPair(), 
            noodleData.getPostData());
        noodleData.setProxyResponse(proxyResponse);
    }

    /** 
     * Do the actual proxy w/ appropriate method (GET, POST, etc.).
     *
     * @param inRequest The servlet request object.
     * @param inURL Basic url w/ host and port information.  Used to 
     *        set up the HTTPClient connection.
     * @param inProxyURI File we're to request along w/ whats left of
     *        query string after stripping page to request (If any).
     * @param inHeaders2Send HTTP headers to send on the proxy request.
     * @param inPostData Data to use w/ POST requests, if thats what
     *        we're doing here.
     *
     * @return A HTTPClient HTTPResponse object.
     *
     * @exception IOException
     * @exception ModuleException
     * @exception java.net.ProtocolException If we get a method request
     *            that we do not support.
     *
     */ 
    protected HTTPResponse proxyRequestMethod(
        HttpServletRequest inRequest, URL inURL, String inProxyURI, 
            NVPair[] inHeaders2Send, byte[] inPostData)
        throws java.net.ProtocolException, IOException, ModuleException
    {
        HTTPResponse httpResponse = null;
        String emptyStr = new String();

        HTTPConnection conn = new HTTPConnection(inURL);
        
        if(authenticationType==Authentication.BASIC)
        {
            conn.addBasicAuthorization(realm, username, password);
        }
        else if(authenticationType==Authentication.DIGEST)
        {
            conn.addDigestAuthorization(realm, username, password);
        }

        if(inRequest.getMethod().equalsIgnoreCase(NoodleConstants.GET))
        {
            httpResponse = conn.Get(inProxyURI, emptyStr, inHeaders2Send);
        }
        else if(inRequest.getMethod().equalsIgnoreCase(NoodleConstants.POST))
        {
            httpResponse = conn.Post(inProxyURI, inPostData, inHeaders2Send);
        }
        else if(inRequest.getMethod().equalsIgnoreCase(NoodleConstants.HEAD))
        {
            httpResponse = conn.Head(inProxyURI, emptyStr, inHeaders2Send);
        }
        else if(inRequest.getMethod().equalsIgnoreCase(NoodleConstants.PUT))
        {
            httpResponse = conn.Put(inProxyURI, emptyStr, inHeaders2Send);
        }
        else
        {
            throw new java.net.ProtocolException("UNSUPPORTED HTTP method: "
                + inRequest.getMethod());
        }

        return httpResponse;
    }

    /** 
     * Get the query string.
     *
     * inRequest.getQueryString() can return null.  Put in check for null and 
     * use alternate query string source, if one supplied.
     *
     * @param inRequest Our incoming request.
     * @parm inSavedOffQuery Last chance query string.  May be null.
     *
     * @return The query string.
     *
     * @exception NullPointerException Throw NPE if we can't get query string.
     */
    protected String getQueryString(HttpServletRequest inRequest,
                                    String inSavedOffQuery) 
        throws NullPointerException
    {
        String query = inRequest.getQueryString();

        if(query == null)
        {
            query = inSavedOffQuery;
            if(query == null)
            {
                query = "";
            }
        }

        if(DEBUG)
        {
            System.out.println("QUERY: " + query);
        }

        return query.trim();
    }

    /** 
     * Get value from the passed in query string.
     *
     * @param inKey Key we use pulling value from inQueryStr.
     * @param inQueryStr Query string to pull value on the end of inKey from.
     *
     * @exception NullPointerException If we can't find inKey value in passed
     *            string.
     */
    protected String getValueFromQueryString(String inKey,
                                             String inQueryStr)
        throws NullPointerException
    {
        String strReturn = null; // PAGE_VAR argument to return
        int indexStart = -1;
        int indexEnd = -1;
        
        if(inQueryStr == null)
        {
            throw new NullPointerException("inQueryStr is null");
        }
        indexStart = inQueryStr.indexOf(inKey);
        if(indexStart < 0) 
        {
            // A bit of misuse of NullPointerException
            throw new NullPointerException
                (inKey + " not specified in inQueryStr");
        }
        indexStart += inKey.length(); // Put index past inKey
        indexStart += 1; // Put index past the '='
        // End of inKey is at end of string or at first first occurance past 
        // current index of a '&' -- the query args seperator.
        //
        indexEnd = inQueryStr.indexOf('&', indexStart);
        if(indexEnd > 0)
        {
            strReturn = inQueryStr.substring(indexStart, indexEnd);
        }
        else
        {
            strReturn = inQueryStr.substring(indexStart); 
        }
        return strReturn;
    }

    /**
     * Strip key and its value from the querystring.
     *
     * @param inQueryString Query string.
     *
     * @return New query string absent 'page=VALUE'.
     */
    public final String stripQueryString(String inQueryString, 
                                         String inKey)
    {
        if (DEBUG)
        {
            System.out.println("Stripping '" + inQueryString + "'" + " from '" + inKey + "'");
        }
        if (inQueryString == null || inQueryString.length() == 0)
        {
            return inQueryString;
        }
        StringBuffer sb = new StringBuffer(inQueryString);
        int start = inQueryString.indexOf(inKey + "=");
        int end = inQueryString.indexOf("&", start);
        if(end == -1)
        {
            end = inQueryString.length();
        }
        if(start > 0)
        {
            start = start - 1;
        }
        else if(start == 0 && ((end + 1) < inQueryString.length()))
        {
            end = end + 1;
        }
        if (start != -1 && end != -1)
        {
            return inQueryString.substring(0, start) +
                inQueryString.substring(end, inQueryString.length());
        }
        else
        {
            return inQueryString;
        }
    }

    /**
     * Parse the protocol.
     *
     * Not used yet, but here for future reference
     *
     * @param value Protocol to parse.
     *
     * @return 
     *
     */
    protected String parseProtocol(String value)
    {
        return value.substring(0,value.indexOf("/"));
    }

    /**
     * Override the default server name ("localhost").        
     *
     * @param name Server name
     */
    public void setServerName(String name)
    {
        serverName = name;
    }

    /**
     * Override the default server port (80).  
     * 
     * @param port Port to use.
     */ 
    public void setServerPort(int port) 
    { 
        serverPort = port; 
    } 
    
    /** 
     * URL for making the connection to the remote location.
     * 
     * @param page The page param must be an URLEncoded string.
     *
     * @return Returns page as an url.
     *
     * @exception MalformedURLException
     */
    public URL getURL(String page) throws MalformedURLException
    {
        if (page == null)
        {
            throw new MalformedURLException("Noodle page variable not found");
        }

        return new URL(NoodleConstants.SCHEME, serverName, serverPort, page);
    }
    
    public void setCredentials(String username,String password,String realm,Authentication authType)
    {
        this.authenticationType=authType;
        this.username=username;
        this.password=password;
        this.realm=realm;
    }

    /** 
     * Read all data from stream even if total length is unknown.
     *
     * Reads 4096 bytes at a time.
     *
     * @param is Stream to read from.
     *
     * @return Byte array of all read from stream.
     *
     * @exception IOException
     */
    public static byte[] readFully (InputStream is)
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] block = new byte[4096];
        int count = 0;
        while ((count = is.read(block)) != -1)
        {
            baos.write(block, 0, count);
        }
        baos.close();
        return baos.toByteArray();
    }
}    
