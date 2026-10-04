package com.rk.pulseboard.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IncidentTagId implements Serializable {
    private Long incidentId;
    private Long tagId;

    public IncidentTagId() {}

    public IncidentTagId(Long incidentId, Long tagId) {
        this.incidentId = incidentId;
        this.tagId = tagId;
    }

    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncidentTagId toughness = (IncidentTagId) o;
        return Objects.equals(incidentId, toughness.incidentId) &&
                Objects.equals(tagId, toughness.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(incidentId, tagId);
    }
}
