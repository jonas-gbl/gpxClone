/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.controllers;

import HTTPClient.HTTPConnection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamSource;
import net.somewhere.gpxclone.entities.User;
import net.somewhere.gpxclone.services.ProxyService;
import net.somewhere.gpxclone.services.TrailService;
import net.somewhere.gpxclone.utils.gpx11.Gpx;
import net.somewhere.gpxclone.utils.gpx11.Track;
import net.somewhere.gpxclone.utils.gpx11.TrackSegment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.tigris.noodle.NoodleConstants;
import org.tigris.noodle.NoodleData;
import org.tigris.noodle.ProxyModule;


/**
 *
 * @author Jonas
 */
@Controller
public class TrailController {
    
    private final TrailService trackService;
    private final ProxyService proxyService;
    private final GeometryFactory geoFactory;     
    private Unmarshaller trackUnMarshaller;
    
    private Properties proxyProperties;
    
    @Autowired
    public TrailController
    (TrailService trackService, GeometryFactory geoFactory, 
    ProxyService proxyService,Properties proxyProperties)
    {
        this.trackService = trackService;
        this.geoFactory = geoFactory;
        this.proxyService = proxyService;
        
        this.proxyProperties = proxyProperties;
        init_proxy();
    }
    
    @Autowired
    public void setTrackUnMarshaller(Unmarshaller trackUnMarshaller) {
        this.trackUnMarshaller = trackUnMarshaller;
    }
    
    
    @RequestMapping(value = "/trails/upload", method = RequestMethod.GET)
    public String initUploadForm(Model model) {
        return "trails/bt_uploadForm";
    }
    
