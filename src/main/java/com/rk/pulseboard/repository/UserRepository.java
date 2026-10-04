package com.rk.pulseboard.repository;

import com.rk.pulseboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
