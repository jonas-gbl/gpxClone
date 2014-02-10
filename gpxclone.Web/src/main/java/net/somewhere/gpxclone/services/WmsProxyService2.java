/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.somewhere.gpxclone.entities.User;
import net.somewhere.gpxclone.utils.HttpQueryMapDecorator;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.security.core.Authentication;

/**
 *
 * @author Jonas
 */
public class WmsProxyService2 implements ProxyService {
    
    private String default_host="localhost";
    private int default_port=8080;
    private String default_path="/";
    private String default_layer="";
    private String default_username="";
    private String default_password="";
    
    private final String anonymousCQL = "public = true";
    private final String userCQL_preamble = "public = true OR owner = ";
    
    private boolean initialized=false;
    
    DefaultHttpClient httpClient;
    
    @Override
    public void setHost(String hostname)
    {
        this.default_host=hostname;
    }

    @Override
    public void setPort(int port) 
    {
        this.default_port=port;
    }

    @Override
    public void setPath(String path)
    {
        this.default_path=path;
    }
    
    public void setLayer(String layer)
    {
        this.default_layer=layer;
    }
    
    public void setUsername(String username)
    {
        this.default_username=username;
    }
    
    public void setPassword(String password)
    {
        this.default_password=password;
    }
    
    public void init()
    {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
            new Scheme("http", default_port, PlainSocketFactory.getSocketFactory()));

        ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        
        httpClient = new DefaultHttpClient(cm);
        if(!default_username.isEmpty() && !default_password.isEmpty())
        {
            httpClient.getCredentialsProvider().setCredentials(
                new AuthScope(default_host, default_port),
            new UsernamePasswordCredentials(default_username, default_password));
        }
        
