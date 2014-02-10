package net.somewhere.gpxclone.dao;

import java.util.Collection;

import net.somewhere.gpxclone.entities.TrailType;

public interface TrailTypeDao {
    boolean trackTypeExists(String name);
    TrailType getTrackType(String name);
    
    TrailType createTrackType(String name);
    void updateTrackType(TrailType type);
    boolean deleteTrackType(TrailType type);
    Collection<TrailType> getTrackTypes();
}
