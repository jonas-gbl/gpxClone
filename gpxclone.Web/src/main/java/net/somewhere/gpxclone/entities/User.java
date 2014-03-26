/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


/**
 *
 * @author Jonas
 */
@Entity
@Table(name = "`Users`", catalog = "`mTicketLocationDB`", schema = "`public`")
public class User implements Serializable, UserDetails {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name="user_pk_sequence",sequenceName="users_surrogate_key_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="user_pk_sequence")
    @Basic(optional = false)
    @Column(name = "`pId`")
    private Integer pId;
    @NaturalId
    @Basic(optional = false)
    @Column(name = "`Username`")
    private String username;
    @Column(name = "`Email`")
    private String email;
    @Basic(optional = false)
    @Column(name = "`Password`")
    private String passwordHash;
    @Column(name = "`ActivationKey`")
    private String activationKey;
    @Column(name = "`PasswordQuestion`")
    private String passwordQuestion;
    @Column(name = "`PasswordAnswer`")
    private String passwordAnswer;
    @Column(name = "`IsApproved`")
    private Boolean approved;
    @Column(name = "`LastActivityDate`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastActivityDate;
    @Column(name = "`LastLoginDate`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoginDate;
    @Column(name = "`LastPasswordChangedDate`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastPasswordChangedDate;
    @Column(name = "`CreationDate`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    @Column(name = "`IsOnLine`")
    private Boolean onLine;
    @Column(name = "`IsLockedOut`")
    private Boolean lockedOut;
    
    //@ManyToMany(mappedBy = "usersCollection")
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="`UsersInRoles`",
            joinColumns = {
			@JoinColumn(name = "`pId`")},
            inverseJoinColumns = {
                        @JoinColumn(name = "`Rolename`") })
    private Collection<Role> roles;
    

    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserPreferences userPreferences;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private Collection<Trail> trackCollection;

    public User() {
        this.approved=false;
        this.lockedOut=true;
        this.onLine=false;
    }

    public User(Integer pId) {
        this.approved=false;
        this.lockedOut=true;
        this.onLine=false;
        
        this.pId = pId;
    }

    public User(Integer pId, String username, String passwordHash) {
        this.approved=false;
        this.lockedOut=true;
        this.onLine=false;
        this.pId = pId;
        
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Integer getPId() {
        return this.pId;
    }

    public void setPId(Integer pId) {
        this.pId = pId;
    }

    @NotEmpty(message="This may not be empty, for sure!")
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    @Email
    @NotEmpty
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    @NotEmpty
    public String getPassword() {
        return this.passwordHash;
    }

    public void setPassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getActivationKey() {
        return this.activationKey;
    }

    public void setActivationKey(String ActivationKey) {
        this.activationKey = ActivationKey;
    }

    @NotEmpty
    public String getPasswordQuestion() {
        return this.passwordQuestion;
    }

    public void setPasswordQuestion(String passwordQuestion) {
        this.passwordQuestion = passwordQuestion;
    }
    
    @NotEmpty
    public String getPasswordAnswer() {
        return this.passwordAnswer;
    }
    
    @NotEmpty
    public void setPasswordAnswer(String passwordAnswer) {
        this.passwordAnswer = passwordAnswer;
    }

    public Boolean IsApproved() {
        return this.approved;
    }
    
    public Boolean getApproved() {
        return this.approved;
    }
    
    public void setApproved(Boolean status) {
        this.approved=status;
    }

    public Date getLastActivityDate() {
        return this.lastActivityDate;
    }

    public void setLastActivityDate(Date lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public Date getLastLoginDate() {
        return this.lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public Date getLastPasswordChangedDate() {
        return this.lastPasswordChangedDate;
    }

    public void setLastPasswordChangedDate(Date lastPasswordChangedDate) {
        this.lastPasswordChangedDate = lastPasswordChangedDate;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean IsOnLine() {
        return this.onLine;
    }

    public void setOnLine(Boolean isOnLine) {
        this.onLine = isOnLine;
    }

    public Boolean IsLockedOut() {
        return this.lockedOut;
    }

    public void setLockedOut(Boolean isLockedOut) {
        this.lockedOut = isLockedOut;
    }

    public Collection<Role> getRoles() {
        return this.roles;
    }

    public void setRoles(Collection<Role> roles) {
        this.roles = roles;
    }

    public Collection<Trail> getTracksCollection() {
        return this.trackCollection;
    }

    public void setTracksCollection(Collection<Trail> tracksCollection) {
        this.trackCollection = tracksCollection;
    }
        
    public void addRole(Role newRole){
        if(this.roles==null){
            this.roles=new ArrayList<Role>();
        }
        this.roles.add(newRole);
    }

    public UserPreferences getUserPreferences() {
        return this.userPreferences;
    }

    public void setUserPreferences(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }
    
    public boolean IsNew() {
        return (this.pId == null);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.pId != null ? this.pId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.username == null && other.username != null) || (this.username != null && !this.username.equals(other.username))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.somewhere.gpxclone.entities.Users[ pId=" + this.pId + " ]";
    }
    
    public boolean hasRole(String roleName) {
        Collection<Role> myRoles = getRoles();
        for (Role next : myRoles) {
            if (next.getRolename().equals(roleName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }
    

    @Override
    public boolean isAccountNonExpired() {
       return this.approved;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.lockedOut;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.approved;
    }

    @Override
    public boolean isEnabled() {
        return this.approved;
    }
}
