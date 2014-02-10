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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.*;
import java.net.*;

// Java Servlet Classes
import javax.servlet.*;
import javax.servlet.http.*;

import HTTPClient.*;

/**
 * NoodleData is a per-request container containing the stage of one
 * proxied request, and the logic to stream that request to the
 * client.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:leonardr@collab.net">Leonard Richardson</a> 
 */
public class NoodleData
{
    /**
     * the NoodleReader we're using
     */
    private NoodleReader reader;

    /**
     * The FilterSet containing the filters to be run on this
     * proxy request and response. 
     */ 
    private FilterSet filterSet;

    /** 
     * The servlet request 
     */
    private HttpServletRequest clientRequest = null;

    /** 
     * The servlet response 
     */
    private HttpServletResponse clientResponse = null;

    /** 
     * The proxy response
     */
    private HTTPResponse proxyResponse = null;

    /** 
     * The URL for the remote connection 
     */
    private URL url = null;

    /** 
     * Post data, if it exists 
     */
    private byte[] postData = null;

    /** 
     * Query data, if it exists 
     */
    private String queryData = null;

    /** 
     * The headers to send into the proxy request.
     */
    private List headersToSend = new ArrayList();

    /** 
     * A storage area for filter state.
     */
    private Map storage = new HashMap();

    /**
     * A container for the currently active filters.
     */
    private SortedMap currentlyActiveFilters = new TreeMap();

    /**
     * A container for the currently active finalizer filters.
     */    
    private SortedMap currentlyActiveFinalizerFilters = new TreeMap();

    /**
     * A SortedMap mapping ordinals to currently active proxy response
     * filters.  
     */
    private SortedMap activeFilters = new TreeMap();

    /**
     * A SortedMap mapping ordinals to proxy response finalizer
     * filters.  
     */
    private SortedMap finalizerFilters = new TreeMap();

    /**
     * The encoding of the data which we're preparing to write (if we
     * read in unicode bytes and are preparing to output, this is the
     * encoding for the output).  This will be null if the data is
     * being read as bytes.
     */
    protected String charset = null;

    /**
     * This is just cached locally to store the result of logic which
     * may be more complex.
     */
    protected Boolean useCharacterReader = null;

    /**
     * Constructor sets items that should be immediately available.
     */
    public NoodleData (HttpServletRequest req, HttpServletResponse res,
                       Properties servletProperties)
    {
        this.clientRequest = req;
        this.clientResponse = res;
        filterSet = FilterSet.getInstance(servletProperties);
        activeFilters.putAll(filterSet.getProxyResponseFilters());
        finalizerFilters.putAll(filterSet.getProxyResponseFinalizerFilters());
    }

    /**
     * Returns the client request.
     */
    public HttpServletRequest getClientRequest()
    {
        return this.clientRequest;
    }

    /**
     * Returns the client response.
     */
    public HttpServletResponse getClientResponse()
    {
        return this.clientResponse;
    }

    /**
     * Returns the HTTP response from the proxy. Will return null
     * if there is not yet a connection to the proxy.
     */
    public HTTPResponse getProxyResponse()
    {
        return this.proxyResponse;
    }

    /**
     * The HTTP response from the proxy. You should never need to call
     * this directly--it's called from ProxyModule.
     */
    void setProxyResponse(HTTPResponse proxyResponse)
    {
        this.proxyResponse = proxyResponse;
    }

    /**
     * Returns the URL used to connect to the proxy.
     */
    public URL getURL()
    {
        return this.url;
    }

    /**
     * Sets the URL used to connect to the proxy. Should only be
     * called by request filters.  
     */
    public void setURL(URL url)
    {
        this.url = url;
    }

    /**
     * Returns the List of name-value pairs corresponding to the
     * headers to send.
     */
    public List getHeadersToSend()
    {
        return this.headersToSend;
    }

    /**
     * Sets the Vector of name-value pairs corresponding to the
     * headers to send. Should only be called by request filters.  
     */
    public void setHeadersToSend(Vector headersToSend)
    {
        this.headersToSend = headersToSend;
    }

    /**
     * You shouldn't need to execute this method directly; it's called
     * by ProxyModule.
     */
    NVPair[] getHeadersToSendAsNVPair()
    {
        NVPair[] result = new NVPair[headersToSend.size()];
        int i = 0;
        for (Iterator j = headersToSend.iterator(); j.hasNext();)
        {
            NVPair tmp = (NVPair)j.next();
            result[i] = tmp;
            i++;
        }
        return result;
    }
    
    /**
     * Returns the headers to send as a string.
     */
    public String getHeadersToSendAsString()
    {
        StringBuffer strbuf = new StringBuffer();
        for(int i = 0; i < headersToSend.size(); i++)
        {
            NVPair pair = (NVPair)headersToSend.get(i);
            strbuf.append(pair.getName()).append(':')
                .append(pair.getValue()).append("===");
        }
        return strbuf.toString();
    }

    /**
     * Returns the POST data from the client request, if any.
     */
    public byte[] getPostData()
    {
        return this.postData;
    }

