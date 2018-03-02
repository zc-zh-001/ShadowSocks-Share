package com.example.ShadowSocksShare.service.impl;

import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.domain.ShadowSocksEntity;
import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jsoup.nodes.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * https://free-ss.site/
 */
@Slf4j
@Service
public class Free_ssCrawlerServiceImpl extends ShadowSocksCrawlerService {
	// 目标网站 URL
	private static final String TARGET_URL = "https://free-ss.site/";
	// userAgent
	private static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36";
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

	@Value("${webDriver.phantomJSPath}")
	private String phantomJSPath;
	@Autowired
	private ResourceLoader resourceLoader;

	public ShadowSocksEntity getShadowSocks() {
		// 下载
		File phantomjsFile = new File(SystemUtils.getJavaIoTmpDir().getAbsolutePath(), "phantomjs");
		if (!phantomjsFile.exists()) {
			try {
				FileUtils.copyURLToFile(new URL("https://github.com/ariya/phantomjs/releases/download/2.1.3/phantomjs"), phantomjsFile);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		log.debug("File Path：{}", phantomjsFile.getAbsolutePath());


		// 设置必要参数
		DesiredCapabilities capability = DesiredCapabilities.chrome();
		// userAgent
		capability.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", userAgent);
		// SSL 证书支持
		capability.setCapability("acceptSslCerts", true);
		// 截屏支持
		// capability.setCapability("takesScreenshot", false);
		// CSS 搜索支持
		capability.setCapability("cssSelectorsEnabled", true);
		// JS 支持
		capability.setJavascriptEnabled(true);
		// 驱动支持
		capability.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsFile.getAbsolutePath()/*resourceLoader.getResource(phantomJSPath).getFile().getAbsolutePath()*/);

		// 设置代理
		if (ssProxyEnable) {
			String proxyServer = ssProxyHost + ":" + ssProxyPort;
			Proxy proxy = new Proxy();
			proxy.setAutodetect(true).setProxyType(Proxy.ProxyType.MANUAL);
			if (ssSocks) {
				proxy.setSocksProxy(proxyServer);
			} else {
				proxy.setHttpProxy(proxyServer).setFtpProxy(proxyServer).setSslProxy(proxyServer);
			}
			capability.setCapability(CapabilityType.PROXY, proxy);
		}


		// WebDriver driver = new RemoteWebDriver(new URL(TARGET_URL), capability);
		WebDriver driver = null;
		try {
			driver = new PhantomJSDriver(capability);
			driver.manage().timeouts().implicitlyWait(TIME_OUT, TimeUnit.SECONDS);
			driver.get(TARGET_URL);

			TimeUnit.SECONDS.sleep(5);

			if (waitForAjax(driver)) {
				List<WebElement> divList = driver.findElements(By.xpath("//div[contains(@class, 'dataTables_wrapper')]"));
				for (WebElement dev : divList) {
					// log.debug("height =================>{}", dev.getSize().height);
					// log.debug("isDisplayed =================>{}", dev.isDisplayed());
					// log.debug("DIV innerHTML =================>{}", dev.getAttribute("innerHTML"));

					if (dev.isDisplayed()) {
						List<WebElement> trList = dev.findElements(By.xpath("./table/tbody/tr"));

						Set<ShadowSocksDetailsEntity> set = new HashSet<>(trList.size());
						for (WebElement tr : trList) {
							// log.debug("TR innerHTML =================>{}", tr.getAttribute("innerHTML"));

							String server = tr.findElement(By.xpath("./td[2]")).getText();
							String server_port = tr.findElement(By.xpath("./td[3]")).getText();
							String password = tr.findElement(By.xpath("./td[4]")).getText();
							String method = tr.findElement(By.xpath("./td[5]")).getText();

							if (StringUtils.isNotBlank(server) && StringUtils.isNumeric(server_port) && StringUtils.isNotBlank(password) && StringUtils.isNotBlank(method)) {
								ShadowSocksDetailsEntity ss = new ShadowSocksDetailsEntity(server, Integer.parseInt(server_port), password, method, SS_PROTOCOL, SS_OBFS);
								// 该网站账号默认为可用，不在此验证可用性
								ss.setValid(true);
								ss.setValidTime(new Date());
								ss.setTitle("免费上网账号");
								ss.setRemarks("https://free-ss.site/");
								ss.setGroup("ShadowSocks-Share");

								// 测试网络
								if (isReachable(ss))
									ss.setValid(true);

								// 无论是否可用都入库
								set.add(ss);

								log.debug("*************** 第 {} 条 ***************{}{}", set.size(), System.lineSeparator(), ss);
							}
						}

						// 3. 生成 ShadowSocksEntity
						ShadowSocksEntity entity = new ShadowSocksEntity(TARGET_URL, driver.getTitle(), true, new Date());
						entity.setShadowSocksSet(set);
						return entity;
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (driver != null)
				driver.close();
		}
		return new ShadowSocksEntity(TARGET_URL, "免费上网账号", false, new Date());
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

	public boolean waitForAjax(WebDriver driver) {
		WebDriverWait wait = new WebDriverWait(driver, 30, 500);
		ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					return ((Long) ((JavascriptExecutor) driver).executeScript("return jQuery.active") == 0);
				} catch (Exception e) {
					return true;
				}
			}
		};
		ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState")
						.toString().equals("complete");
			}
		};
		return wait.until(jQueryLoad) && wait.until(jsLoad);
	}
}
