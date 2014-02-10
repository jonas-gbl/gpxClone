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

import java.util.*;

/**
 * This class is responsible for loading a set of NoodleFilters from a
 * Properties object and making them available in the correct order.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:leonardr@collab.net">Leonard Richardson</a> 
 */
public final class FilterSet
{    
    /**
     * Prefix to load errors.
     */
    private static final String COULDNT_INITIALIZE_PREFIX = 
        "Couldn't initialize filter class: ";

    /**
     * Prefix designating a proxy request filter.
     */
    private static final String PROXY_REQUEST_FILTER_PREFIX = "filter.request";

    /**
     * Prefix designating a proxy response filter.
     */
    private static final String PROXY_RESPONSE_FILTER_PREFIX =
        "filter.response";

    /**
     * Prefix designating a proxy response finalizer filter (to be run
     * on the last block of data). 
     */
    private static final String PROXY_RESPONSE_FINALIZER_FILTER_PREFIX =
        "filter.response.finalizer";

    /** 
     * Instances of this class, keyed by the properties used to initialize
     * them. 
     */
    private static Map instances = new Hashtable();

    /**
     * A map of NoodleFilter objects to their ordinals.
     */
    private Map filterOrdinals;

    /** 
     * A sorted map of ordinals mapped to the NoodleFilter objects
     * corresponding to proxy request filters.
     */
    private SortedMap proxyRequestFilters;

    /** 
     * A sorted map of ordinals mapped to the NoodleFilter objects
     * corresponding to proxy response filters.
     */
    private SortedMap proxyResponseFilters;

    /** 
     * A sorted map of ordinals mapped to the NoodleFilter objects
     * corresponding to proxy response finalizer filters.
     */
    private SortedMap proxyResponseFinalizerFilters;
    
    /**
     * This constructor is private to force clients to use getInstance()
     * to access this class.
     */
    private FilterSet(Properties properties)
    {
        filterOrdinals = new Hashtable();
        proxyRequestFilters = new TreeMap();
        proxyResponseFilters = new TreeMap();
        proxyResponseFinalizerFilters = new TreeMap();
        fillFilterCache(properties);
    }
        
    /**
     * The method through which instances of this class are accessed.
     *
     * @return The single instance of this class.
     */
    public static final FilterSet getInstance(Properties properties)
    {
        FilterSet instance = (FilterSet)instances.get(properties);
        if (instance == null)
        {
            instance = new FilterSet(properties);
            instances.put(properties, instance);
        }
        return instance;
    }

    public Integer getOrdinal(NoodleFilter filter)
    {
        Integer ordinal = (Integer)filterOrdinals.get(filter);
        return ordinal;
    }

    /**
     * Returns the request filters. 
     */
    public SortedMap getProxyRequestFilters()
    {
        return proxyRequestFilters;
    }

    /**
     * Returns the response filters. 
     */
    public SortedMap getProxyResponseFilters()
    {
        return proxyResponseFilters;
    }

    /**
     * Returns the response finalizer filters. 
     */
    public SortedMap getProxyResponseFinalizerFilters()
    {
        return proxyResponseFinalizerFilters;
    }

    /**
     * Not the most efficient, but it works and is only executed during
     * initialization.
     */
    private void fillFilterCache(Properties properties)
    {
        Hashtable rawData = new Hashtable();
        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();)
        {
            Map relevantMap = null;
            String filterName = (String) e.nextElement();
            if (filterName.startsWith(PROXY_REQUEST_FILTER_PREFIX))
            {
                relevantMap = proxyRequestFilters;                
            }
            else if (filterName.startsWith
                     (PROXY_RESPONSE_FINALIZER_FILTER_PREFIX))
            {
                relevantMap = proxyResponseFinalizerFilters;
            }
            else if (filterName.startsWith(PROXY_RESPONSE_FILTER_PREFIX))
            {
                relevantMap = proxyResponseFilters;
            }
            if (relevantMap != null)
            {
                Integer i = getNumber(filterName);
                String className = properties.getProperty(filterName);
                Class filterClass = null;
                try 
                {
                    filterClass = Class.forName(className);
                }
                catch (ClassNotFoundException ex)
                {
                    System.err.println(COULDNT_INITIALIZE_PREFIX 
                                       + className + " doesn't exist!");
                }

                NoodleFilter filter = null;
                if (filterClass != null)
                {
                    try 
                    {
                        filter = (NoodleFilter)filterClass.newInstance();
                    }
                    catch (Exception ex)
                    {
                        System.err.println
                            (COULDNT_INITIALIZE_PREFIX
                             + className + " couldn't be instantiated: " 
                             + ex.getMessage());
                    }
                }

                if (filter != null)
                {
                    boolean ok = true;
                    if (relevantMap == proxyRequestFilters)
                    {
                        if (!(filter instanceof NoodleRequestFilter))
                        {

                            ok = false;
                            System.err.println
                                (COULDNT_INITIALIZE_PREFIX
                                 + className 
                                 + " doesn't implement NoodleRequestFilter!");
                        }
                    }
                    else if (!(filter instanceof NoodleResponseFilter))
                    {                  
                        ok = false;
                        System.err.println
                            (COULDNT_INITIALIZE_PREFIX + className 
                             + " doesn't implement NoodleResponseFilter!");
                    }

                    if (ok)
                    {
                        relevantMap.put(i, filter);
                        filterOrdinals.put(filter, i);
                    }
                }
            }
        }
    }
    
    /**
     * INPUT: filter.request.1
     * <p>
     * OUTPUT: 1
     */
    private static final Integer getNumber(String prop)
    {
        String name = (prop.substring(prop.lastIndexOf(".")+1, prop.length()));
        return new Integer(Integer.parseInt(name));
    }
}    
