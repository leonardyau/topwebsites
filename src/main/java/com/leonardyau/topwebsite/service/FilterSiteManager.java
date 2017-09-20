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

/**
 * Service to filter excluded websites. For performance consideration, the list
 * is cached
 * 
 * @author Leonard
 *
 */
@Service
@Slf4j
public class FilterSiteManager {

	@Getter
	@Setter
	@Value("${filter.URL}")
	// URL to get the exclusion list
	private String filterURL;

	@Getter
	@Setter
	@Value("${filter.cache.TTL}")
	// Cache evict period in seconds
	private long cacheTTL;

	@Autowired
	private RestTemplate restTemplate;

	@Getter
	private Date expireTime;

	private List<FilterSite> filteredSites;

	/**
	 * Function to get the exclusion list by making rest call. The cached copy is
	 * returned if not expire yet
	 * 
	 * @return
	 */
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
				// Remote service down, just return empty site for now, wait for next cache TTL
				// and hope it will be back to service
				result = new FilterSite[0];
			}
			filteredSites = Arrays.asList(result);
			expireTime = new Date(System.currentTimeMillis() + cacheTTL * 1000);
		} else {
			log.info("Using cached copy filted sites expires at {}", expireTime);
		}
		return filteredSites;
	}

	public void resetCache() {
		expireTime = null;
	}

	/**
	 * Helper function to match the website, only compare with the last part of the
	 * hostname e.g. Input name www1.facebook.com with the exclusion entry
	 * facebook.com will be excluded
	 * 
	 * @param url
	 *            URL to check for
	 * @param hostname
	 *            The excluded base hostname
	 * @return True if the URL is matched
	 */
	static boolean matchSiteUrl(String url, String hostname) {
		return url.endsWith(hostname);
	}

	/**
	 * Helper fucntion to check if the site is within the exclusion period
	 * (inclusive)
	 * 
	 * @param d
	 *            The date to check against
	 * @param start
	 *            The exclusion start date, could be null, means start from
	 *            beginning
	 * @param end
	 *            the exclusion end date, could be null, means no end date
	 * @return True if the input date is within the exclusion period
	 */
	static boolean withinPeriod(Date d, Date start, Date end) {
		if (start != null && d.before(start))
			return false;
		if (end != null && d.after(end))
			return false;
		return true;
	}

	/**
	 * Function to check if a URL is to be excluded
	 * 
	 * @param website
	 *            URL name
	 * @param date
	 *            The date of the URL to be checked
	 * @return Return true if the URL is to be excluded
	 */
	public boolean filteredSite(String website, Date date) {

		Stream<FilterSite> s = getFilterSites().stream();
		return s.anyMatch(f -> matchSiteUrl(website, f.getHost())
				&& withinPeriod(date, f.getExcludedSince(), f.getExcludedTill()));
	}
}
