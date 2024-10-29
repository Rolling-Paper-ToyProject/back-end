package com.sparklenote.domain.repository;

import com.sparklenote.domain.entity.Roll;
import com.sparklenote.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RollRepository extends JpaRepository<Roll, Long> {
    Optional<Roll> findByUrl(String url);
    boolean existsByUrl(String url);
    List<Roll> findAllByUser(User user);
}
