package net.somewhere.gpxclone.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "`UserPreferences`", catalog = "`gpxCloneDB`", schema = "`public`")    
public class UserPreferences implements Serializable {
    private static final long serialVersionUID = 1L;
    @GenericGenerator(name = "generator", strategy = "foreign", 
        parameters = @Parameter(name = "property", value = "user"))
    @Id
    @GeneratedValue(generator = "generator")
    @Column(name = "`pId`")
    private Integer pId;
    @Basic(optional = false)
    @Column(name = "`profilePolicy`")
    private String profilePolicy;
    @Basic(optional = false)
    @Column(name = "`gender`")
    private String gender;
    @Column(name = "`richEmailFormat`")
    private Boolean richEmailFormat;
    @Column(name = "`marketingOptIn`")
    private Boolean marketingOptIn;
    @Column(name = "`photoImageType`")
    private String photoImageType;
    @Column(name = "`profilePhoto`")
    private byte[] profilePhoto;
    @Column(name = "`birthYear`")
    private Integer birthYear;
    @Column(name = "`displayName`")
    private String displayName;
    @Column(name = "`description`")
    private String description;
    @Column(name = "`country`")
    private String country;
    @Column(name = "`region`")
    private String region;
    
    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

    public UserPreferences() {
    }

    public UserPreferences(Integer pId) {
        this.pId= pId;
    }

    public UserPreferences(Integer pId, String profilePolicy, String gender) {
        this.pId = pId;
        this.profilePolicy = profilePolicy;
        this.gender = gender;
    }

    public Integer getPId() {
        return pId;
    }

    public void setPId(Integer pId) {
        this.pId = pId;
    }

    public String getProfilePolicy() {
        return profilePolicy;
    }

    public void setProfilePolicy(String profilePolicy) {
        this.profilePolicy = profilePolicy;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Boolean getRichEmailFormat() {
        return richEmailFormat;
    }

    public void setRichEmailFormat(Boolean richEmailFormat) {
        this.richEmailFormat = richEmailFormat;
    }

    public Boolean getMarketingOptIn() {
        return marketingOptIn;
    }

    public void setMarketingOptIn(Boolean marketingOptIn) {
        this.marketingOptIn = marketingOptIn;
    }

    public String getPhotoImageType() {
        return photoImageType;
    }

    public void setPhotoImageType(String photoImageType) {
        this.photoImageType = photoImageType;
    }

    
    public byte[] getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(byte[] profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pId != null ? pId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserPreferences)) {
            return false;
        }
        UserPreferences other = (UserPreferences) object;
        if ((this.pId == null && other.pId != null) || (this.pId != null && !this.pId.equals(other.pId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.somewhere.gpxclone.entities.UserPreferences[ username=" + pId + " ]";
    }
    
}
