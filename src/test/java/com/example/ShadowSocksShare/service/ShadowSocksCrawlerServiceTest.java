package com.example.ShadowSocksShare.service;

import com.example.ShadowSocksShare.BaseTest;
import com.example.ShadowSocksShare.domain.ShadowSocksEntity;
import com.google.zxing.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;

@Slf4j
public class ShadowSocksCrawlerServiceTest extends BaseTest {
	@Autowired
	@Qualifier("iShadowCrawlerServiceImpl")
	private ShadowSocksCrawlerService iShadowCrawlerServiceImpl;    // ishadow
	@Autowired
	@Qualifier("doubCrawlerServiceImpl")
	private ShadowSocksCrawlerService doubCrawlerServiceImpl;                // https://doub.io
	@Autowired
	@Qualifier("freeSS_EasyToUseCrawlerServiceImpl")
	private ShadowSocksCrawlerService freeSS_EasyToUseCrawlerServiceImpl;                // https://freess.cx/#portfolio-preview
	@Autowired
	@Qualifier("ss8ServiceImpl")
	private ShadowSocksCrawlerService ss8ServiceImpl;                // https://en.ss8.fun/
	@Autowired
	@Qualifier("freeSSRCrawlerServiceImpl")
	private ShadowSocksCrawlerService freeSSRCrawlerServiceImpl;                // https://global.ishadowx.net/
	@Autowired
	@Qualifier("free_ssServiceImpl")
	private ShadowSocksCrawlerService free_ssServiceImpl;                // https://free-ss.site/
	@Autowired
	@Qualifier("ssrBlueCrawlerServiceImpl")
	private ShadowSocksCrawlerService ssrBlueCrawlerServiceImpl;                // http://www.ssr.blue
	@Autowired
	@Qualifier("free_yitianjianssCrawlerServiceImpl")
	private ShadowSocksCrawlerService free_yitianjianssCrawlerServiceImpl;                // http://www.ssr.blue
	@Autowired
	@Qualifier("promPHPCrawlerServiceImpl")
	private ShadowSocksCrawlerService promPHPCrawlerServiceImpl;                // https://prom-php.herokuapp.com/cloudfra_ssr.txt

	@Test
	public void parseURL() throws IOException, NotFoundException {
		String url = "https://free.yitianjianss.com/img/qrcode_image/293/c797e96ed6969ab4bc24726104fe12ea.png";
		free_yitianjianssCrawlerServiceImpl.parseURL(url);
	}


	@Test
	public void testDoubCrawlerService() {
		ShadowSocksEntity entity = doubCrawlerServiceImpl.getShadowSocks();
		log.debug("========>{}", entity);
	}

	@Test
	public void testFree_ssService() {
		ShadowSocksEntity entity = free_ssServiceImpl.getShadowSocks();
		log.debug("========>{}", entity);
	}

	@Test
	public void testFree_yitianjianssCrawlerService() {
		ShadowSocksEntity entity = free_yitianjianssCrawlerServiceImpl.getShadowSocks();
		log.debug("========>{}", entity);
	}

	@Test
	public void testPromPHPCrawlerService() {
		ShadowSocksEntity entity = promPHPCrawlerServiceImpl.getShadowSocks();
		log.debug("========>{}", entity);
	}
}