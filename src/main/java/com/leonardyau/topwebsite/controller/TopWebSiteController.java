package com.leonardyau.topwebsite.controller;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.leonardyau.topwebsite.model.FilterSite;
import com.leonardyau.topwebsite.model.ImportHistory;
import com.leonardyau.topwebsite.model.WebSiteAccess;
import com.leonardyau.topwebsite.repository.ImportHistoryRepository;
import com.leonardyau.topwebsite.repository.WebSiteRepository;
import com.leonardyau.topwebsite.service.FileImportManager;
import com.leonardyau.topwebsite.service.FileWatchManager;
import com.leonardyau.topwebsite.service.FilterSiteManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller for the top website app
 * 
 * @author Leonard
 *
 */
@Controller
@Slf4j
public class TopWebSiteController {

	@Autowired
	private WebSiteRepository websiterepository;
	@Autowired
	private ImportHistoryRepository historyrepository;
	@Autowired
	private FilterSiteManager filtersitemanager;
	@Autowired
	private FileImportManager importmanager;
	@Autowired
	private FileWatchManager watcher;

	/**
	 * Default page
	 * 
	 * @param model
	 *            The model inherited
	 * @return The view to be used
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String index(Model model) {
		return "index";
	}

	/**
	 * Login page, takes the error information from last failed login attempt
	 * 
	 * @param error
	 *            Error message to display
	 * @param model
	 *            The model inherited
	 * @return The view to be used
	 */
	@RequestMapping(value = "login", method = RequestMethod.GET)
	public String login(@RequestParam(required = false, defaultValue = "false") boolean error, Model model) {
		if (error) {
			model.addAttribute("param.error", "true");
		}
		return "login";
	}

	/**
	 * Setting information
	 * 
	 * @param model
	 *            The model inherited
	 * @return The view to be used
	 */
	@RequestMapping(value = "settings", method = RequestMethod.GET)
	public String settings(Model model) {

		// if the configuration is already set, don't retrieve the current value
		if (!model.containsAttribute("exclusionurl"))
			model.addAttribute("exclusionurl", filtersitemanager.getFilterURL());
		if (!model.containsAttribute("cachettl"))
			model.addAttribute("cachettl", filtersitemanager.getCacheTTL());
		if (!model.containsAttribute("donepath"))
			model.addAttribute("donepath", importmanager.getDonePath());
		if (!model.containsAttribute("watchpath"))
			model.addAttribute("watchpath", watcher.getWatchPath());
		model.addAttribute("importstatus", watcher.isRunning());
		return "settings";
	}

	/**
	 * To update exclusion service parameters, clear the cache after successful
	 * update
	 * 
	 * @param exclusionurl
	 *            URL to get the exclusion list
	 * @param cacheTTL
	 *            Cache refresh time (seconds)
	 * @param model
	 *            The model inherited
	 * @return The view to be used
	 */
	@RequestMapping(value = "setexclusion", method = RequestMethod.POST)
	public String setexclusion(@RequestParam String exclusionurl, @RequestParam int cacheTTL, Model model) {
		model.addAttribute("cachettl", cacheTTL);
		model.addAttribute("exclusionurl", exclusionurl);
		if (cacheTTL < 0) {
			model.addAttribute("error", "CacheTTL must be > 0");
		} else if (exclusionurl == null || !ResourceUtils.isUrl(exclusionurl)
				|| !exclusionurl.trim().toLowerCase().startsWith("http")) {
			model.addAttribute("error", exclusionurl + " is not a valid URL");
		} else {
			filtersitemanager.setFilterURL(exclusionurl.trim());
			filtersitemanager.setCacheTTL(cacheTTL);
			filtersitemanager.resetCache();
			model.addAttribute("message", "Exclusion settings updated.");
		}
		return settings(model);
	}

	/**
	 * To update file watcher service parameters, restart after successful update
	 * 
	 * @param watchpath
	 *            Folder to watch for
	 * @param donepath
	 *            Folder to move the imported files
	 * @param model
	 *            The model inherited
	 * @return The view to be used
	 */
	@RequestMapping(value = "setwatch", method = RequestMethod.POST)
	public String settings(@RequestParam String watchpath, @RequestParam String donepath, Model model) {
		File dpath = new File(donepath);
		File wpath = new File(watchpath);
		model.addAttribute("donepath", donepath);
		model.addAttribute("watchpath", watchpath);
		try {

			// check if the input path is a folder
			if (!wpath.isDirectory()) {
				model.addAttribute("error", "Watch path " + watchpath + " is not a valid folder");
			} else if (!dpath.isDirectory()) {
				model.addAttribute("error", "Done path " + donepath + " is not a valid folder");
			}
			// watch path and done path cannot be the same to avoid infinite looping
			else if (dpath.getCanonicalPath().equals(wpath.getCanonicalPath())) {
				model.addAttribute("error", "Done path cannot be the same as watch path");
			} else {
				watcher.setWatchPath(watchpath);
				importmanager.setDonePath(donepath);
				watcher.stop();
				watcher.start();
				model.addAttribute("message", "Import settings updated.");
			}
		} catch (IOException e) {
			model.addAttribute("error", "Error in setting the paths " + e.getMessage());
		}
		return settings(model);
	}

