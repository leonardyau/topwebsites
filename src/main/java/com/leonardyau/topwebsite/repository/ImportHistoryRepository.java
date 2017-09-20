package com.leonardyau.topwebsite.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.leonardyau.topwebsite.model.ImportHistory;

/**
 * Repository for storing the history of importing workfiles
 * @author Leonard
 *
 */
public interface ImportHistoryRepository extends  MongoRepository<ImportHistory, String> {
	Page<ImportHistory> findAll(Pageable pageable);
}
