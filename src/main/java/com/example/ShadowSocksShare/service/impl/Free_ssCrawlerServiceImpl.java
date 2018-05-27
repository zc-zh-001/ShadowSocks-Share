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
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * https://free-ss.site/
 */
@Slf4j
@Service("free_ssCrawlerServiceImpl")
public class Free_ssCrawlerServiceImpl extends ShadowSocksCrawlerService {
	// 目标网站 URL
	private static final String TARGET_URL = "https://free-ss.site/";
	// userAgent
	// private static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36";
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
	private DriverService driverService;

	public ShadowSocksEntity getShadowSocks() {
		WebDriver driver = null;
		try {
			driver = driverService.getDriver();

			driver.manage().window().maximize();
			driver.manage().timeouts().implicitlyWait(TIME_OUT, TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(TIME_OUT, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(TIME_OUT, TimeUnit.SECONDS);

			driver.get(TARGET_URL);

			TimeUnit.SECONDS.sleep(5);

			if (waitForAjax(driver)) {

				TimeUnit.SECONDS.sleep(3);

				List<WebElement> divList = driver.findElements(By.xpath("//div[contains(@class, 'dataTables_wrapper')]"));
				for (WebElement dev : divList) {
					// log.debug("id =================>{}", dev.getAttribute("id"));
					// log.debug("height =================>{}", dev.getSize().height);
					// log.debug("isDisplayed =================>{}", dev.isDisplayed());
					// log.debug("DIV innerHTML =================>{}", dev.getAttribute("innerHTML"));

					if (dev.getSize().height > 100) {
						List<WebElement> trList = dev.findElements(By.xpath("./table/tbody/tr"));

						Set<ShadowSocksDetailsEntity> set = Collections.synchronizedSet(new HashSet<>(trList.size()));

						trList.parallelStream().forEach((tr) -> {
							// log.debug("TR innerHTML =================>{}", tr.getAttribute("innerHTML"));
							try {
								String server = tr.findElement(By.xpath("./td[2]")).getText();
								String server_port = tr.findElement(By.xpath("./td[3]")).getText();
								String method = tr.findElement(By.xpath("./td[4]")).getText();
								String password = tr.findElement(By.xpath("./td[5]")).getText();

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
							} catch (StaleElementReferenceException e) {
								log.error(e.getMessage(), e);
							}
						});

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
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete");
			}
		};
		return wait.until(jQueryLoad) && wait.until(jsLoad);
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
		return TARGET_URL;
	}

	interface DriverService {
		WebDriver getDriver() throws IOException;
	}

	@Profile("dev")
	@Service("driverService")
	class EdgeDriverService implements DriverService {
		@Value("${driver.path}")
		private String driverPath;

		@Override
		public WebDriver getDriver() {
			System.setProperty("webdriver.edge.driver", driverPath);
			// System.setProperty("webdriver.ie.driver.loglevel", "DEBUG");

			org.openqa.selenium.edge.EdgeDriverService service = org.openqa.selenium.edge.EdgeDriverService.createDefaultService();

			EdgeOptions options = new EdgeOptions();
			return new EdgeDriver(service, options);
		}
	}

	@Profile("prod")
	@Service("driverService")
	class ChromeDriverService implements DriverService {
		@Override
		public WebDriver getDriver() throws IOException {
			/*System.setProperty("webdriver.chrome.logfile", "D:\\chromedriver.log");
			System.setProperty("webdriver.chrome.verboseLogging", "true");*/

			org.openqa.selenium.chrome.ChromeDriverService service;

			if (SystemUtils.IS_OS_WINDOWS) {
				service = new org.openqa.selenium.chrome.ChromeDriverService.Builder().usingAnyFreePort()
						.usingDriverExecutable(new File("D:\\chromedriver.exe"))
						.build();
			} else {
				service = new org.openqa.selenium.chrome.ChromeDriverService.Builder().usingAnyFreePort().build();
			}

			ChromeOptions options = new ChromeOptions();

			if (SystemUtils.IS_OS_WINDOWS) {
				options.setBinary("D:\\software\\CentBrowser\\chrome.exe");
				// options.addArguments("user-data-dir=D:\\software\\CentBrowser\\User Data");
				options.addArguments("--headless");
				options.addArguments("window-size=1200x600");
				options.addArguments("--disable-gpu");
			} else {
				String binaryPath = EnvironmentUtils.getProcEnvironment().get("GOOGLE_CHROME_SHIM");
				log.debug("GOOGLE_CHROME_SHIM : {}", binaryPath);
				log.debug("GOOGLE_CHROME_BIN : {}", EnvironmentUtils.getProcEnvironment().get("GOOGLE_CHROME_BIN"));
				options.setBinary(binaryPath);
				options.addArguments("--headless");
				options.addArguments("window-size=1200x600");
				options.addArguments("--disable-gpu");
				// options.addArguments("--no-sandbox");
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

			return new ChromeDriver(service, options);
		}
	}
}
