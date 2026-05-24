package com.checkmate.persistence.repository;

import com.checkmate.persistence.entity.GameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRecordRepository extends JpaRepository<GameRecord, String> {
}