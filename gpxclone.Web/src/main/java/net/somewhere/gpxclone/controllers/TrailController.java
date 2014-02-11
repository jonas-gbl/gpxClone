/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.controllers;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.IOException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.util.Properties;
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
    
    
   
    @RequestMapping(value = "/trails/wms", method = RequestMethod.GET)
    public void wmsProxy(HttpServletRequest req, HttpServletResponse res)
    {      
        proxyService.proxyRequest(req, res);        
    }
    
}
