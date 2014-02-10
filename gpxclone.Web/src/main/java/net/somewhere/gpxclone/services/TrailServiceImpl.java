/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.services;

import java.util.Collection;
import net.somewhere.gpxclone.dao.TrailDao;
import net.somewhere.gpxclone.dao.TrailTypeDao;
import net.somewhere.gpxclone.entities.Trail;
import net.somewhere.gpxclone.entities.TrailType;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Jonas
 */
@Transactional
public class TrailServiceImpl implements TrailService {
    
    private TrailDao trailDao;
    private TrailTypeDao trailTypeDao;

    public void setTrailDao(TrailDao trailDao) {
        this.trailDao = trailDao;
    }

    public void setTrailTypeDao(TrailTypeDao trailTypeDao) {
        this.trailTypeDao = trailTypeDao;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasTrack(long trackId) {
        return trailDao.trackExists(trackId);
    }

    @Override
    @Transactional(readOnly = true)
    public Trail getTrack(long trackId) {
        return trailDao.getTrack(trackId);
    }

    @Override
    public Trail createTrack(Trail track) {
        
        if(track.getType()==null)
        {
            TrailType unknownType = trailTypeDao.getTrackType("Unknown");
            if (unknownType == null) 
            {
                unknownType = trailTypeDao.createTrackType("Unknown");
            }
            track.setType(unknownType);
        }
        return trailDao.createTrack(track);
        
    }

    @Override
    public void updateTrack(Trail track) {
        trailDao.updateTrack(track);
    }

    @Override
    public void updateTrackType(TrailType type) {
        trailTypeDao.updateTrackType(type);
    }

    @Override
    public boolean deleteTrack(Trail track) {
        return trailDao.deleteTrack(track);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Trail> getTracks() {
        return trailDao.getTracks();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTrackOfType(long trackId, String typeName) {
        if (typeName == null)
        { return false;}
        Trail track = trailDao.getTrack(trackId);
        if (track == null) return false;
        return track.getType().getTypename().equals(typeName);
    }

    @Override
    public TrailType createTrackType(String typeName) {
        return trailTypeDao.createTrackType(typeName);
    }

    @Override
    public boolean deleteTrackType(TrailType type) {
        return trailTypeDao.deleteTrackType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public TrailType getTrackType(String typeName) {
        return trailTypeDao.getTrackType(typeName);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<TrailType> getTrackTypes() {
        return trailTypeDao.getTrackTypes();
    }
    
}
