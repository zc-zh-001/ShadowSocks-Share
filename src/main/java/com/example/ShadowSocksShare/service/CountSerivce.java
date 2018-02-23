package com.example.ShadowSocksShare.service;

import org.springframework.stereotype.Service;

import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 网站计数器
 */
@Service
public class CountSerivce {
	// 记录网站请求量
	private AtomicInteger atomicInteger = new AtomicInteger(0);

	/**
	 * 累计增加
	 */
	public int incrementAndGet() {
		return atomicInteger.incrementAndGet();
	}

	/**
	 * 获取计数
	 */
	public int get() {
		return atomicInteger.get();
	}
}
