package com.example.ShadowSocksShare.service;

import com.example.ShadowSocksShare.BaseTest;
import com.example.ShadowSocksShare.domain.ShadowSocksEntity;
import com.example.ShadowSocksShare.service.impl.IShadowCrawlerServiceImpl;
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
	private ShadowSocksCrawlerService doubCrawlerServiceImpl;
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
	@Qualifier("free_ssCrawlerServiceImpl")
	private ShadowSocksCrawlerService free_ssCrawlerServiceImpl;
	@Autowired
	@Qualifier("ssrBlueCrawlerServiceImpl")
	private ShadowSocksCrawlerService ssrBlueCrawlerServiceImpl;
	@Autowired
	@Qualifier("free_yitianjianssCrawlerServiceImpl")
	private ShadowSocksCrawlerService free_yitianjianssCrawlerServiceImpl;
	@Autowired
	@Qualifier("promPHPCrawlerServiceImpl")
	private ShadowSocksCrawlerService promPHPCrawlerServiceImpl;
	@Autowired
	@Qualifier("iShadowCrawlerServiceImpl")
	private IShadowCrawlerServiceImpl iShadowCrawlerService;

	@Test
	public void parseURL() throws IOException, NotFoundException {
		String url = "https://free.yitianjianss.com/img/qrcode_image/293/c797e96ed6969ab4bc24726104fe12ea.png";
		free_yitianjianssCrawlerServiceImpl.parseURL(url);
	}

	// https://doub.io
	@Test
	public void testIShadowService() {
		ShadowSocksEntity entity = iShadowCrawlerService.getShadowSocks();
		log.debug("========>{}", entity);
	}

	// https://doub.io
	@Test
	public void testDoubCrawlerService() {
		ShadowSocksEntity entity = doubCrawlerServiceImpl.getShadowSocks();
		log.debug("========>{}", entity);
	}

	// https://free-ss.site/
	@Test
	public void testFree_ssCrawlerService() {
		ShadowSocksEntity entity = free_ssCrawlerServiceImpl.getShadowSocks();
		log.debug("========>{}", entity);
	}

	// https://free.yitianjianss.com/
	@Test
	public void testFree_yitianjianssCrawlerService() {
		ShadowSocksEntity entity = free_yitianjianssCrawlerServiceImpl.getShadowSocks();
		log.debug("========>{}", entity);
	}

	// https://www.52ssr.cn/
	@Test
	public void testSsrBlueCrawlerService() {
		ShadowSocksEntity entity = ssrBlueCrawlerServiceImpl.getShadowSocks();
		log.debug("========>{}", entity);
	}

	// https://prom-php.herokuapp.com/cloudfra_ssr.txt
	@Test
	public void testPromPHPCrawlerService() {
		ShadowSocksEntity entity = promPHPCrawlerServiceImpl.getShadowSocks();
		log.debug("========>{}", entity);
	}
}