package com.leonardyau.topwebsite.service;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class FileWatchManagerTest {

	private Path tempdir;

	@Mock
	FileHandler handler;

	@InjectMocks
	private FileWatchManager fm;

	private List<Path> tmpfiles;
	@Before
	public void setup() throws IOException {
		tmpfiles = new ArrayList<>();
		doNothing().when(handler).process(anyObject());
		tempdir = Files.createTempDirectory("donedir");
		tempdir.toFile().deleteOnExit();
		fm.setWatchPath(tempdir.toString());
		fm.setFileExtension(".csv");
	}

	@After
	public void cleanup() {
		for(Path p: tmpfiles) {
			p.toFile().delete();
		}
	}

	@Test
	public void testWatcher() throws IOException, InterruptedException, ExecutionException {
		tmpfiles.add(Files.createTempFile(tempdir, "testcsv1", ".csv"));
		fm.start();
		Thread.sleep(1000);
		tmpfiles.add(Files.createTempFile(tempdir, "testcsv2", ".csv"));
		tmpfiles.add(Files.createTempFile(tempdir, "testcsv3", ".cxv"));
		Thread.sleep(3000);
		verify(handler, times(2)).process(anyObject());
		verify(handler, times(1)).process(tmpfiles.get(0));
		verify(handler, times(1)).process(tmpfiles.get(1));
		fm.stop();
	}
}
