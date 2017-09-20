package com.leonardyau.topwebsite.service;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileWatchManager {

	@Setter
	@Getter
	@Value("${filewatch.path}")
	private volatile String watchPath;

	@Setter
	@Getter
	@Value("${filewatch.extension}")
	private volatile String fileExtension;

	@Autowired
	FileHandler filehandler;

	private ExecutorService executor;

	private volatile FileWatchWorker worker = null;
	private Future<?> status;

	public FileWatchManager() {
		executor = Executors.newSingleThreadScheduledExecutor();
	}

	
	public synchronized void start() {
		if (worker != null) {
			log.warn("File watcher service is already running on {}", watchPath.toString());
			return;
		}

		try {
			worker = new FileWatchWorker(Paths.get(watchPath), fileExtension);
		} catch (IOException e) {
			log.warn("Error in setting up watcher {}", e.getMessage());
		}
		status = executor.submit(worker);
	}

	public synchronized void stop() {
		try {
			log.info("Stopping file watcher service");
			worker.stop();
			status.get();
			worker = null;
			log.info("Stopped file watcher service");
		} catch (InterruptedException | ExecutionException e) {
			log.warn("Error in stopping watcher service {}", e.getMessage());
		}
	}

	class FileWatchWorker implements Runnable {
		private WatchService watchService;
		private String extension;
		private Path path;

		public FileWatchWorker(Path path, String extension) throws IOException {
			this.extension = extension;
			this.path = path;
			watchService = FileSystems.getDefault().newWatchService();
			path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.OVERFLOW);
		}

		private boolean matchFile(Path p) {
			return p.toFile().isFile() && p.getFileName().toString().toLowerCase().endsWith(extension);
		}

		void checkFolder(Path path) throws IOException {
			Files.walk(path, 1).filter(p -> matchFile(p)).forEach(p -> filehandler.process(p));
			// Files.walk(path, 1).forEach(fileFunc);
			// && p.getFileName().getClass().toString().toLowerCase().endsWith("csv")
		}

		void stop() {
			try {
				watchService.close();
			} catch (IOException e) {
				log.warn("Error in stopping watcher thread {}", e.getMessage());
			}
		}

		@Override
		public void run() {
			try {
				log.info("File watcher service is starting for {}", watchPath.toString());
				WatchKey k;
				checkFolder(path);
				while (true) {
					k = watchService.take();
					List<WatchEvent<?>> events = (List<WatchEvent<?>>)k.pollEvents();
					log.info("File watcher events detected : {}", events.size());
					for (WatchEvent<?> event : events) {
						if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
							Path foundpath = Paths.get(path.toString(), ((Path) event.context()).toString());
							log.info("Detected file: {}", foundpath);
							if (matchFile(foundpath)) {
								filehandler.process(foundpath);
							}
						} else if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
							checkFolder(path);
						}
					}
					k.reset();
				}
			} catch (IOException | InterruptedException e) {
				log.warn("Error in file watcher {} {}", path, e.getMessage());
				return;
			} catch (ClosedWatchServiceException l) {
				
			}
		}
	}

}
