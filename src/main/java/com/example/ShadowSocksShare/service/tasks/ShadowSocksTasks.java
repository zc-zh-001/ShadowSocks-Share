package com.example.ShadowSocksShare.service.tasks;

import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;
import com.example.ShadowSocksShare.service.ShadowSocksSerivce;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 定时抓取 ss 信息
 */
@Slf4j
@Component
public class ShadowSocksTasks {
	@Autowired
	private ShadowSocksSerivce shadowSocksSerivce;

	/*@Value("${health.url}")
	private String healthURL;*/

	@Autowired
	@Qualifier("iShadowCrawlerServiceImpl")
	private ShadowSocksCrawlerService iShadowCrawlerServiceImpl;
	@Autowired
	@Qualifier("doubCrawlerServiceImpl")
	private ShadowSocksCrawlerService doubCrawlerServiceImpl;
	@Autowired
	@Qualifier("freeSS_EasyToUseCrawlerServiceImpl")
	private ShadowSocksCrawlerService freeSS_EasyToUseCrawlerServiceImpl;
	@Autowired
	@Qualifier("ss8ServiceImpl")
	private ShadowSocksCrawlerService ss8ServiceImpl;
	@Autowired
	@Qualifier("freeSSRCrawlerServiceImpl")
	private ShadowSocksCrawlerService freeSSRCrawlerServiceImpl;
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


	// https://global.ishadowx.net/
	@Scheduled(cron = "0 10 0/1 * * ?")
	public void iShadowCrawler() {
		shadowSocksSerivce.crawlerAndSave(iShadowCrawlerServiceImpl);
	}

	// https://doub.io
	@Scheduled(cron = "0 10 0/3 * * ?")
	public void doubCrawler() {
		shadowSocksSerivce.crawlerAndSave(doubCrawlerServiceImpl);
	}

	// https://freess.cx/#portfolio-preview
	@Scheduled(cron = "0 10 0/3 * * ?")
	public void freeSS_EasyToUseCrawler() {
		shadowSocksSerivce.crawlerAndSave(freeSS_EasyToUseCrawlerServiceImpl);
	}

	// https://en.ss8.fun/
	@Scheduled(cron = "0 10 0/2 * * ?")
	public void ss8Crawler() {
		shadowSocksSerivce.crawlerAndSave(ss8ServiceImpl);
	}

	// https://freessr.win/
	@Scheduled(cron = "0 10 0/3 * * ?")
	public void freeSSRCrawler() {
		shadowSocksSerivce.crawlerAndSave(freeSSRCrawlerServiceImpl);
	}

	// https://free-ss.site/
	@Scheduled(cron = "0 10 0/1 * * ?")
	public void free_ssCrawler() {
		shadowSocksSerivce.crawlerAndSave(free_ssCrawlerServiceImpl);
	}

	// https://www.52ssr.cn/
	@Scheduled(cron = "0 10 0/1 * * ?")
	public void ssrBlueCrawler() {
		shadowSocksSerivce.crawlerAndSave(ssrBlueCrawlerServiceImpl);
	}

	// https://free.yitianjianss.com/
	@Scheduled(cron = "0 10 0/1 * * ?")
	public void free_yitianjianssCrawler() {
		shadowSocksSerivce.crawlerAndSave(free_yitianjianssCrawlerServiceImpl);
	}

	// https://prom-php.herokuapp.com/cloudfra_ssr.txt
	@Scheduled(cron = "0 10 0/1 * * ?")
	public void promPHPCrawler() {
		shadowSocksSerivce.crawlerAndSave(promPHPCrawlerServiceImpl);
	}

	/**
	 * SS 有效性检查，每 1 小时
	 */
	/*@Scheduled(cron = "0 0 0/1 * * ?")
	public void checkValid() {
		shadowSocksSerivce.checkValid();
	}*/

	/**
	 * 为防止 herokuapp 休眠，每 10 分钟访问一次
	 */
	/*@Scheduled(cron = "${health.cron}")
	public void monitor() throws IOException {
		if (StringUtils.isNotBlank(healthURL))
			Jsoup.connect(healthURL).get();
	}*/
}
