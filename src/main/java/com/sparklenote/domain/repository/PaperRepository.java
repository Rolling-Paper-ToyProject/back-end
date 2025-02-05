package com.sparklenote.domain.repository;

import com.sparklenote.domain.entity.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Long> {
    List<Paper> findByRoll_Id(Long rollId);

}
