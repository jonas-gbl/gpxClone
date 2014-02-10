/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Jonas
 */
public interface ProxyService {
    
    public void proxyRequest(HttpServletRequest request, HttpServletResponse response);
    public void setHost(String hostName);
    public void setPort(int port);
    public void setPath(String path);
}