    /**
     * Sets the POST data from the client request. If there is no POST
     * data, this should be called with null.  Should only be called
     * by request filters.
     */
    public void setPostData(byte[] postData)
    {
        this.postData = postData;
    }

    /**
     * Returns the query data from the client request, if it exists.
     */
    public String getQueryData()
    {
        return this.queryData;
    }

    /**
     * Sets the query data from the client request. If there is no
     * query data, this should be called with null. Should only be
     * called by request filters.
     */
    public void setQueryData(String queryData)
    {
        this.queryData = queryData;
    }

    /**
     * Retrieves a value from the general storage area.
     */
    public Object getValue(String key)
    {
        return storage.get(key);
    }

    /**
     * Adds a value to the general storage area.
     */
    public void putValue(String key, Object value)
    {
        storage.put(key, value);
    }

    // Filter-specific methods.

    /**
     * Returns an iterator over the currently active proxy response
     * filters. We put them in another Map and return an iterator to
     * the other map, so that we can remove items from the original
     * map without getting a CME. This should probably be rewritten so
     * that it doesn't need to use duplicate storage.  
     */
    public Iterator getActiveFilters()
    {
        currentlyActiveFilters.clear();
        currentlyActiveFilters.putAll(activeFilters);
        return currentlyActiveFilters.values().iterator();
    }

    /**
     * Returns an iterator over the current finalizer filters. We put
     * them in another Map and return an iterator to the other map, so
     * that we can remove items from the original map without getting
     * a CME. This should probably be rewritten so that it doesn't
     * need to use duplicate storage.  
     */
    public Iterator getFinalizerFilters()
    {
        currentlyActiveFinalizerFilters.clear();
        currentlyActiveFinalizerFilters.putAll(finalizerFilters);
        return currentlyActiveFinalizerFilters.values().iterator();
    }

    /**
     * Kills an active filter.
     * @return Whether or not there are any active filters left.
     */
    public boolean killFilter(NoodleResponseFilter filter)
    {
        Integer ordinal = filterSet.getOrdinal(filter);
        activeFilters.remove(ordinal);
        return !activeFilters.isEmpty();
    }

    /**
     * Kills all active and finalizer filters.
     * @return Whether or not there are any active filters (there won't be).
     */
    public boolean killFilters()
    {
        activeFilters.clear();
        finalizerFilters.clear();
        return false;
    }

    /**
     * Sets the given proxy response filter loose on the current block
     * and does an integrity check on the results.  
     */
    protected int runProxyResponseFilter(NoodleResponseFilter filter,
                                         ResponseBlock block)
        throws Exception
    {
        // TODO : add more debugging -- before status
        int status = filter.filter(this, block);
        // TODO : add more debugging -- after status
        return status;
    }

    // Stream processing methods.
    //

    /**
     * The default charset to assume if a character stream is used, for
     * reading data from the proxied response.
     */
    public String getDefaultCharset()
    {
        return NoodleConstants.DEFAULT_CHARSET;
    }

    /**
     * The charset to assume if a character stream is used, for
     * sending data back to the original client.
     */
    public String getOutputCharset()
    {
        return NoodleConstants.DEFAULT_OUTPUT_CHARSET;
    }

    /**
     * Assuming this NoodleData instance has been set up properly,
     * this method will run a proxy request through the request
     * filters, get the response, and stream the response through the
     * defined response filters.
     */
    public void proxyRequest()
        throws Exception
    {
        proxyRequest("/");
    }

    /**
     * Get the noodle reader, which is responsible for getting blocks of
     * data from the response.  Since we break the response up into blocks,
     * it is important to respect character boundaries when we want to process
     * byte data as Strings; however, the default reader assumes the default
     * encoding, since that's also safe for binary data.
     */
    protected NoodleReader getReader()
        throws NoodleException
    {
        if (useCharacterReader())
        {
            CharacterNoodleReader reader = new CharacterNoodleReader(this);
            charset = reader.getCharset();
            return reader;
        }
        else
        {
            return new ByteNoodleReader(this);
        }
    }

    /**
     * This determines if we should try to interact with the incoming 
     * data as characters or as bytes.
     */
    public boolean useCharacterReader()
        throws NoodleException
    {
        if (useCharacterReader == null)
        {
            // if we don't have html, we don't do encoding transformations
            String type = null;
            try
            {
                type = getProxyResponse().
                    getHeader(NoodleConstants.CONTENT_TYPE_HEADER_NAME);
                if (type.startsWith(NoodleConstants.CONTENT_TYPE_HTML))
                {
                    useCharacterReader = Boolean.TRUE;
                }
                else
                {
                    useCharacterReader = Boolean.FALSE;
                }
            }
            catch (IOException io)
            {
                throw new NoodleException("unable to get content type header",
                                          io);
            }
            catch (ModuleException me)
            {
                throw new NoodleException("unable to get content type header",
                                          me);
            }
        }
        return useCharacterReader.booleanValue();
    }

