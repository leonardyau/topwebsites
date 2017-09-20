package com.leonardyau.topwebsite.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.leonardyau.topwebsite.model.WebSiteAccess;
import com.mongodb.WriteResult;

public class WebSiteRepositoryImpl implements WebSiteRepositoryCustom {

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public int upsertRecord(WebSiteAccess wa) {

		Query query = new Query(Criteria.where("website").is(wa.getWebsite())
				.and("date").is(wa.getDate()));
		Update update = new Update();
		update.set("count", wa.getCount());

		WriteResult result = mongoTemplate.upsert(query, update,
				WebSiteAccess.class);

		if (result != null)
			return result.getN();
		else
			return 0;

	}

}
