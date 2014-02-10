/* ================================================================
 * Copyright (c) 2000-2003 Collabnet.  All rights reserved.
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
 * software developed by Collabnet <http://www.collab.net/>."
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

package org.tigris.noodle.filters;

import java.text.*;
import java.util.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

import HTTPClient.HTTPResponse;
 
import org.tigris.noodle.NoodleResponseFilter;
import org.tigris.noodle.NoodleData;
import org.tigris.noodle.ResponseBlock;

/**
 * This filter will copy the Set-Cookie: and Set-Cookie2: headers
 * from the proxy connection response to the HttpServletResponse.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:leonardr@collab.net">Leonard Richardson</a>
 */
public class CopyCookies implements NoodleResponseFilter
{
    private static SimpleDateFormat cookieDate =
        new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zz", Locale.US );

    static
    {
        cookieDate.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /** 
     * Copy over cookies.
     */
    public int filter(NoodleData noodleData, ResponseBlock block)
    {
        HTTPResponse proxyResponse = noodleData.getProxyResponse();
        HttpServletResponse clientResponse = noodleData.getClientResponse();
        
        String[] cookieHeaders = {"Set-Cookie", "Set-Cookie2"};
        try
        {
            for (int i = 0; i < cookieHeaders.length; i++)
            {
                String setCookieResponse = 
                    proxyResponse.getHeader(cookieHeaders[i]);
                parseCookie(clientResponse, setCookieResponse);
            }
        }
        catch (Exception ignored)
        {
        }
        return KILL_THIS_FILTER;
    }

    /**
     * Parse and copy over a cookie.
     */
    private static void parseCookie (HttpServletResponse res, String line)
        throws Exception
    {
        StringTokenizer st = new StringTokenizer(line, ";,", true );
        String expires = "";
        boolean inexpires = false;
        Vector cookieData = new Vector();
        Vector cookieJar = new Vector();
        while( st.hasMoreElements() )
        {
            String token = (String) st.nextElement();
            token = token.trim();

            if (token.equals(";"))
            {
                continue;
            }

            // the end of the cookie
            if (token.equals(",") && !inexpires)
            {
                parseCookieData (cookieJar, cookieData);
                continue;
            }
            // regular tokens
            if (inexpires == false && ! token.equals (",") &&
                ! token.startsWith("expires"))
            {
                cookieData.addElement(token);
            }

            // handle expires
            if (inexpires == true && ! token.equals (","))
            {
                expires += ", " + token;
                cookieData.addElement(expires);
                expires = "";
                inexpires = false;
            }
            if (token.startsWith("expires"))
            {
                expires = token;
                inexpires = true;
            }
        }
        // need to call one more time
        parseCookieData (cookieJar, cookieData);
        for (Enumeration e = cookieJar.elements(); e.hasMoreElements(); )
        {
            res.addCookie((Cookie)e.nextElement());
        }
    }

    private static void parseCookieData(Vector cookieJar, Vector cookieData)
        throws Exception
    {
        Date expiresVal = null;
        String pathVal = null;
        String domainVal = null;
        String var = null;
        String varVal = null;
        for (Enumeration e = cookieData.elements();e.hasMoreElements() ; )
        {
            String tok = (String) e.nextElement();
            int equals_pos = tok.indexOf('=');
            if (equals_pos > 0)
            {
                String name = tok.substring(0, equals_pos);
                String value = tok.substring(equals_pos + 1);
                if (name.equals("expires"))
                {
                    expiresVal = cookieDate.parse(value);
                }
                else if (name.equals("path"))
                {
                    pathVal = value;                        
                }    
                else if (name.equals("domain"))
                {
                    domainVal = value;                        
                }
                else
                {
                    var = name;
                    varVal = value;                        
                }
            }
        }
        Cookie cookie = new Cookie(var, varVal);
        if (expiresVal != null)
            cookie.setMaxAge(new Long(expiresVal.getTime()).intValue());
        if (domainVal != null)
            cookie.setDomain(domainVal);
        if (pathVal != null)
            cookie.setPath(pathVal);
        cookieJar.addElement(cookie);
        cookieData.removeAllElements();
    }
}    
