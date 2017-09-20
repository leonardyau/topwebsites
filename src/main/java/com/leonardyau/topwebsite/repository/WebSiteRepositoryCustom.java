package com.leonardyau.topwebsite.repository;

import com.leonardyau.topwebsite.model.WebSiteAccess;

/**
 * Interface for the upsert method
 * 
 * @author Leonard
 *
 */
public interface WebSiteRepositoryCustom {
	/**
	 * Compare the website name and the date, upsert the record to repository
	 * 
	 * @param wa
	 * @return
	 */
	public int upsertRecord(WebSiteAccess wa);
}
