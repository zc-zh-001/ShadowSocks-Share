package com.example.ShadowSocksShare.service.impl;

import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * https://www.52ssr.cn/
 */
@Slf4j
@Service
public class SsrBlueCrawlerServiceImpl extends ShadowSocksCrawlerService {
	// 目标网站 URL
	private static final String TARGET_URL = "https://www.52ssr.cn/";
	// 访问目标网站，是否启动代理
	@Value("${proxy.enable}")
	@Getter
	private boolean proxyEnable;
	// 代理地址
	@Getter
	@Value("${proxy.host}")
	private String proxyHost;
	// 代理端口
	@Getter
	@Value("${proxy.port}")
	private int proxyPort;

	/**
	 * 网页内容解析 ss 信息
	 */
	@Override
	protected Set<ShadowSocksDetailsEntity> parse(Document document) {
		Elements ssList = document.select("a.btn-info");

		Set<ShadowSocksDetailsEntity> set = new HashSet<>(ssList.size());
		for (int i = 0; i < ssList.size(); i++) {
			try {
				String ssrLink = ssList.get(i).attr("href");
				ShadowSocksDetailsEntity ss = parseLink(ssrLink);
				ss.setValid(false);
				ss.setValidTime(new Date());
				ss.setTitle(document.title());
				ss.setRemarks(TARGET_URL);
				ss.setGroup("ShadowSocks-Share");

				// 测试网络
				if (isReachable(ss))
					ss.setValid(true);

				// 无论是否可用都入库
				set.add(ss);

				log.debug("*************** 第 {} 条 ***************{}{}", i + 1, System.lineSeparator(), ss);
				// log.debug("{}", ss.getLink());

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return set;
	}

	/**
	 * 目标网站 URL
	 */
	@Override
	protected String getTargetURL() {
		return TARGET_URL;
	}
}
