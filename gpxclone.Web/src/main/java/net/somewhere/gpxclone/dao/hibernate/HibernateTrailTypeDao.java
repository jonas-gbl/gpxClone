/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.dao.hibernate;

import java.util.Collection;
import net.somewhere.gpxclone.dao.TrailTypeDao;
import net.somewhere.gpxclone.entities.TrailType;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataIntegrityViolationException;

/**
 *
 * @author Jonas
 */
public class HibernateTrailTypeDao implements TrailTypeDao{
    
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public boolean trackTypeExists(String typeName)
    {
        if (typeName == null) return false;
        
        TrailType someType = (TrailType)sessionFactory.getCurrentSession().bySimpleNaturalId(TrailType.class).load(typeName);
        return someType!=null; 
    }

    @Override
    public TrailType getTrackType(String typeName) {
       return (TrailType)sessionFactory.getCurrentSession().bySimpleNaturalId(TrailType.class).load(typeName);
    }

    @Override
    public TrailType createTrackType(String typeName) {
        if (trackTypeExists(typeName)) throw new DataIntegrityViolationException(typeName);
        
        TrailType tr_type = new TrailType();
        tr_type.setTypename(typeName);
        sessionFactory.getCurrentSession().save(tr_type);
        return tr_type;
    }

    @Override
    public void updateTrackType(TrailType type) {
        if (type != null) sessionFactory.getCurrentSession().update(type);
    }

    @Override
    public boolean deleteTrackType(TrailType type) {
        if (type == null) return false;
        boolean wasDeleted = false;
        try
        {
            sessionFactory.getCurrentSession().delete(type);
            wasDeleted = true;
        }
        catch (Exception zzz)
        {
            zzz.printStackTrace();
        }
        return wasDeleted;
    }

    @Override
    public Collection<TrailType> getTrackTypes() {
        return sessionFactory.getCurrentSession().createCriteria(TrailType.class).list();
    }
    
}
