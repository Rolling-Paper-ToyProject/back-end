package com.sparklenote.domain.repository;

import com.sparklenote.domain.entity.Roll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RollRepository extends JpaRepository<Roll, Long> {

    boolean existsByUrl(String url);

}
