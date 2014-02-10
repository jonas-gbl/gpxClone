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

import java.io.IOException;

import HTTPClient.HTTPResponse;
import HTTPClient.ModuleException;

import org.tigris.noodle.NoodleResponseFilter;
import org.tigris.noodle.NoodleData;
import org.tigris.noodle.NoodleConstants;
import org.tigris.noodle.ResponseBlock;

/**
 * This is a proxy response filter to be run after CheckForRedirect. It
 * determines the content type of the proxy response, and propagates it
 * to the client response. If the content type is binary (non-"text/html"),
 * it disables further filtering.
 */
public class HandleContentType implements NoodleResponseFilter
{
    /**
     * Header containing content type.
     */
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";

    /**
     * Key for content length header.
     */
    private static final String CONTENT_LENGTH_HEADER_NAME = "Content-Length";

    /**
     * Propagate the content type to the client response. Turn
     * off further filtering unless we're delivering HTML data.
     */
    public int filter(NoodleData data, ResponseBlock block)
        throws Exception
    {
        String contentType = NoodleConstants.DEFAULT_CONTENT_TYPE;
        HTTPResponse proxyResponse = data.getProxyResponse();
        String type = proxyResponse.getHeader(CONTENT_TYPE_HEADER_NAME);
        if (type != null && type.length() > 0)
        {
            contentType = type;
        }
        data.getClientResponse().setContentType(contentType);
        
        int returnStatus = KILL_THIS_FILTER;
        if (!contentType.startsWith(NoodleConstants.CONTENT_TYPE_HTML))
        {
            // Propagate content-length if present so that IE can deal
            // with binaries.
            String len = proxyResponse.getHeader(CONTENT_LENGTH_HEADER_NAME);
            if (len != null && len.length() > 0)
            {
                data.getClientResponse().setHeader(CONTENT_LENGTH_HEADER_NAME,
                                                   len);
            }
            returnStatus = KILL_ALL_FILTERS;
        }
        return returnStatus;
    }
}