    /**
     * Proxies the request using the default <code>ProxyModule</code>.
     *
     * @see #proxyRequest(ProxyModule, String)
     */
    public void proxyRequest(String savedQueryString)
        throws Exception
    {
        proxyRequest(ProxyModule.getInstance(), savedQueryString);
    }

    /**
     * Assuming this NoodleData instance has been set up properly,
     * this method will run a proxy request through the request
     * filters, get the response, and stream the response through the
     * defined response filters.
     *
     * @param proxyModule Our HTTP proxy.
     * @param savedQueryString A previously saved-off query string
     * (e.g. "/docs-2.0/mod/mod_rewrite.html").
     */
    public void proxyRequest(ProxyModule proxyModule, String savedQueryString)
        throws Exception
    {
        // this provides a way to handle things if the client goes away
        boolean abort = false;
        
        String query = proxyModule.getQueryString(clientRequest, savedQueryString);
        try
        {            
            //They don't have a setup that puts the page into a request
            //attribute, so try to get it from a 'page' value in the query.
            String page = proxyModule.getValueFromQueryString(NoodleConstants.PAGE_VAR,
                                           query);
            clientRequest.setAttribute(NoodleConstants.PAGE_ATTRIBUTE, page);
            //Set the 'real' query data to be the sanitized version.
            query = proxyModule.stripQueryString(query, NoodleConstants.PAGE_VAR);
        }
        catch(NullPointerException e)
        {
            e.getClass();
        }
        finally
        {
            setQueryData(query);
        }

        //Run each proxy request filter, then set up the proxy.
        for (Iterator i = filterSet.getProxyRequestFilters()
                 .values().iterator();
             i.hasNext();)
        {
            NoodleRequestFilter filter = (NoodleRequestFilter)i.next();
            filter.filter(this);
        }
        proxyModule.proxyRequest(this, savedQueryString);

        clientResponse.setStatus(getProxyResponse().getStatusCode());

        // get a Reader
        reader = getReader();

        //Read from the input stream in blocks and apply each active
        //proxy response filter to each block. A filter may decide to
        //deactivate itself or to deactivate all filters.  A filter
        //may also decide to stream part of the current block into the
        //output stream. If there is any unstreamed data after all the
        //filters have had a go at it, we will stream the remainder.

        boolean doFilter = true;
        ResponseBlock block = null;
        while (!reader.readComplete() && !abort)
        {
            block = reader.doRead();
            if (doFilter)
            {
                //Process each active filter and handle its
                //return value.
                for (Iterator i = getActiveFilters(); 
                     i.hasNext() && doFilter;)
                {
                    NoodleResponseFilter filter = 
                        (NoodleResponseFilter)i.next();
                    int status = runProxyResponseFilter(filter, block);
                    switch (status) 
                    {
                    case NoodleResponseFilter.KILL_ALL_FILTERS:
                        doFilter = killFilters();
                        break;
                    case NoodleResponseFilter.KILL_THIS_FILTER:
                        doFilter = killFilter(filter);
                        break;
                    }
                }
            }
            if (!reader.readComplete())
            {
                try
                {
                    streamBlock(block);
                }
                catch (IOException e)
                {
                    abort = true;
                }
            }
        }

        //Without even a single block, we can't run finalizers --
        //somethings is wrong in that case.
        if (block == null)
        {
            throw new NoodleException("no data read from the client");
        }
        
        // if we got an IOException sending data to the client, then 
        // we don't bother with the finalizers -- the client is already
        // gone, there's no point in wasting more cpu cycles
        if (!abort)
        {
            //We're done reading data. Run through the finalizer filters
            //and have them process the final block.
            for (Iterator i = getFinalizerFilters(); i.hasNext();)
            {
                NoodleResponseFilter filter = (NoodleResponseFilter)i.next();
                runProxyResponseFilter(filter, block);
            }
        
            try
            {
                //Finish streaming the block in case the filters didn't do it
                //all.
                streamBlock(block);
            }
            catch (IOException e)
            {
                abort = true;
            }
        }
    }

    /**
     * Streams the current block from the current pointer to the end of the
     * block.
     */
    public void streamBlock(ResponseBlock block)
        throws IOException, NoodleException
    {
        streamBlockTo(block, block.getEndOffset());
    }

    /**
     * Streams the current block from the current pointer to the given
     * offset, inclusively.
     */
    public void streamBlockTo(ResponseBlock block, int lastStreamedByte)
        throws IOException, NoodleException
    {
        OutputStream out = clientResponse.getOutputStream();
        int newOffset = lastStreamedByte;
        int size = lastStreamedByte - block.getOffset();

        if (size > 0)
        {
            out.write(block.getBlock(), block.getOffset(), size);
            block.setOffset(newOffset);
        }
    }

    /**
     * Override this method if you want a hook for ProxyRequest to
     * call which will allow you to change the URL Noodle is proxying
     * just before ProxyRequest makes the HTTP call.
     */
    public String postProcessPageName (String page) 
    {
        // by default, do no special transform, but subclasses can override
        return page;
    }
}
