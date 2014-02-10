package net.somewhere.gpxclone.dao.hibernate;

import java.util.*;

import org.hibernate.Session;
import org.springframework.dao.DataIntegrityViolationException;
import net.somewhere.gpxclone.dao.UserDao;
import net.somewhere.gpxclone.entities.*;
import org.hibernate.Query;
import org.hibernate.SessionFactory;


public class HibernateUserDao implements UserDao {
    
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public boolean userExists(final String userName) {
        User someone;
        
        if (userName == null) return false;
        someone=(User)sessionFactory.getCurrentSession().bySimpleNaturalId(User.class ).load(userName);
        return someone != null;
    }

    public Collection<User> getUsers() {
        return sessionFactory.getCurrentSession().createCriteria(User.class).list();
    }

    public User getUser(long id) {
        return (User)sessionFactory.getCurrentSession().byId(User.class).load(id);
    }

    public User getUser(String userName) {
        User someone = null;
        if (userName != null) {
            someone=(User)sessionFactory.getCurrentSession().bySimpleNaturalId(User.class ).load(userName);
        }
        return someone;
    }

    public User createUser(User u){
        
        String userName = u.getUsername();
        if (userExists(userName)) throw new DataIntegrityViolationException(userName);

        u.setCreationDate(new Date());
        
        UserPreferences prefs = u.getUserPreferences();
        if (prefs == null) {
            prefs = new UserPreferences();
            prefs.setProfilePolicy("private");
            prefs.setGender("secret");

            u.setUserPreferences(prefs);
            prefs.setUser(u);
        }
        sessionFactory.getCurrentSession().save(u);
        return u;
    }

    public void updateUser(User u) {
        if (u != null) sessionFactory.getCurrentSession().update(u);
    }

    public void updateUserPreferences(UserPreferences prefs) {
        if (prefs != null) sessionFactory.getCurrentSession().update(prefs);
    }

    public boolean deleteUser(User u) {
        if (u == null) return false;
        boolean wasDeleted = false;
        
        try
        {
            sessionFactory.getCurrentSession().delete(u);
            wasDeleted = true;
        } 
        catch (Exception zzz) {
            zzz.printStackTrace();
        }
        return wasDeleted;
    }

    public String activateUser(final String activationKey) {
        
        Session currentSession=sessionFactory.getCurrentSession();

        String hql="from User u where u.activationKey = :activationKey and u.approved = false" ;
        Query HQLquery=currentSession.createQuery(hql);
        HQLquery=HQLquery.setParameter("activationKey", activationKey);
        List userList = HQLquery.list();

        Iterator userIterator = userList.iterator();
        User user2activate=(userIterator.hasNext()) ? (User) userIterator.next() : null;

        if (user2activate!=null)
        {
            user2activate.setApproved(true);
            user2activate.setLockedOut(false);
            user2activate.setActivationKey(null);
            currentSession.update(user2activate);
            return user2activate.getUsername();
        }
        else { return null; }
    }
}