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

import java.util.*;

/**
 * doub
 * https://doub.io
 */
@Slf4j
@Service
public class DoubCrawlerServiceImpl extends ShadowSocksCrawlerService {

	// 目标网站 URL
	private static final String TARGET_URL = "https://doub.io/sszhfx/";

	// 协议
	private final static Map<String, String> protocolMap = new HashMap<>();
	// 混淆
	private final static Map<String, String> obfsMap = new HashMap<>();

	static {
		protocolMap.put("1", "origin");
		protocolMap.put("2", "verify_deflate");
		protocolMap.put("3", "auth_sha1_v4");
		protocolMap.put("4", "auth_aes128_md5");
		protocolMap.put("5", "auth_aes128_sha1");
		protocolMap.put("6", "auth_chain_a");
		protocolMap.put("7", "auth_chain_b");

		obfsMap.put("1", "plain");
		obfsMap.put("2", "http_simple");
		obfsMap.put("3", "http_post");
		obfsMap.put("4", "random_head");
		obfsMap.put("5", "tls1.2_ticket_auth");
		obfsMap.put("6", "tls1.2_ticket_fastauth");
	}

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
		Elements tables = document.select("table");

		if (!tables.isEmpty()) {
			// 取第一个 table
			Element table = tables.get(0);
			// 所有行（首行为表头）
			Elements rows = table.select("tr");
			if (rows.size() > 2) {

				Set<ShadowSocksDetailsEntity> set = new HashSet<>(rows.size() - 1);
				// 从第二行读取
				for (int i = 1; i < rows.size(); i++) {
					Elements cols = rows.get(i).select("td");
					if (cols.size() >= 5) {
						// 服务器地址 / 服务器IP / 端口 / 协议|混淆 / 加密方式 / 提供者 / 二维码
						// 计算 协议|混淆
						String strs[] = StringUtils.deleteWhitespace(cols.get(3).text()).split("\\|");
						// 协议|混淆
						if (strs.length >= 2) {
							String protocol = protocolMap.get(strs[0].trim());
							String obfs = obfsMap.get(strs[1].trim());

							// IPV4、协议、混淆 不为空
							if (cols.get(1).text().contains(".") && StringUtils.isNotBlank(protocol) && StringUtils.isNotBlank(obfs)) {
								ShadowSocksDetailsEntity ss = new ShadowSocksDetailsEntity(cols.get(1).text(), Integer.parseInt(cols.get(2).text()), "doub.io/sszhfx/*doub.bid/sszhfx/*" + cols.get(2).text(), cols.get(4).text(), protocol, obfs);
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
						}
					}
				}
				return set;
			}
		}
		return new HashSet<>();
	}

	// 解析 连接方式
	/*@Override
	protected Set<ShadowSocksDetailsEntity> parse(Document document) {
		Elements ssList = document.select("a.dl1");

		Set<ShadowSocksDetailsEntity> set = new HashSet(ssList.size());
		for (int i = 0; i < ssList.size(); i++) {
			try {
				Element element = ssList.get(i);
				if (element.hasText() && element.text().equalsIgnoreCase("ssr")) {
					String ssrHtml = element.attributes().get("href");
					String ssrLink = ssrHtml.replace("http://doub.pw/qr/qr.php?text=", "");

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
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return set;
	}*/

	/**
	 * 目标网站 URL
	 */
	@Override
	protected String getTargetURL() {
		return TARGET_URL;
	}
}
