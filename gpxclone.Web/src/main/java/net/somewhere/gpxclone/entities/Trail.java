/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.somewhere.gpxclone.entities;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

/**
 *
 * @author Jonas
 */
@Entity
@Table(name = "`Tracks`")
public class Trail implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @SequenceGenerator(name="track_pk_sequence",sequenceName="tracks_surrogate_key_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="track_pk_sequence")
    @Column(name = "`trackID`")
    private Integer trackID;

    @Column(name = "`trace`",columnDefinition="Geometry")
    @Type(type = "org.hibernate.spatial.GeometryType")
    private LineString trace;
    
    @JoinColumn(name = "`owner`", referencedColumnName = "`pId`")
    @ManyToOne(optional = false)
    private User owner;
    
    @JoinColumn(name = "`type`", referencedColumnName = "`typeID`")
    @ManyToOne(optional = false)
    private TrailType type;

    public Trail() {
    }

    public Trail(Integer trackID) {
        this.trackID = trackID;
    }

    public Integer getTrackID() {
        return trackID;
    }

    public void setTrackID(Integer trackID) {
        this.trackID = trackID;
    }

    public LineString getTrace() {
        return (LineString)trace;
    }

    public void setTrace(LineString trace) {
        this.trace = trace;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public TrailType getType() {
        return type;
    }

    public void setType(TrailType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (trackID != null ? trackID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Trail)) {
            return false;
        }
        Trail other = (Trail) object;
        if ((this.trackID == null && other.trackID != null) || (this.trackID != null && !this.trackID.equals(other.trackID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.somewhere.gpxclone.entities.Tracks[ trackID=" + trackID + " ]";
    }
    
}
