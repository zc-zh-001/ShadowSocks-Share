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
 * iShadow
 * https://global.ishadowx.net/
 */
@Slf4j
@Service("iShadowCrawlerServiceImpl")
public class IShadowCrawlerServiceImpl extends ShadowSocksCrawlerService {
	// 目标网站 URL
	private static final String TARGET_URL = "https://my.ishadowx.net/";
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
		Elements ssList = document.select("div.hover-text");

		Set<ShadowSocksDetailsEntity> set = new HashSet<>(ssList.size());
		for (int i = 0; i < ssList.size(); i++) {
			try {
				Element element = ssList.get(i);
				// 取 h4 信息，为 ss 信息
				Elements ssHtml = element.select("h4");

				// log.debug(ssHtml.html());
				// 如果 得到 大于 5 个（address、port、password、method、agreement、obscure），分别取相应信息
				if (ssHtml.size() >= 5) {
					// server
					String server = ssHtml.get(0).select("span[id]").first().html();
					Assert.hasLength(server, "server 不能为空");

					int server_port = NumberUtils.toInt(ssHtml.get(1).select("span[id]").first().html());
					// Assert.isNull(port, "port 不能为空");

					String password = ssHtml.get(2).select("span[id]").first().html();
					Assert.hasLength(password, "password 不能为空");

					String method = StringUtils.substringAfter(ssHtml.get(3).text(), ":");
					Assert.hasLength(method, "method 不能为空");

					ShadowSocksDetailsEntity ss = new ShadowSocksDetailsEntity(server, server_port, password, method, SS_PROTOCOL, SS_OBFS);
					// 最后一行 可能是 ss 或 ssr 区别是 ss 是 点击获取图片，ssr 是 协议和混淆
					if (ssHtml.get(4).select("a") != null) {
						// ss
						// ss = new ShadowSocksDetailsEntity(server, server_port, password, method, SS_PROTOCOL, SS_OBFS);
					} else {
						// ssr
						String[] ao = StringUtils.split(ssHtml.get(4).text(), " ");
						if (ao.length == 2) {
							String protocol = ao[0];
							Assert.hasLength(method, "protocol 不能为空");

							String obfs = ao[1];
							Assert.hasLength(method, "obscure 不能为空");

							ss = new ShadowSocksDetailsEntity(server, server_port, password, method, protocol, obfs);
						}
					}

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
