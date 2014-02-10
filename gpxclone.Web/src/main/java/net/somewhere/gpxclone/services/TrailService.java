package net.somewhere.gpxclone.services;

import java.util.Collection;
import net.somewhere.gpxclone.entities.*;


public interface TrailService {
    boolean hasTrack(long TrackId);
    Trail getTrack(long TrackId);
    Trail createTrack(Trail track);
    void updateTrack(Trail track);
    void updateTrackType(TrailType type);
    boolean deleteTrack(Trail track);
    Collection<Trail> getTracks();
    boolean isTrackOfType(long trackId, String roleName);
    TrailType createTrackType(String typeName);
    boolean deleteTrackType(TrailType type);
    TrailType getTrackType(String typeName);
    Collection<TrailType> getTrackTypes();
}