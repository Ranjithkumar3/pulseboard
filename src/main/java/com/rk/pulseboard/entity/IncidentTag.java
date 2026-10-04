package com.rk.pulseboard.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "incident_tags")
public class IncidentTag {
    @EmbeddedId
    private IncidentTagId id = new IncidentTagId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("incidentId")
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public IncidentTag() {}

    public IncidentTag(Incident incident, Tag tag) {
        this.incident = incident;
        this.tag = tag;
        this.id = new IncidentTagId(incident.getId(), tag.getId());
    }

    public IncidentTagId getId() { return id; }
    public void setId(IncidentTagId id) { this.id = id; }

    public Incident getIncident() { return incident; }
    public void setIncident(Incident incident) { this.incident = incident; }

    public Tag getTag() { return tag; }
    public void setTag(Tag tag) { this.tag = tag; }
}
