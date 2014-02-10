/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tigris.noodle.filters;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import net.somewhere.gpxclone.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.util.UriUtils;
import org.tigris.noodle.NoodleData;
import org.tigris.noodle.NoodleRequestFilter;

/**
 *
 * @author Jonas
 */
public class AddEcqlQuery implements NoodleRequestFilter{

    @Override
    public void filter(NoodleData noodleData)
    {
        HttpServletRequest incomingRequest=noodleData.getClientRequest();
        String queryData = noodleData.getQueryData();
                
        String anonymousCQL = "public = true";
        String userCQL_preamble = "public = true OR owner = ";
        User currentUser=null;
        String activeCQL, cql_queryParam, proxy_query;
        
        Principal principal = incomingRequest.getUserPrincipal();
        
        if (principal!=null)
        {
            currentUser=(User) ((Authentication) principal).getPrincipal();
            activeCQL= userCQL_preamble + currentUser.getPId();
        }
        else
        {
            activeCQL=anonymousCQL;
        }
        
        try
        {
            noodleData.getURL();
            cql_queryParam=UriUtils.encodeQueryParam(activeCQL,"UTF-8");
            proxy_query=queryData+"&cql_filter="+cql_queryParam;
            noodleData.setQueryData(proxy_query);
        }
        catch (UnsupportedEncodingException ex) {}
        
        
    }
    
}
