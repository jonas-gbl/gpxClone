/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.entities;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;


/**
 *
 * @author Jonas
 */
@Entity
@Table(name = "`Roles`", catalog = "`mTicketLocationDB`", schema = "`public`")
public class Role implements Serializable,GrantedAuthority {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "`Rolename`")
    private String rolename;
    @Column(name = "`Description`")
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Collection<User> users;

    public Role() {
    }

    public Role(String rolename) {
        this.rolename = rolename;
    }

    public String getRolename() {
        return this.rolename;
    }

    public void setRolename(String rolename) {
        this.rolename = rolename;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<User> getUsersCollection() {
        return this.users;
    }

    public void setUsersCollection(Collection<User> usersCollection) {
        this.users = usersCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.rolename != null ? this.rolename.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Role)) {
            return false;
        }
        Role other = (Role) object;
        if ((this.rolename == null && other.rolename != null) || (this.rolename != null && !this.rolename.equals(other.rolename))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.somewhere.gpxclone.entities.Roles[ rolename=" + this.rolename + " ]";
    }

    @Override
    public String getAuthority() {
        return this.rolename;
    }
    
}
