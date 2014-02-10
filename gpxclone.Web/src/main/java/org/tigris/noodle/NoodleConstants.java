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

public interface NoodleConstants
{
    public static final String PROPERTIES = "properties";
    public static final String DEFAULT_PORT = "default.port";
    public static final String DEFAULT_HOST = "default.host";

    /** 
     * Default scheme, which you can't change right now. 
     */
    public static final String SCHEME = "http";

    /**
     * The content type for HTML data.
     */
    public static final String CONTENT_TYPE_HTML = "text/html";

    /** 
     * Used when no content type is set on the proxy return.
     */
    public static final String DEFAULT_CONTENT_TYPE = CONTENT_TYPE_HTML;

    public static final String PAGE_ATTRIBUTE = "org.tigris.noodle.page";
    public static final String PAGE_VAR = "page";
    public static final String POST = "POST";
    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String HEAD = "HEAD";

    /**
     * The header used for HTTP content type
     */
    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";

    /**
     * This is used when dealing with charset headers; it is the modifier
     * within the content-type header which specifies the charset.
     */
    public static final String CONTENT_TYPE_CHARSET = "charset=";

    /**
     * The default encoding to use when :
     * (1) we have read bytes into unicode characters per response info
     * (2) we are writing those unicode characters into bytes for output
     * (3) we were able to guess an encoding based on the response http
     * content type header or a meta HTML tag within the response
     *
     * Generally, we want this to be UTF-8 to avoid losing data.
     */
    public static final String DEFAULT_OUTPUT_CHARSET = "UTF-8";

    /**
     * The default encoding to use when :
     * (1) we are reading bytes into unicode characters
     * (2) we cannot determine the encoding for those bytes due to a lack
     * of information in the response
     *
     * When we use this, we won't use the DEFAULT_OUTPUT_CHARSET.
     */
    public static final String DEFAULT_CHARSET = "ISO-8859-1";
    
    public static enum Authentication {NONE,BASIC,DIGEST}
}    