    @RequestMapping(value = "/trails/upload", method = RequestMethod.POST)
    public String processUploadForm(Model model,Principal principal,
        @RequestParam("track") MultipartFile uploadedGPX)
    {
        JAXBElement trace_jaxb;
        Gpx gpx_log;
        
        User currentUser = (User) ((Authentication) principal).getPrincipal();
        
        if (!uploadedGPX.isEmpty())
        { 
            try
            {
                trace_jaxb = (JAXBElement) trackUnMarshaller.unmarshal(new StreamSource(uploadedGPX.getInputStream()));
                gpx_log= (Gpx)trace_jaxb.getValue();
                net.somewhere.gpxclone.entities.Trail trackEntity;
                
                for(Track track : gpx_log.getTracks())
                {
                    for(TrackSegment segment : track.getTrackSegments())
                    {
                        trackEntity= new net.somewhere.gpxclone.entities.Trail();
                        trackEntity.setOwner(currentUser);
                        Coordinate[] track_coords= CoordinateArrays.toCoordinateArray(segment.getTrackPointCoordinates());
                        trackEntity.setTrace(geoFactory.createLineString(track_coords));
                        trackService.createTrack(trackEntity);
                    }
                }                
            }
            catch(IOException e)
            {
                model.addAttribute("problem","Upload Failed!");
                return "errors/bt_oops";
            }
            catch(XmlMappingException e)
            {
                model.addAttribute("problem","Marshalling Failed!");
                return "errors/bt_oops";
            }
            catch(Exception e)
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                
                model.addAttribute("problem",sw.toString());
                return "errors/bt_oops";
            }
            // store the bytes somewhere
           return "redirect:/";
       } 
        else
        {
            model.addAttribute("problem","Upload Failed!");
            return "errors/bt_oops";
       }
    }
    
    
    //@RequestMapping(value = "/trails/wms", method = RequestMethod.GET)
    public void wmsProxy(HttpServletRequest req, HttpServletResponse res/*, Principal principal*/)
        throws IOException, ServletException
    {
        String tmp;
        /*
        String anonymousCQL = "public = true";
        String userCQL_preamble = "public = true OR owner = ";
        User currentUser=null;
        String activeCQL;        
       
        Principal principal = req.getUserPrincipal();
        
        if (principal!=null)
        {
            currentUser=(User) ((Authentication) principal).getPrincipal();
            activeCQL= userCQL_preamble + currentUser.getPId();
        }
        else
        {
            activeCQL=anonymousCQL;
        }       
        */
        tmp= req.getQueryString();
        req.setAttribute(NoodleConstants.PAGE_ATTRIBUTE, proxyProperties.getProperty("default.page"));
              
        
        OutputStream output = null;
        try
        {
            // Create a NoodleData object for this request.
            NoodleData noodleData = new NoodleData(req, res, proxyProperties);

            // deal with POST Data. Since this code is meant to be modular
            // to other systems, then we need to be able to assign 
            // the byte[] of post data here instead of within the ProxyModule
            // because other systems may have already read the byte[] of data
            // from the stream
            if (req.getMethod().equalsIgnoreCase(NoodleConstants.POST))
            {
                noodleData.setPostData
                    (ProxyModule.readFully
                     (new BufferedInputStream(req.getInputStream())));

                /*
                  This code will also work if you do not have access to 
                  the data in the InputStream
                  noodleData.setPostData(postToByteArray(req));
                */
            }

            //Proxy the request, running the request and response servlets
            //defined in proxyProperties.
            noodleData.proxyRequest();
        }
        catch (Exception e)
        {
            // get the outputstream
            if (output == null)
            {
                output = res.getOutputStream();
            }
            output.write ("<pre>".getBytes());
            output.write (stackTrace(e).getBytes());
            output.write ("</pre>".getBytes());
        }
        finally
        {
            if (output != null)
            {
                output.close();
            }
        }
    }
    
    //@RequestMapping(value = "/trails/wms2", method = RequestMethod.GET)
    @RequestMapping(value = "/trails/wms", method = RequestMethod.GET)
    public void wmsProxy2(HttpServletRequest req, HttpServletResponse res)
    {      
        proxyService.proxyRequest(req, res);        
    }
    
    private static String stackTrace(Throwable e)
    {
        String foo = null;
        try
        {
            // and show the Error Screen
            ByteArrayOutputStream ostr = new ByteArrayOutputStream();
            e.printStackTrace( new PrintWriter(ostr,true) );
            foo = ostr.toString();
        }
        catch (Exception f)
        {
            // do nothing
        }
        return foo;
    }
    
    private void init_proxy() //throws ServletException
    {
        //Remove the default HTTPClient.CookieModule.class
        //so that it can be overridden by our own implementation
        HTTPConnection.removeDefaultModule(HTTPClient.CookieModule.class);
        
        try
        {
            HTTPConnection.addDefaultModule
                    (Class.forName("org.tigris.noodle.NoodleCookieModule"), 1);
        }
        catch (ClassNotFoundException cnfe){}
        

        // setup some defaults
        int port = 80;
        String configuredPort =
            proxyProperties.getProperty(NoodleConstants.DEFAULT_PORT);
        try
        {
            if (configuredPort != null && configuredPort.length() > 0)
            {
                port = Integer.parseInt(configuredPort);
            }
        }
        catch (NumberFormatException e)
        {
            System.err.println(NoodleConstants.DEFAULT_PORT + " '" +
                               configuredPort +
                               "' not parsable as an integer: Defaulting " +
                               "to " + port);
        }
        ProxyModule defaultProxyModule = ProxyModule.getInstance();
        defaultProxyModule.setServerPort(port);

        String serverName = proxyProperties.getProperty
            (NoodleConstants.DEFAULT_HOST, "localhost");
        defaultProxyModule.setServerName(serverName);
        
        String username=proxyProperties.getProperty("default.username");
        String password=proxyProperties.getProperty("default.password");
        String realm=proxyProperties.getProperty("default.realm");
        String authType=proxyProperties.getProperty("default.authenticationType");
        if(username!=null && password!=null && realm!=null && authType!=null)
        {
            if(authType.equals("BASIC"))
            {defaultProxyModule.setCredentials(serverName, serverName, serverName, NoodleConstants.Authentication.NONE);}
            else if(authType.equals("DIGEST"))
            {defaultProxyModule.setCredentials(serverName, serverName, serverName, NoodleConstants.Authentication.NONE);}
        }
        defaultProxyModule.setCredentials(serverName, serverName, serverName, NoodleConstants.Authentication.NONE);
    }

    
}