	/**
	 * List all imported website access records
	 * 
	 * @param pageNumber
	 *            Page to retrieve
	 * @param model
	 *            The model inherited
	 * @return The view to be used
	 */
	@RequestMapping(value = "all", method = RequestMethod.GET)
	public String list(@RequestParam(required = false, defaultValue = "0") int pageNumber, Model model) {
		Pageable pageable = new PageRequest(pageNumber, 10, Sort.Direction.ASC, "date", "website");
		Page<WebSiteAccess> page = websiterepository.findAll(pageable);
		model.addAttribute("websites", page.getContent());
		model.addAttribute("total", page.getTotalElements());
		model.addAttribute("last", page.isLast());
		model.addAttribute("pageNumber", page.getNumber());
		model.addAttribute("size", page.getSize());
		return "allresult";
	}

	/**
	 * List file import history with paging support
	 * 
	 * @param pageNumber
	 *            Page to retrieve
	 * @param model
	 *            The model inherited
	 * @return The view to be used
	 */
	@RequestMapping(value = "history", method = RequestMethod.GET)
	public String history(@RequestParam(required = false, defaultValue = "0") int pageNumber, Model model) {
		Pageable pageable = new PageRequest(pageNumber, 10, Sort.Direction.ASC, "date");
		Page<ImportHistory> page = historyrepository.findAll(pageable);
		model.addAttribute("history", page.getContent());
		model.addAttribute("total", page.getTotalElements());
		model.addAttribute("last", page.isLast());
		model.addAttribute("pageNumber", page.getNumber());
		model.addAttribute("size", page.getSize());
		return "history";
	}

	/**
	 * Get the exclusion list and the cache expiry time
	 * 
	 * @param pageNumber
	 *            Page to retrieve
	 * @param model
	 *            The model inherited
	 * @return The view to be used
	 */
	@RequestMapping(value = "exclusion", method = RequestMethod.GET)
	public String exclusion(@RequestParam(required = false, defaultValue = "0") int pageNumber, Model model) {
		// public List<FilterSite> exclusion(@RequestParam(required = false,
		// defaultValue = "0") int pageNumber, Model model) {

		List<FilterSite> l = filtersitemanager.getFilterSites();
		List<FilterSite> mcopy = new ArrayList<>(l.size());
		for (FilterSite f : l) {
			mcopy.add(new FilterSite(f.getHost(), f.getExcludedSince(), f.getExcludedTill()));
		}
		int startindex = pageNumber * 10 > mcopy.size() ? mcopy.size() : pageNumber * 10;
		int lastindex = (pageNumber + 1) * 10 > mcopy.size() ? mcopy.size() : (pageNumber + 1) * 10;

		l = mcopy.subList(startindex, lastindex);
		model.addAttribute("exclusion", l);
		model.addAttribute("total", mcopy.size());
		model.addAttribute("last", lastindex == mcopy.size());
		model.addAttribute("pageNumber", pageNumber);
		model.addAttribute("size", 10);
		model.addAttribute("cachetime", filtersitemanager.getExpireTime());
		return "exclusion";
	}

	/**
	 * Show the search status without any result
	 * 
	 * @param model
	 *            The model inherited
	 * @return The view to be used
	 */
	@RequestMapping(value = "topsites", method = RequestMethod.GET)
	public String form(Model model) {
		return "result";
	}

	/**
	 * Search for the top websites of a particular date. The results are filtered
	 * with the exclusion list
	 * 
	 * @param datestr
	 *            Date to query for
	 * @param count
	 *            Number of websites to return
	 * @param model
	 *            The model inherited
	 * @return The view to be used
	 */
	@RequestMapping(value = "topsites", method = RequestMethod.POST)
	public String result(@RequestParam String datestr, @RequestParam(required = false, defaultValue = "5") int count,
			Model model) {
		Date date;
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			date = format.parse(datestr);
			if (count <= 0)
				throw (new Exception());
			log.info("Getting resutls for {}", date);
			List<WebSiteAccess> result = getTop(date, count);
			log.info("{} records are found", result.size());
			model.addAttribute("websites", result);
			model.addAttribute("datestr", datestr);
			model.addAttribute("count", count);

		} catch (ParseException e) {
			model.addAttribute("errormessage", "Invalid date");
		} catch (Exception i) {
			model.addAttribute("errormessage", "Invalid count number");
		}
		return "result";
	}

	/**
	 * Function to do the actual retrieval of websites and filtering
	 * 
	 * @param date
	 *            Date to query for
	 * @param count
	 *            Number of websites to return
	 * @return The list of web sites in descending order of access count
	 */
	private List<WebSiteAccess> getTop(Date date, int count) {
		int page = 0;
		int totalrecs = 0;
		ArrayList<WebSiteAccess> topsites = new ArrayList<>(count);
		// Since we don't know how many websites will be filtered out, each time get
		// <tt>count</tt> records and filter them
		// until the number of records required are archieved
		while (topsites.size() < count) {
			Pageable pageable = new PageRequest(page, count, new Sort(Sort.Direction.DESC, "count"));
			List<WebSiteAccess> result = websiterepository.findByDate(date, pageable);
			if (result.isEmpty())
				break;
			for (WebSiteAccess w : result) {
				totalrecs++;
				if (!filtersitemanager.filteredSite(w.getWebsite(), date)) {
					topsites.add(w);
					if (topsites.size() >= count)
						break;
				}
			}
			page++;
		}
		log.info("{} records retrieved, {} filtered", totalrecs, totalrecs - topsites.size());
		return topsites;
	}

}
