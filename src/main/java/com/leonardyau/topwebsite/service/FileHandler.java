package com.leonardyau.topwebsite.service;

import java.nio.file.Path;

/**
 * Interface for handling the file being detected by the watcher
 * @author Leonard
 *
 */
public interface FileHandler {
	/**
	 * What to do with the file
	 * 
	 * @param path File to be processed
	 */
	public void process(Path path);
}
