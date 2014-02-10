/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.dao.hibernate;


import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.Collection;
import net.somewhere.gpxclone.dao.TrailDao;
import net.somewhere.gpxclone.entities.Trail;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 *
 * @author Jonas
 */
public class HibernateTrailDao implements TrailDao{
    
    private SessionFactory sessionFactory;
    private GeometryFactory geofactory;
    
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public boolean trackExists(long trackId) {
        Trail something;
        
        something=(Trail)sessionFactory.getCurrentSession().byId(Trail.class).load(trackId);
        return something!=null;
    }

    @Override
    public Trail getTrack(long trackId) {
        return (Trail)sessionFactory.getCurrentSession().byId(Trail.class).load(trackId);
    }

    @Override
    public Trail createTrack(Trail newTrack) {
        
        //newTrack.setCreationDate(new Date());
        sessionFactory.getCurrentSession().save(newTrack);
        return newTrack;
    }

    @Override
    public void updateTrack(Trail track) {
        if (track != null) sessionFactory.getCurrentSession().update(track);
    }

    @Override
    public boolean deleteTrack(Trail track) {
        if (track == null) return false;
        boolean wasDeleted = false;
        
        try
        {
            sessionFactory.getCurrentSession().delete(track);
            wasDeleted = true;
        } 
        catch (Exception zzz) {
            //zzz.printStackTrace();
        }
        return wasDeleted;
    }

    @Override
    public Collection<Trail> getTracks() {
        return sessionFactory.getCurrentSession().createCriteria(Trail.class).list();
    }

    @Override
    public Collection<Trail> getTracksByUserName(String userName) {
        
        Session currentSession=sessionFactory.getCurrentSession();

        String hql="from Track tr where tr.user.username = :userName";
        Query HQLquery=currentSession.createQuery(hql);
        HQLquery=HQLquery.setParameter("userName", userName);
        return HQLquery.list();
    }
    
}
