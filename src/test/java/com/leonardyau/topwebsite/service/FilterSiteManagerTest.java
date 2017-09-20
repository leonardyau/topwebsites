package com.leonardyau.topwebsite.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;

import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.leonardyau.topwebsite.model.FilterSite;

@RunWith(SpringRunner.class)
public class FilterSiteManagerTest {

	@Mock
	private RestTemplate resttemplate;

	@InjectMocks
	private FilterSiteManager filter;

	@Before
	public void setup() {
		FilterSite[] sites = new FilterSite[5];
		sites[0] = new FilterSite("facebook.com", new GregorianCalendar(2016, 12, 1).getTime(), null);
		sites[1] = new FilterSite("google.com", new GregorianCalendar(2016, 3, 12).getTime(),
				new GregorianCalendar(2016, 3, 14).getTime());
		sites[2] = new FilterSite("hello.com", null, null);
		sites[3] = new FilterSite("oldsite.com", new GregorianCalendar(1980, 3, 12).getTime(),
				new GregorianCalendar(1981, 3, 14).getTime());
		sites[4] = new FilterSite("newsite.com", new GregorianCalendar(2016, 3, 12).getTime(),
				new GregorianCalendar(2016, 3, 14).getTime());
		Mockito.when(resttemplate.getForObject(anyString(), anyObject())).thenReturn(sites);

		filter.setCacheTTL(1);
	}

	@Test
	public void testFilterSite() {
		assertFalse(filter.filteredSite("facebook.com", new GregorianCalendar(2016, 1, 11).getTime()));
		assertTrue(filter.filteredSite("facebook.com", new GregorianCalendar(2016, 12, 1).getTime()));
		assertTrue(filter.filteredSite("facebook.com", new GregorianCalendar(2047, 1, 11).getTime()));
		assertTrue(filter.filteredSite("hello.com", new GregorianCalendar(2016, 1, 11).getTime()));
		assertFalse(filter.filteredSite("google.com", new GregorianCalendar(2016, 4, 11).getTime()));
		assertTrue(filter.filteredSite("google.com", new GregorianCalendar(2016, 3, 12).getTime()));
		assertFalse(filter.filteredSite("www.newsite.com", new GregorianCalendar(2017, 1, 1).getTime()));
		assertTrue(filter.filteredSite("123.oldsite.com", new GregorianCalendar(1980, 12, 12).getTime()));
		assertFalse(filter.filteredSite("123.oldsite.com.hk", new GregorianCalendar(1980, 12, 12).getTime()));

	}

	@Test
	public void testCache() throws InterruptedException {
		filter.getFilterSites();
		filter.getFilterSites();
		Thread.sleep(2000);
		;
		filter.getFilterSites();
		filter.getFilterSites();
		Mockito.verify(resttemplate, times(2)).getForObject(anyString(), anyObject());

	}

	@Test
	public void testMatchWebsite() {
		assertTrue(FilterSiteManager.matchSiteUrl("www.facebook.com", "facebook.com"));
		assertTrue(FilterSiteManager.matchSiteUrl("www12.facebook.com.hk", "facebook.com.hk"));
		assertTrue(FilterSiteManager.matchSiteUrl("facebook.com", "facebook.com"));
		assertFalse(FilterSiteManager.matchSiteUrl("google.com", "facebook.com"));
	}

	@Test
	public void testWithinPeriod() {
		assertTrue(FilterSiteManager.withinPeriod(new GregorianCalendar(2017, 1, 11).getTime(),
				new GregorianCalendar(2017, 1, 1).getTime(), null));
		assertTrue(FilterSiteManager.withinPeriod(new GregorianCalendar(2017, 1, 3).getTime(),
				new GregorianCalendar(2016, 12, 31).getTime(), new GregorianCalendar(2017, 1, 3).getTime()));
		assertFalse(FilterSiteManager.withinPeriod(new GregorianCalendar(2017, 1, 4).getTime(),
				new GregorianCalendar(2016, 12, 31).getTime(), new GregorianCalendar(2017, 1, 3).getTime()));
//		assertFalse(FilterSiteManager.withinPeriod(LocalDate.of(2016, 12, 4), LocalDate.of(2016, 12, 31), null));
//		assertFalse(FilterSiteManager.withinPeriod(LocalDate.of(2016, 12, 4), LocalDate.of(2016, 12, 31), null));
//		assertTrue(FilterSiteManager.withinPeriod(LocalDate.of(2016, 12, 4), null, null));
//		assertTrue(FilterSiteManager.withinPeriod(LocalDate.of(2016, 12, 4), null, LocalDate.of(2017, 1, 1)));
//		assertFalse(FilterSiteManager.withinPeriod(LocalDate.of(2017, 12, 4), null, LocalDate.of(2017, 1, 1)));
	}

}
