package com.example.ShadowSocksShare.service.impl;

import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 免费shadowsocks账号
 * https://freessr.win/
 */
@Slf4j
@Service
public class FreeSSRCrawlerServiceImpl extends ShadowSocksCrawlerService {
	// 目标网站 URL
	private static final String TARGET_URL = "https://freessr.win/";
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
		Elements ssList = document.select("div.text-center");

		Set<ShadowSocksDetailsEntity> set = new HashSet<>(ssList.size());
		for (int i = 0; i < ssList.size(); i++) {
			try {
				Element element = ssList.get(i);
				// 取 h4 信息，为 ss 信息
				Elements ssHtml = element.select("h4");

				if (ssHtml.size() >= 5) {
					// server
					String server = StringUtils.remove(ssHtml.get(0).text(), "服务器地址:");
					Assert.hasLength(server, "server 不能为空");

					int server_port = NumberUtils.toInt(StringUtils.remove(ssHtml.get(1).text(), "端口:"));
					// Assert.isNull(port, "port 不能为空");


					String password = StringUtils.remove(ssHtml.get(2).text(), "密码:");
					Assert.hasLength(password, "password 不能为空");

					String method = StringUtils.remove(ssHtml.get(3).text(), "加密方式:");
					Assert.hasLength(method, "method 不能为空");

					// 账号状态
					String status = ssHtml.get(4).text();
					if (status.contains("正常")) {
						ShadowSocksDetailsEntity ss = new ShadowSocksDetailsEntity(server, server_port, password, method, SS_PROTOCOL, SS_OBFS);
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
					}
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
