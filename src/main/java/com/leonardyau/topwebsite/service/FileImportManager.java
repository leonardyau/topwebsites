package com.leonardyau.topwebsite.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.leonardyau.topwebsite.model.ImportHistory;
import com.leonardyau.topwebsite.model.WebSiteAccess;
import com.leonardyau.topwebsite.repository.ImportHistoryRepository;
import com.leonardyau.topwebsite.repository.WebSiteRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileImportManager implements FileHandler{

	@Getter
	@Setter
	@Value("${fileimport.done.path}")
	private volatile String donePath;


	@Getter
	@Setter
	@Value("${fileimport.csvdelimiter}")
	private volatile String delimiter;

	@Autowired
	private WebSiteRepository websiterepository;

	@Autowired
	private ImportHistoryRepository history;
	
	
	void moveFile(Path path) throws IOException {
		Path newpath = null;
		boolean done = false;
		String filename = path.getFileName().toString();
		int dotindex = filename.lastIndexOf('.');
		String origname = filename.substring(0, dotindex);
		String ext = filename.substring(dotindex, filename.length());
		int i = 1;
		while (!done) {
			newpath = Paths.get(donePath.toString(), filename);
			if (Files.exists(newpath)) {
				filename = origname + i + ext;
				i++;
			} else {
				done = true;
			}
		}

		Files.move(path, newpath);
	}

	WebSiteAccess extractRecord(String line) {
		WebSiteAccess w = null;
		String[] fields = line.split(Pattern.quote(delimiter));
		if( fields.length>2 )
		{
			Date date;
			long count;
			try {
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				format.setTimeZone(TimeZone.getTimeZone("UTC"));
				date = format.parse(fields[0]);
				count = Long.parseLong(fields[2]);
			} catch (NumberFormatException| ParseException e) {
				return w;
			}			
			w = new WebSiteAccess(fields[1], date, count);
		}
		return w;
	}
	
	@Override
	public
	void process(Path path) {
		int success = 0;
		int total = 0;
		log.info("Start processing {}", path);
		FileLock lock = null;
		RandomAccessFile csvfile = null;
		String line = null;
		try {
			csvfile = new RandomAccessFile( new File(path.toString()), "rw" );
			FileChannel channel = csvfile.getChannel( );
			lock = channel.lock( );			
			while ((line = csvfile.readLine()) != null) {
				total++;
				WebSiteAccess w = extractRecord(line);
				if( w != null ) {
					websiterepository.upsertRecord(w);
					success++;
				}
			}
		} catch (IOException e) {
			log.error("Error in importing {} of file {}", line, path.toString());
		} finally {
			
			try {
				log.info("Done processing {}, {} lines read, {} records imported", path, total, success );
				history.save( new ImportHistory(new Date(), path.toString(), total, success) );
				if( lock != null && lock.isValid() )
					lock.release();
				if( csvfile != null )
					csvfile.close();
				
				moveFile(path);
			} catch (IOException e) {
				log.error("Error in finishing import {}", e.getMessage());
			}
		}

	}
}