        this.initialized=true;
    }
    
    @Override
    public void proxyRequest(HttpServletRequest request, HttpServletResponse response)
    {
        HttpContext httpContext;
        Principal principal = request.getUserPrincipal();
        User currentUser;
        String activeCQL;
                
        try
        {               
            if(!this.initialized)
            {
                throw new IllegalStateException("WmsProxyService not initialized!");
            }
            
            httpContext = new BasicHttpContext();
            
            HttpQueryMapDecorator queryMap = new HttpQueryMapDecorator(new HashMap<String,String>());
            queryMap.fill(request.getQueryString());
                    
            if(!queryMap.containsKey("service"))
            {
                throw new ClientProtocolException("Not a ows request");
            }
            else if(!queryMap.get("service").equalsIgnoreCase("wms"))
            {
                throw new ClientProtocolException("Not a WMS request");
            }
            
            String host=default_host;
            if(request.getAttribute("net.somewhere.proxy.host")!=null)
            {
                host = (String)request.getAttribute("net.somewhere.proxy.host");
            }
            
            Integer port = default_port;
            if(request.getAttribute("net.somewhere.proxy.port")!=null)
            {
                port = (Integer) request.getAttribute("net.somewhere.proxy.port");
            }
            
            String path = default_path;
            if(request.getAttribute("net.somewhere.proxy.path")!=null)
            {
                path = (String)request.getAttribute("net.somewhere.proxy.path");
            }

            String username = default_username;
            if(request.getAttribute("net.somewhere.proxy.username")!=null)
            {
                username = (String)request.getAttribute("net.somewhere.proxy.username");
            }
            
            String password = default_password;
            if(request.getAttribute("net.somewhere.proxy.password")!=null)
            {
                password = (String)request.getAttribute("net.somewhere.proxy.password");
            }
                        
            if(!default_layer.isEmpty())
            {
                queryMap.put("layers",default_layer);
                if(queryMap.get("request").equalsIgnoreCase("getfeatureinfo"))
                {
                    queryMap.put("query_layers",default_layer);
                }
            }
            
            if(queryMap.get("request").equalsIgnoreCase("getmap")
                    || queryMap.get("request").equalsIgnoreCase("getfeatureinfo"))
            {
                if (principal!=null)
                {
                    currentUser=(User) ((Authentication) principal).getPrincipal();
                    activeCQL= userCQL_preamble + currentUser.getPId();     
                }
                else
                {
                    activeCQL=anonymousCQL;
                }
                
                if(queryMap.containsKey("cql_filter"))
                {
                    activeCQL = "(" + queryMap.get("cql_filter") + ") AND (" + activeCQL + ")";
                }
                queryMap.put("cql_filter", activeCQL);
            }
            
            HttpUriRequest proxyRequest;
            
            if(!username.isEmpty() && !password.isEmpty())
            {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                    new AuthScope(host, port),
                    new UsernamePasswordCredentials(username, password));
                httpContext.setAttribute(ClientContext.CREDS_PROVIDER, credentialsProvider);
            }
            
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http")
                    .setHost(host)
                    .setPort(port)
                    .setPath(path)
                    .setQuery(queryMap.toString());
        
            URI uri = builder.build();
            
            if(request.getMethod().equalsIgnoreCase("GET"))
            {
                proxyRequest= new HttpGet(uri);
            }
            else if(request.getMethod().equalsIgnoreCase("POST"))
            {
                proxyRequest= new HttpPost(uri);
                InputStream postEntityStream = request.getInputStream();
                ContentType postContentType = ContentType.create(request.getContentType());
                HttpEntity postEntity= new InputStreamEntity(postEntityStream,
                                                                (long)request.getContentLength(),
                                                                postContentType);                    
                ((HttpPost)proxyRequest).setEntity(postEntity);
            }
            else
            {
                throw new java.net.ProtocolException("UNSUPPORTED HTTP method: "+ request.getMethod());
            }
            
            javax.servlet.http.Cookie[] requestCookies = request.getCookies();
            if (requestCookies!=null)
            {
                CookieStore cookieStore = new BasicCookieStore();
                BasicClientCookie proxyRequestCookie;
                for(int i=0;i<requestCookies.length;i++)
                {
                    proxyRequestCookie=new BasicClientCookie(requestCookies[i].getName(),
                            requestCookies[i].getValue());
                    proxyRequestCookie.setComment(requestCookies[i].getComment());
                    proxyRequestCookie.setDomain(requestCookies[i].getDomain());
                    proxyRequestCookie.setPath(requestCookies[i].getPath());
                    proxyRequestCookie.setSecure(requestCookies[i].getSecure());
                    proxyRequestCookie.setVersion(requestCookies[i].getVersion());
                    cookieStore.addCookie(proxyRequestCookie);
                }
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            }
            
            
            if(!queryMap.get("request").equalsIgnoreCase("getfeatureinfo"))
            {
                Enumeration<String> requestHeaderNames = request.getHeaderNames();
                String currentHeaderName;
                while(requestHeaderNames.hasMoreElements())
                {
                    currentHeaderName=requestHeaderNames.nextElement();
                    Enumeration<String> headerValues=request.getHeaders(currentHeaderName);
                    while(headerValues.hasMoreElements())
                    {
                        proxyRequest.addHeader(currentHeaderName, headerValues.nextElement());
                    }

                }
            }
            //
            //  Executing the Proxy Request
            //
            HttpResponse proxyResponse = httpClient.execute(proxyRequest,httpContext);
            
            
            //
            //  PostProcessing: Creating the Response to the client
            //
            String TmpStatusLine;
            TmpStatusLine = proxyResponse.getStatusLine().toString();
            
            HttpEntity proxyEntity = proxyResponse.getEntity();
            if(proxyEntity!=null)
            {
                proxyEntity = new BufferedHttpEntity(proxyEntity);
                String tmpEntityString;
                tmpEntityString = EntityUtils.toString(proxyEntity,Charset.defaultCharset());
                OutputStream responseEntityStream=response.getOutputStream();
                try
                {
                    proxyEntity.writeTo(responseEntityStream);
                }
                finally
                {
                    responseEntityStream.close();
                }
            }
            response.setStatus(proxyResponse.getStatusLine().getStatusCode());
            response.setContentType(proxyResponse.getEntity().getContentType().getValue());
            response.setContentLength((int)proxyResponse.getEntity().getContentLength());
            //response.setCharacterEncoding(proxyResponse.getLocale()......);
            
            HeaderIterator it = proxyResponse.headerIterator();
            Header currentHeader;
            while (it.hasNext())
            {
                currentHeader=it.nextHeader();
                response.setHeader(currentHeader.getName(), currentHeader.getValue());
            }
            
            javax.servlet.http.Cookie responseCookie;
            List<Cookie> proxyResponseCookieList = ((CookieStore)httpContext.
                                                        getAttribute(ClientContext.COOKIE_STORE)).
                                                        getCookies();
            
            for (Cookie proxyResponseCookie: proxyResponseCookieList)
            {
                responseCookie = 
                        new javax.servlet.http.Cookie(proxyResponseCookie.getName(),
                                                        proxyResponseCookie.getValue());
                
                if(proxyResponseCookie.getExpiryDate()!=null)
                {
                    int maxAge=(int)(proxyResponseCookie.getExpiryDate().getTime() - (new Date()).getTime());
                    responseCookie.setMaxAge(maxAge);
                }
                
                if(proxyResponseCookie.getDomain()!=null)
                {
                    responseCookie.setDomain(proxyResponseCookie.getDomain());
                }
                
                if(proxyResponseCookie.getPath()!=null)
                {
                    responseCookie.setPath(proxyResponseCookie.getPath());
                }
                
                responseCookie.setSecure(proxyResponseCookie.isSecure());
                responseCookie.setVersion(proxyResponseCookie.getVersion());
                
                response.addCookie(responseCookie);
            }
        }
        catch (URISyntaxException ex)
        {
            Logger.getLogger(WmsProxyService.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (ClientProtocolException ex)
        {
            Logger.getLogger(WmsProxyService.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (ProtocolException ex) 
        {
            Logger.getLogger(WmsProxyService.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (IOException ex)
        {
            Logger.getLogger(WmsProxyService.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch (IllegalStateException ex)
        {
            Logger.getLogger(WmsProxyService.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }        
    }    
}
