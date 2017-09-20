package com.leonardyau.topwebsite.model;

import java.util.Date;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Entity for storing website access record, each record has the website name,
 * access count, and the date
 * 
 * @author Leonard
 *
 */
@Data
@AllArgsConstructor
@Document(collection = "websites")
public class WebSiteAccess {

	@Field
	@Indexed
	private String website;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Field
	@Indexed
	private Date date;

	@Field
	private long count;
}
