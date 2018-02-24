package com.example.ShadowSocksShare.service.impl;

import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.domain.ShadowSocksEntity;
import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * https://free-ss.site/
 */
@Slf4j
@Service
public class Free_ssServiceImpl extends ShadowSocksCrawlerService {
	// 目标网站 URL
	private static final String TARGET_URL = "https://free-ss.site/";
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

	@Value("${proxy.free-ss.enable}")
	private boolean ssProxyEnable;
	@Value("${proxy.free-ss.host}")
	private String ssProxyHost;
	@Value("${proxy.free-ss.port}")
	private int ssProxyPort;
	@Value("${proxy.free-ss.socks}")
	private boolean ssSocks;

	public ShadowSocksEntity getShadowSocks() {
		try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
			// 设置代理
			if (ssProxyEnable)
				webClient.getOptions().setProxyConfig(new ProxyConfig(ssProxyHost, ssProxyPort, ssSocks));
			// 1. 爬取账号
			webClient.getOptions().setJavaScriptEnabled(true);                        // 启动 JS
			webClient.setJavaScriptTimeout(10 * 1000);                                // 设置 JS 执行的超时时间
			webClient.getOptions().setUseInsecureSSL(true);                            // 忽略 SSL 认证
			webClient.getOptions().setCssEnabled(false);                            // 禁用 CSS，可避免自动二次请求 CSS 进行渲染
			webClient.getOptions().setThrowExceptionOnScriptError(false);        //运行错误时，不抛出异常
			webClient.getOptions().setTimeout(TIME_OUT);                            // 连接超时时间。如果为 0，则无限期等待
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());    // 设置 Ajax 异步
			webClient.getCookieManager().setCookiesEnabled(true);                    // 开启 cookie 管理

			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);    // 忽略错误的 Http code

			// 模拟浏览器打开一个目标网址
			HtmlPage htmlPage = webClient.getPage(getTargetURL());
			webClient.waitForBackgroundJavaScript(6 * 1000); // 等待 JS 执行时间

			// 提交：1. 添加一个 submit；2. 添加到 form；3. 点击按钮
			DomElement button = htmlPage.createElement("button");
			button.setAttribute("type", "submit");

			final DomElement form = htmlPage.getElementById("challenge-form");
			form.appendChild(button);

			HtmlPage page = button.click();
			webClient.waitForBackgroundJavaScript(10 * 1000); // 等待 JS 执行时间

			// String ssListJson = page.getWebResponse().getContentAsString();
			// log.debug("========= > ssListJson:{}", page.asXml());

			// 2. 解析 json 生成 ShadowSocksDetailsEntity
			DomNodeList<DomNode> trList = page.querySelectorAll("table tbody tr");
			// log.debug("trList:{}", trList);

			Set<ShadowSocksDetailsEntity> set = new HashSet<>(trList.size());
			for (int i = 0; i < trList.size(); i++) {
				DomNode tr = trList.get(i);
				// log.debug("===============>{}", tr.asText());
				DomNodeList<DomNode> tdList = tr.querySelectorAll("td");
				if (tdList.size() > 4 && StringUtils.isNotBlank(tdList.get(1).asText()) && StringUtils.isNumeric(tdList.get(2).asText()) && StringUtils.isNotBlank(tdList.get(3).asText()) && StringUtils.isNotBlank(tdList.get(4).asText())) {
					ShadowSocksDetailsEntity ss = new ShadowSocksDetailsEntity(tdList.get(1).asText(), Integer.parseInt(tdList.get(2).asText()), tdList.get(3).asText(), tdList.get(4).asText(), SS_PROTOCOL, SS_OBFS);
					// 该网站账号默认为可用，不在此验证可用性
					ss.setValid(true);
					ss.setValidTime(new Date());
					ss.setTitle("免费上网账号");
					ss.setRemarks("https://free-ss.site/");
					ss.setGroup("ShadowSocks-Share");

					// 测试网络
					/*if (isReachable(ss))
						ss.setValid(true);*/

					// 无论是否可用都入库
					set.add(ss);

					log.debug("*************** 第 {} 条 ***************{}{}", i + 1, System.lineSeparator(), ss);
				}
			}

			// 3. 生成 ShadowSocksEntity
			ShadowSocksEntity entity = new ShadowSocksEntity("https://free-ss.site/", "免费上网账号", true, new Date());
			entity.setShadowSocksSet(set);
			return entity;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return new ShadowSocksEntity("https://free-ss.site/", "免费上网账号", false, new Date());
	}

	/**
	 * 网页内容解析 ss 信息
	 */
	@Override
	protected Set<ShadowSocksDetailsEntity> parse(Document document) {
		return null;
	}

	/**
	 * 目标网站 URL
	 */
	@Override
	protected String getTargetURL() {
		return MessageFormat.format(TARGET_URL, String.valueOf(System.currentTimeMillis()));
	}
}
