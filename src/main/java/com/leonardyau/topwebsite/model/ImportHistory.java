package com.leonardyau.topwebsite.model;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Entity for storing the import history
 * 
 * @author Leonard
 *
 */
@Data
@AllArgsConstructor
@Document(collection = "history")
public class ImportHistory {

	private Date date;
	private String filename;
	private int total;
	private int imported;
	private long duration;

}
