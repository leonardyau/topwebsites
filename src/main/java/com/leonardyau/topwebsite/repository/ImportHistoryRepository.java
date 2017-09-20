package com.leonardyau.topwebsite.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.leonardyau.topwebsite.model.ImportHistory;

public interface ImportHistoryRepository extends  MongoRepository<ImportHistory, String> {
	Page<ImportHistory> findAll(Pageable pageable);
}
