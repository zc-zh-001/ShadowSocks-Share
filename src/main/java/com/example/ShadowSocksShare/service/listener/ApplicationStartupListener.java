package com.example.ShadowSocksShare.service.listener;

import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;
import com.example.ShadowSocksShare.service.ShadowSocksSerivce;
import com.example.ShadowSocksShare.service.impl.Free_ssCrawlerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 系统启动 监听事件
 */
@Slf4j
@Component
// @Profile("prod")
public class ApplicationStartupListener {
	@Autowired
	private ShadowSocksSerivce shadowSocksSerivce;
	@Autowired
	private Set<ShadowSocksCrawlerService> crawlerSet;


	/**
	 * 系统启动 监听事件
	 */
	@Async
	@EventListener
	public void handleOrderStateChange(ContextRefreshedEvent contextRefreshedEvent) {
		try {
			crawlerSet.parallelStream().filter((service) -> (service instanceof Free_ssCrawlerServiceImpl)).forEach((service) -> shadowSocksSerivce.crawlerAndSave(service));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
