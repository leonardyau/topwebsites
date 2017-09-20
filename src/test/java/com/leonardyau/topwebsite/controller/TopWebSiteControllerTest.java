package com.leonardyau.topwebsite.controller;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.Model;

import com.leonardyau.topwebsite.model.FilterSite;
import com.leonardyau.topwebsite.model.WebSiteAccess;
import com.leonardyau.topwebsite.repository.WebSiteRepository;
import com.leonardyau.topwebsite.service.FileImportManager;
import com.leonardyau.topwebsite.service.FileWatchManager;
import com.leonardyau.topwebsite.service.FilterSiteManager;

@RunWith(SpringRunner.class)
public class TopWebSiteControllerTest {

	@Mock
	private WebSiteRepository websiterepository;
	@Mock
	private FilterSiteManager filtersitemanager;
	@Mock
	private FileImportManager importmanager;
	@Mock
	private FileWatchManager watcher;
	@Mock
	private Model model;
	@InjectMocks
	TopWebSiteController c;
	
	private List<WebSiteAccess> fullList;
	private List<WebSiteAccess> filteredList;
	private List<FilterSite> exclusionList;

	@Before
	public void setup() {
		fullList = new ArrayList<>(2);
		fullList.add(new WebSiteAccess("www.abc.com", new GregorianCalendar(1997, 6, 1).getTime(), 123));
		fullList.add(new WebSiteAccess("www.def.com", new GregorianCalendar(1997, 6, 1).getTime(), 123));
		filteredList = new ArrayList<>(1);
		filteredList.add(new WebSiteAccess("www.abc.com", new GregorianCalendar(1997, 6, 1).getTime(), 123));
		exclusionList = new ArrayList<>(1);
		exclusionList.add(new FilterSite("def.com", null, null));
		
		doNothing().when(watcher).start();
		doNothing().when(watcher).stop();
		doNothing().when(filtersitemanager).setFilterURL(anyString());
		doNothing().when(filtersitemanager).setCacheTTL(anyLong());
		when(model.addAttribute(anyString(), anyObject())).thenReturn(null);
		when(websiterepository.findByDate(anyObject(), anyObject())).thenReturn(fullList);
		when(filtersitemanager.getFilterSites()).thenReturn(exclusionList);
	}

	@Test
	public void testSettings() throws IOException {
		Path tempdir;
		Path tempdir2;
		tempdir = Files.createTempDirectory("dir1");
		tempdir.toFile().deleteOnExit();
		tempdir2 = Files.createTempDirectory("dir2");
		tempdir2.toFile().deleteOnExit();

		c.settings(tempdir.toString(), tempdir.toString(), model);
		c.settings("#$#$", tempdir.toString(), model);
		c.settings(tempdir.toString(), "@#@3", model);
		verify(watcher, times(0)).setWatchPath(anyObject());
		verify(importmanager, times(0)).setDonePath(anyObject());
		c.settings(tempdir.toString(), tempdir2.toString(), model);
		verify(watcher, times(1)).setWatchPath(tempdir.toString());
		verify(importmanager, times(1)).setDonePath(tempdir2.toString());
		verify(watcher, times(1)).start();

	}

	@Test
	public void testSetexclusionURL() throws IOException {
		c.setexclusion("badurl", 10, model);
		c.setexclusion("ftp://badurl", 10, model);
		c.setexclusion("ftp://b a durl", 10, model);
		c.setexclusion("http::b xxa durl", 10, model);
		verify(filtersitemanager, times(0)).setFilterURL(anyString());


		c.setexclusion("http://a.com", -1, model);
		verify(filtersitemanager, times(0)).setFilterURL(anyString());
		c.setexclusion("http://b.com", 10, model);
		c.setexclusion("https://b.com", 20, model);
		verify(filtersitemanager, times(2)).setFilterURL(anyString());
		verify(filtersitemanager, times(1)).setCacheTTL(10);
		verify(filtersitemanager, times(1)).setCacheTTL(20);
		verify(filtersitemanager, times(2)).resetCache();

	}

	@Test
	public void testResult(){
		c.result("abcd-12-01", 10, model);
		c.result("2091-01-01", -1, model);
		verify(websiterepository, times(0)).findByDate(anyObject(), anyObject());
	}
	
	@Test
	public void testFilterResult() {
		c.result("1997-7-1", 1, model);
		verify(model, times(1)).addAttribute("websites", filteredList);

	}
	
}
