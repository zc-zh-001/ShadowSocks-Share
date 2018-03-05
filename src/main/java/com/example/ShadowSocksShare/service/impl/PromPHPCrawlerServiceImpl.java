package com.example.ShadowSocksShare.service.impl;

import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.domain.ShadowSocksEntity;
import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * https://prom-php.herokuapp.com/cloudfra_ssr.txt
 */
@Slf4j
@Service
public class PromPHPCrawlerServiceImpl extends ShadowSocksCrawlerService {

	// 目标网站 URL
	private static final String TARGET_URL = "https://prom-php.herokuapp.com/cloudfra_ssr.txt";

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
	 * 爬取 ss 账号
	 */
	@Override
	public ShadowSocksEntity getShadowSocks() {
		try {
			Document document = getDocument();
			ShadowSocksEntity entity = new ShadowSocksEntity("https://cloudfra.com/", "免费账号 | 云端框架", true, new Date());
			entity.setShadowSocksSet(parse(document));
			return entity;
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return new ShadowSocksEntity(getTargetURL(), "", false, new Date());
	}

	// 解析 连接方式
	@Override
	protected Set<ShadowSocksDetailsEntity> parse(Document document) {

		// SSR 订阅地址内容
		String base64ssrLinks = document.text();
		String ssrLinks = StringUtils.toEncodedString(Base64.decodeBase64(base64ssrLinks), StandardCharsets.UTF_8);
		String[] ssrLinkList = ssrLinks.split("\n");

		// log.debug("---------------->{}={}", ssrLinkList.length + "", ssrLinkList);
		Set<ShadowSocksDetailsEntity> set = Collections.synchronizedSet(new HashSet<>(ssrLinkList.length));

		Arrays.asList(ssrLinkList).parallelStream().forEach((str) -> {
			try {
				if (StringUtils.isNotBlank(str)) {
					ShadowSocksDetailsEntity ss = parseLink(str.trim());
					ss.setValid(false);
					ss.setValidTime(new Date());
					ss.setTitle("免费账号 | 云端框架");
					ss.setRemarks("https://cloudfra.com/");
					ss.setGroup("ShadowSocks-Share");

					// 测试网络
					if (isReachable(ss))
						ss.setValid(true);

					// 无论是否可用都入库
					set.add(ss);

					log.debug("*************** 第 {} 条 ***************{}{}", set.size(), System.lineSeparator(), ss);
					// log.debug("{}", ss.getLink());
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		});

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
