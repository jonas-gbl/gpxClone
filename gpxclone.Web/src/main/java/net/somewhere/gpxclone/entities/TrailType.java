/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.entities;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.NaturalId;

/**
 *
 * @author Jonas
 */
@Entity
@Table(name = "`TrackTypes`")
public class TrailType implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @SequenceGenerator(name="tracktype_pk_sequence",sequenceName="tracktypes_surrogate_key_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="tracktype_pk_sequence")
    @Column(name = "`typeID`")
    private Integer typeID;
    @NaturalId
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "`Typename`")
    private String typename;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
    private Collection<Trail> tracksCollection;

    public TrailType() {
    }

    public TrailType(Integer typeID) {
        this.typeID = typeID;
    }

    public TrailType(Integer typeID, String typename) {
        this.typeID = typeID;
        this.typename = typename;
    }

    public Integer getTypeID() {
        return typeID;
    }

    public void setTypeID(Integer typeID) {
        this.typeID = typeID;
    }

    public String getTypename() {
        return typename;
    }

    public void setTypename(String typename) {
        this.typename = typename;
    }

    public Collection<Trail> getTracksCollection() {
        return tracksCollection;
    }

    public void setTracksCollection(Collection<Trail> tracksCollection) {
        this.tracksCollection = tracksCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (typeID != null ? typeID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TrailType)) {
            return false;
        }
        TrailType other = (TrailType) object;
        if ((this.typeID == null && other.typeID != null) || (this.typeID != null && !this.typeID.equals(other.typeID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.somewhere.gpxclone.entities.TrackTypes[ typeID=" + typeID + " ]";
    }
    
}
