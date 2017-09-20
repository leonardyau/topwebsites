package com.leonardyau.topwebsite.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.leonardyau.topwebsite.model.FilterSite;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FilterSiteManager {

	@Getter
	@Setter
	@Value("${filter.URL}")
	private String filterURL;

	@Getter
	@Setter
	@Value("${filter.cache.TTL}")
	private long cacheTTL;

	@Autowired
	private RestTemplate restTemplate;

	@Getter
	private Date expireTime;
	
	private List<FilterSite> filteredSites;

	public synchronized List<FilterSite> getFilterSites() {
		if (expireTime == null || expireTime.compareTo(new Date()) < 0) {
			log.info("Getting filted sites from {}", filterURL);
			FilterSite[] result;
			try {
				result = restTemplate.getForObject(filterURL, FilterSite[].class);
				log.info("Got {} entires back", result.length);
			} catch (RestClientException e) {
				log.warn("Failed in getting filtered sites from {}, assuming no exclusion", filterURL);
				log.warn("Error: {}", e.getMessage());
				// just return empty site for now, wait for next cache TTL and hope it will
				// be back to service
				result = new FilterSite[0];
			}
			filteredSites = Arrays.asList(result);
			expireTime = new Date(System.currentTimeMillis()+cacheTTL*1000);
		} else {
			log.info("Using cached copy filted sites expires at {}", expireTime);
		}
		return filteredSites;
	}

	public void resetCache() {
		expireTime = null;
	}
	
	static boolean matchSiteUrl(String url, String hostname) {
		return url.endsWith(hostname);
	}


	static boolean withinPeriod(Date d, Date start, Date end) {
		if (start != null && d.before(start))
			return false;
		if (end != null && d.after(end))
			return false;
		return true;
	}

	public boolean filteredSite(String website, Date date) {

		Stream<FilterSite> s = getFilterSites().stream();
		return s.anyMatch(f -> matchSiteUrl(website, f.getHost())
				&& withinPeriod(date, f.getExcludedSince(), f.getExcludedTill()));
	}
}
