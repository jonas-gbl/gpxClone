/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.dao;

import java.util.Collection;
import net.somewhere.gpxclone.entities.Trail;

public interface TrailDao {
    
    boolean trackExists(long trackId);
    Trail getTrack(long trackId);
    Trail createTrack(Trail newTrack);
    void updateTrack(Trail track);
    boolean deleteTrack(Trail track);
    
    Collection<Trail> getTracks();
    Collection<Trail> getTracksByUserName(String userName);
        
}
