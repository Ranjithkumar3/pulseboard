package com.rk.pulseboard.repository;

import com.rk.pulseboard.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
