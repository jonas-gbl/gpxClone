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

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import HTTPClient.HTTPResponse;
import HTTPClient.ModuleException;

import org.tigris.noodle.NoodleResponseFilter;
import org.tigris.noodle.NoodleData;
import org.tigris.noodle.ResponseBlock;

/**
 * This is the first proxy response filter which should be run.
 * It checks for and propagates redirects sent out by the proxy.
 */
public class CheckForRedirect implements NoodleResponseFilter
{
    private static final boolean DEBUG = true;

    private static final String LOCATION_HEADER = "Location";
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";

    /**
     * Handle unusual status codes.
     */
    public int filter(NoodleData data, ResponseBlock block)
        throws Exception
    {
        int status = KILL_THIS_FILTER;
        HTTPResponse proxyResponse = data.getProxyResponse();
        HttpServletResponse clientResponse = data.getClientResponse();
        boolean booleanRedirectResult = false;
        int statusCode = proxyResponse.getStatusCode();
        if(statusCode >= HttpServletResponse.SC_MULTIPLE_CHOICES /* 300 */
           && statusCode < HttpServletResponse.SC_NOT_MODIFIED /* 304 */)
        {
            booleanRedirectResult = true;
            String strStatusCode = Integer.toString(statusCode);
            String location = proxyResponse.getHeader(LOCATION_HEADER);
            if(location == null)
            {
                throw new Exception("No location. StatusCode: "
                                    + strStatusCode);
            }
            if(DEBUG) 
            {
                System.out.println("Redirect to: " + location + 
                                   ". StatusCode = " + strStatusCode);
            }
            clientResponse.sendRedirect(location);
            status = KILL_ALL_FILTERS;
        }
        else if(statusCode == HttpServletResponse.SC_NOT_MODIFIED)
        {
            // 304 needs special handling.  See:
            // http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
            // We get a 304 whenever passed an 'If-Modified-Since'
            // header and the data on disk has not changed; server
            // responds w/ a 304 saying I'm not going to send the
            // body because the file has not changed.
            //
            booleanRedirectResult = true;
            HttpServletResponse response = data.getClientResponse();
            response.setIntHeader(CONTENT_LENGTH_HEADER, 0);
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            status = KILL_ALL_FILTERS;
        }
        return status;
    }
}
