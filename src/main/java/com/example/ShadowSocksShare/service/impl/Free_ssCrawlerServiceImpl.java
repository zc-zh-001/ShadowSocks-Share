package com.example.ShadowSocksShare.service.impl;

import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.domain.ShadowSocksEntity;
import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

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
@Profile("prod")
@Service("free_ssCrawlerServiceImpl")
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

	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private Environment env;

	public ShadowSocksEntity getShadowSocks() {
		WebDriver driver = null;
		try {
			/*System.setProperty("webdriver.chrome.logfile", "D:\\chromedriver.log");
			System.setProperty("webdriver.chrome.verboseLogging", "true");*/

			ChromeDriverService service = null;

			if (SystemUtils.IS_OS_WINDOWS) {
				service = new ChromeDriverService.Builder().usingAnyFreePort()
						.usingDriverExecutable(resourceLoader.getResource("classpath:lib/chromedriver.exe").getFile())
						.build();
			} else {
				service = new ChromeDriverService.Builder().usingAnyFreePort().build();
			}

			ChromeOptions options = new ChromeOptions();

			if (SystemUtils.IS_OS_WINDOWS) {
				options.setBinary("D:\\software\\CentBrowser\\chrome.exe");
				// options.addArguments("user-data-dir=D:\\software\\CentBrowser\\User Data");
			} else {
				options.setBinary("/app/.apt/usr/bin/google-chrome");
				options.addArguments("--headless");
				options.addArguments("--disable-gpu");
				options.addArguments("--no-sandbox");
				// options.addArguments("--remote-debugging-port=9222");
			}

			if (ssProxyEnable) {
				String proxyServer = ssProxyHost + ":" + ssProxyPort;
				Proxy proxy = new Proxy();
				// proxy.setAutodetect(true).setProxyType(Proxy.ProxyType.MANUAL);
				if (ssSocks) {
					proxy.setSocksProxy(proxyServer);
				} else {
					proxy.setHttpProxy(proxyServer);
				}
				options.setCapability(CapabilityType.PROXY, proxy);
			}

			driver = new ChromeDriver(service, options);
			driver.manage().timeouts().implicitlyWait(TIME_OUT, TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(TIME_OUT, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(TIME_OUT, TimeUnit.SECONDS);

			driver.get(TARGET_URL);

			TimeUnit.SECONDS.sleep(15);

			if (true) {
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
			if (driver != null) {
				driver.quit();
				// driver.close();
			}
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
}
