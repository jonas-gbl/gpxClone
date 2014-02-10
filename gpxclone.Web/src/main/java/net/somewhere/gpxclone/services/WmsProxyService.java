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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.springframework.security.core.Authentication;

/**
 *
 * @author Jonas
 */
public class WmsProxyService implements ProxyService {
    
    private String default_host="localhost";
    private int default_port=8080;
    private String default_path="/";
    private String default_layer="";
    private String default_username="";
    private String default_password="";
    
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
    
    @Override
    public void proxyRequest(HttpServletRequest request, HttpServletResponse response)
    {
        String anonymousCQL = "public = true";
        String userCQL_preamble = "public = true OR owner = ";
        Principal principal = request.getUserPrincipal();
        User currentUser;
        String activeCQL;
        
        try
        {               
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
                queryMap.put("cql_filter", activeCQL);
            }
            
            
            

            HttpUriRequest proxyRequest;
            
            DefaultHttpClient httpclient = new DefaultHttpClient();
            
            if(!username.isEmpty() && !password.isEmpty())
            {
                httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope(host, port),
                    new UsernamePasswordCredentials(username, password));
            }
            
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http")
                    .setHost(host)
                    .setPort(port)
                    .setPath(path)
                    .setQuery(queryMap.toString());
            
            if(request.getQueryString().toLowerCase().contains("request=getmap"))
            {
                if (principal!=null)
                {
                    currentUser=(User) ((Authentication) principal).getPrincipal();
                    activeCQL= userCQL_preamble + currentUser.getPId();
                    builder.addParameter("cql_filter", activeCQL);
                }
                else
                {
                    activeCQL=anonymousCQL;
                    builder.addParameter("cql_filter", activeCQL);
                }
            }
            
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
                    httpclient.getCookieStore().addCookie(proxyRequestCookie);
                }
            }
            
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
            //
            //  Executing the Proxy Request
            //
            HttpResponse proxyResponse = httpclient.execute(proxyRequest);
            
            
            //
            //  PostProcessing: Creating the Response to the client
            //
            HttpEntity proxyEntity = proxyResponse.getEntity();
            if(proxyEntity!=null)
            {
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
            List<Cookie> proxyResponseCookieList = httpclient.getCookieStore().getCookies();
            //Iterator<Cookie> cookieIterator = proxyResponseCookieList.iterator();
            //Cookie proxyResponseCookie;
            //while(cookieIterator.hasNext())
            for (Cookie proxyResponseCookie: proxyResponseCookieList)
            {
                //proxyResponseCookie = cookieIterator.next();
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
        catch (Exception ex)
        {
            Logger.getLogger(WmsProxyService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
