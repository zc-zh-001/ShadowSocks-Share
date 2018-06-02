package com.example.ShadowSocksShare.service;

import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.domain.ShadowSocksEntity;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * SSR 爬虫 抽象类
 * 本类只负责 爬取 SS 信息，入库操作由 ShadowSocksSerivce 处理
 * 1. 请求 目标地址 获取 Document
 * 2. 构造 ShadowSocksEntity 对象
 * 3. 解析 Document 构造 ShadowSocksSet
 */
@Slf4j
public abstract class ShadowSocksCrawlerService {
	// SS 的协议和混淆
	protected static final String SS_PROTOCOL = "origin";
	protected static final String SS_OBFS = "plain";
	// 目标网站请求超时时间（60 秒）
	protected static final int TIME_OUT = 60 * 1000;
	// 测试网络超时时间（3 秒）
	protected static final int SOCKET_TIME_OUT = 3 * 1000;

	/**
	 * 网络连通性测试
	 */
	public static boolean isReachable(ShadowSocksDetailsEntity ss) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(ss.getServer(), ss.getServer_port()), SOCKET_TIME_OUT);
			return true;
		} catch (IOException e) {
			// log.error(e.getMessage(), e);
		}
		return false;
	}


	/**
	 * 请求目标 URL 获取 Document
	 */
	protected Document getDocument() throws IOException {
		Document document;
		try {
			document = getConnection(getTargetURL()).get();
		} catch (IOException e) {
			throw new IOException("请求[" + getTargetURL() + "]异常：" + e.getMessage(), e);
		}
		return document;
	}

	protected Connection getConnection(String url) {
		@SuppressWarnings("deprecation")
		Connection connection = Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36")
				// .referrer("https://www.google.com/")
				.ignoreContentType(true)
				.followRedirects(true)
				.ignoreHttpErrors(true)
				.validateTLSCertificates(false)
				.timeout(TIME_OUT);
		if (isProxyEnable())
			connection.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getProxyHost(), getProxyPort())));
		return connection;
	}

	/**
	 * 爬取 ss 账号
	 */
	public ShadowSocksEntity getShadowSocks() {
		try {
			Document document = getDocument();
			ShadowSocksEntity entity = new ShadowSocksEntity(getTargetURL(), document.title(), true, new Date());
			entity.setShadowSocksSet(parse(document));
			return entity;
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return new ShadowSocksEntity(getTargetURL(), "", false, new Date());
	}

	/**
	 * 连接解析
	 */
	protected ShadowSocksDetailsEntity parseLink(String link) {
		// 分为 SSR 或 SS
		if (StringUtils.isNotBlank(link) && StringUtils.startsWithIgnoreCase(link, "ssr")) {
			String ssrInfoStr = new String(Base64.decodeBase64(StringUtils.remove(link, "ssr://").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
			try {
				// 按照 /? 拆分，前半段为 主要配置信息，后半段为 URL 参数
				String[] strs = StringUtils.split(ssrInfoStr, "/?");

				// ssr://server:port:protocol:method:obfs:password_base64/?suffix_base64
				// obfsparam=obfsparam_base64&protoparam=protoparam_base64&remarks=remarks_base64&group=group_base64

				String[] ssInfo = StringUtils.split(strs[0], ":", 6);

				ShadowSocksDetailsEntity entity = new ShadowSocksDetailsEntity(ssInfo[0].trim(), Integer.parseInt(ssInfo[1].trim()), new String(Base64.decodeBase64(ssInfo[5].trim().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8), ssInfo[3].trim(), ssInfo[2].trim(), ssInfo[4].trim());

				/*String suffix_base64 = strs[1];
				byte[] _remarks = Base64.decodeBase64(StringUtils.substringBetween(suffix_base64, "remarks=", "&"));
				if (_remarks != null && _remarks.length > 0)
					this.remarks = new String(_remarks);

				byte[] _group = Base64.decodeBase64(StringUtils.substringBetween(suffix_base64, "group="));
				if (_group != null && _group.length > 0)
					this.group = new String(_group);*/
				return entity;
			} catch (Exception e) {
				throw new RuntimeException("SSR 连接[" + ssrInfoStr + "]解析异常：" + e.getMessage(), e);
			}
		} else if (StringUtils.isNotBlank(link) && StringUtils.startsWithIgnoreCase(link, "ss")) {
			// aes-256-cfb:60948959@jp01.fss.fun:15785
			String ssInfoStr = new String(Base64.decodeBase64(StringUtils.remove(link, "ss://").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
			String[] strs = StringUtils.split(ssInfoStr, "@");
			String[] ssInfo1 = StringUtils.split(strs[0], ":");
			String[] ssInfo2 = StringUtils.split(strs[1], ":");

			ShadowSocksDetailsEntity entity = new ShadowSocksDetailsEntity(ssInfo2[0].trim(), Integer.parseInt(ssInfo2[1].trim()), ssInfo1[1].trim(), ssInfo1[0].trim(), "origin", "plain");
			return entity;
		} else {
			throw new IllegalArgumentException("SSR 连接[" + link + "]解析异常：协议类型错误");
		}
	}

	/**
	 * 图片解析
	 */
	protected ShadowSocksDetailsEntity parseURL(String imgURL) throws IOException, NotFoundException {
		Connection.Response resultImageResponse = getConnection(imgURL).execute();

		Map<DecodeHintType, Object> hints = new LinkedHashMap<>();
		// 解码设置编码方式为：utf-8，
		hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
		//优化精度
		hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		//复杂模式，开启PURE_BARCODE模式
		hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

		try (BufferedInputStream bytes = resultImageResponse.bodyStream()) {
			BufferedImage image = ImageIO.read(bytes);
			Binarizer binarizer = new HybridBinarizer(new BufferedImageLuminanceSource(image));
			BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
			Result res = new MultiFormatReader().decode(binaryBitmap, hints);
			return parseLink(res.toString());
		}
	}

	/**
	 * 图片解析
	 */
	protected ShadowSocksDetailsEntity parseImg(String imgURL) throws IOException, NotFoundException {
		String str = StringUtils.removeFirst(imgURL, "data:image/png;base64,");

		Map<DecodeHintType, Object> hints = new LinkedHashMap<>();
		// 解码设置编码方式为：utf-8，
		hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
		//优化精度
		hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		//复杂模式，开启PURE_BARCODE模式
		hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

		try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(str))) {
			BufferedImage image = ImageIO.read(bis);
			Binarizer binarizer = new HybridBinarizer(new BufferedImageLuminanceSource(image));
			BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
			Result res = new MultiFormatReader().decode(binaryBitmap, hints);
			return parseLink(res.toString());
		}
	}

	/**
	 * 网页内容解析 ss 信息
	 */
	protected abstract Set<ShadowSocksDetailsEntity> parse(Document document);

	/**
	 * 目标网站 URL
	 */
	protected abstract String getTargetURL();

	/**
	 * 访问目标网站，是否启动代理
	 */
	protected abstract boolean isProxyEnable();

	/**
	 * 代理地址
	 */
	protected abstract String getProxyHost();

	/**
	 * 代理端口
	 */
	protected abstract int getProxyPort();
}
