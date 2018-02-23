package com.example.ShadowSocksShare.service;


import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.domain.ShadowSocksEntity;
import com.google.zxing.WriterException;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

/**
 * 1. 爬取 目标网站 SS 信息
 * 2. SS 信息入库
 * 3. 前台页面展示信息
 */
public interface ShadowSocksSerivce {
	/**
	 * 1. 爬取 SS 并入库
	 * 2. SS 信息入库
	 */
	void crawlerAndSave(ShadowSocksCrawlerService service);

	/**
	 * 3. 查询 SS 信息
	 */
	List<ShadowSocksEntity> findAll(Pageable pageable);

	/**
	 * 随机查询一条可用 ss 信息
	 */
	ShadowSocksDetailsEntity findFirstByRandom();

	/**
	 * 3. 生成 SSR 连接
	 */
	String toSSLink(List<ShadowSocksEntity> entities, boolean valid);

	/**
	 * SS 有效性检查，获取 SS 信息，判断端口有效性，并更新数据
	 */
	void checkValid();

	/**
	 * 生成二维码
	 */
	byte[] createQRCodeImage(String text, int width, int height) throws WriterException, IOException;
}
