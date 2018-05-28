package com.example.ShadowSocksShare.service.impl;

import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * iShadow
 * https://global.ishadowx.net/
 */
@Slf4j
@Service
public class WuwRedCrawlerServiceImpl extends ShadowSocksCrawlerService {
	// 目标网站 URL
	private static final String TARGET_URL = "http://i.wuw.red/";
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
		Elements tableList = document.select("div.elementor-tabs-content-wrapper table");

		Set<ShadowSocksDetailsEntity> set = new HashSet<>();
		for (int i = 0; i < tableList.size(); i++) {
			try {
				Element table = tableList.get(i);
				// 取 h4 信息，为 ss 信息
				Elements ssHtml = table.select("tr");

				if (StringUtils.equalsAnyIgnoreCase("shadowsocksR", table.select("tr td").first().text())) {
					String server = ssHtml.get(1).select("td").get(1).text();
					int server_port = 5240;
					String password = ssHtml.get(3).select("td").get(1).text();
					String method = ssHtml.get(4).select("td").get(1).text();

					String obfs = ssHtml.get(5).select("td").get(1).text();
					String protocol = ssHtml.get(6).select("td").get(1).text();
					// log.debug("---------------->{}={}={}={}={}={}", server, server_port, password, method, obfs, protocol);

					ShadowSocksDetailsEntity ss = new ShadowSocksDetailsEntity(server, server_port, password, method, protocol, obfs);
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

					log.debug("*************** 第 {} 条 ***************{}{}", set.size(), System.lineSeparator(), ss);
				}

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
