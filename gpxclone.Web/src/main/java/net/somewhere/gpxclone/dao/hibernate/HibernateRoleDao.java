package net.somewhere.gpxclone.dao.hibernate;

import java.util.Collection;

import org.springframework.dao.DataIntegrityViolationException;

import net.somewhere.gpxclone.dao.RoleDao;
import net.somewhere.gpxclone.entities.Role;
import org.hibernate.SessionFactory;

public class HibernateRoleDao implements RoleDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    public boolean roleExists(final String roleName)
    {       
        if (roleName == null) return false;
        
        Role someRole = (Role)sessionFactory.getCurrentSession().byId(Role.class).load(roleName);
        return someRole!=null;    
    }

    public Collection<Role> getRoles()
    {
        return sessionFactory.getCurrentSession().createCriteria(Role.class).list();
    }

    public Role getRole(String roleName)
    {
        return (Role)sessionFactory.getCurrentSession().byId(Role.class).load(roleName);
    }

    public Role createRole(String roleName, String description)
    {
        if (roleExists(roleName)) throw new DataIntegrityViolationException(roleName);
        Role r = new Role();
        r.setRolename(roleName);
        r.setDescription(description);
        sessionFactory.getCurrentSession().save(r);
        return r;
    }

    public void updateRole(Role r)
    {
        if (r != null)
        {sessionFactory.getCurrentSession().update(r);}
    }

    public boolean deleteRole(Role r)
    {
        if (r == null) return false;
        boolean wasDeleted = false;
        try
        {
            sessionFactory.getCurrentSession().delete(r);
            wasDeleted = true;
        }
        catch (Exception zzz)
        {
            zzz.printStackTrace();
        }
        return wasDeleted;
    }
}