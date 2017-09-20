package com.leonardyau.topwebsite;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import com.leonardyau.topwebsite.controller.TopWebSiteController;
import com.leonardyau.topwebsite.model.WebSiteAccess;
import com.leonardyau.topwebsite.repository.WebSiteRepository;

@RunWith(SpringRunner.class)
public class TopWebsiteApplicationTests {

	
	@Mock
	private WebSiteRepository repository;
	
	@InjectMocks
	private TopWebSiteController c;

	@Test
	public void testAdd() throws ParseException {
		c.add("www.hello.com", "2016-01-01", 12345);
		c.add("www.hello.com", "2016-01-02", 22);
		c.add("www.hello.com", "2016-01-02", 33);
		c.add("www.hello.com", "2016-01-03", 33333);
		c.add("facebook.com", "2016-01-03", 20160103);
		c.add("facebook.com", "2017-01-03", 20170103);
		c.add("www.google.com", "2017-01-03", 170103);
		c.add("google.com", "2017-01-03", 270103);
		c.add("abc.com", "2017-01-03", 123);
		c.add("def.com", "2017-01-03", 456);
		c.add("ghi.com", "2017-01-03", 789);
		c.add("facebook.com", "2017-01-03", 3331313);
		c.add("www.hello-world.com", "2016-01-03", 3);
		c.add("www.hello-world.com", "2016-01-03", 3);
		c.add("www.hello-world.com", "2016-01-03", 3);
		c.add("www.hello-world.com", "2016-01-03", 3);
		
	}

	
	
}
