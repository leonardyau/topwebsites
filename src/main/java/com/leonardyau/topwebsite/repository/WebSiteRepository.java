package com.leonardyau.topwebsite.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.leonardyau.topwebsite.model.WebSiteAccess;

public interface WebSiteRepository extends MongoRepository<WebSiteAccess, String>, WebSiteRepositoryCustom {
	Page<WebSiteAccess> findAll(Pageable pageable);

	List<WebSiteAccess> findByDate(Date date, Pageable pageable);

}
