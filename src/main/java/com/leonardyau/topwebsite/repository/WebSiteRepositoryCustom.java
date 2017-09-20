package com.leonardyau.topwebsite.repository;

import com.leonardyau.topwebsite.model.WebSiteAccess;

public interface WebSiteRepositoryCustom {
	public int upsertRecord(WebSiteAccess wa);
}
