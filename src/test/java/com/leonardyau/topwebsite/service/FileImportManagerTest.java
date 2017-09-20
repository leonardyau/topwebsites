package com.leonardyau.topwebsite.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

import com.leonardyau.topwebsite.model.WebSiteAccess;
import com.leonardyau.topwebsite.repository.WebSiteRepository;

@RunWith(SpringRunner.class)
public class FileImportManagerTest {
	@Mock
	private WebSiteRepository repository;

	@InjectMocks
	private FileImportManager fm;

	private Path donedir;

	@Before
	public void setup() throws IOException {
		MockitoAnnotations.initMocks(this);

		donedir = Files.createTempDirectory("donedir");
		donedir.toFile().deleteOnExit();
		fm.setDonePath(donedir.toString());
		fm.setDelimiter("|");
		when(repository.upsertRecord(Mockito.isA(WebSiteAccess.class))).thenReturn(1);
	}

	@Test
	public void testImportFile() throws IOException {

		try {
			Path tmpfile = Files.createTempFile("website", ".csv");
			tmpfile.toFile().deleteOnExit();
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmpfile.toString()));
			writer.write("2016-01-06|www.a.com|1");
			writer.newLine();
			writer.write("2016-01-07|www.b.com|2");
			writer.newLine();
			writer.write("12/2/2|xxx|4");
			writer.newLine();
			writer.write("2016-01-08,www.c.com,3");
			writer.newLine();
			writer.write("date|website|visits");
			writer.newLine();
			writer.close();
			fm.process(tmpfile);
			WebSiteAccess firstrec = new WebSiteAccess("www.a.com", new GregorianCalendar(2016, 0, 6).getTime(), 1);
			WebSiteAccess secondrec = new WebSiteAccess("www.b.com", new GregorianCalendar(2016, 0, 7).getTime(), 2);
			verify(repository, times(2)).upsertRecord(anyObject());
			verify(repository, times(1)).upsertRecord(firstrec);
			verify(repository, times(1)).upsertRecord(secondrec);
			assertEquals(donedir.toFile().listFiles().length, 1);
		} finally {
			for (File f : donedir.toFile().listFiles()) {
				f.delete();
			}
		}
	}

}
