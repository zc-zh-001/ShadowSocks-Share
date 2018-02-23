package com.example.ShadowSocksShare.web;

import com.example.ShadowSocksShare.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@Slf4j
public class MainControllerTest extends BaseTest {
	// final Logger logger = LoggerFactory.getLogger(SampleControllerTest.class);

	@Autowired
	private MockMvc mvc;

	@Test
	public void home() throws Exception {
		String uri = "/";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON)).andReturn();
		int status = mvcResult.getResponse().getStatus();
		String content = mvcResult.getResponse().getContentAsString();
		log.debug("---------------->{}", content);
		Assert.assertTrue("错误，正确的返回值为200", status == 200);
		// Assert.assertFalse("数据不一致", !user.toString().equals(content));
	}

	@Test
	public void count() throws Exception {
		String uri = "/count";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)).andReturn();
		int status = mvcResult.getResponse().getStatus();
		String content = mvcResult.getResponse().getContentAsString();
		log.debug("---------------->{}", content);
		Assert.assertTrue("错误，正确的返回值为200", status == 200);
		// Assert.assertFalse("数据不一致", !user.toString().equals(content));
	}
}