package com.rk.pulseboard.repository;

import com.rk.pulseboard.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
}
