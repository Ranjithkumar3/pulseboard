package com.rk.pulseboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "incident_tags")
public class IncidentTag {
    @Column(nullable = false)
    private String incidentId;

    @Column(nullable = false)
    private String tagId;

    public IncidentTag() {}

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }
}
