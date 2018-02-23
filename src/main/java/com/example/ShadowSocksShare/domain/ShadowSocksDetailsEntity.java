package com.example.ShadowSocksShare.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.apache.commons.codec.binary.Base64;

import javax.persistence.*;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ssr 信息
 * 参考：
 * https://www.zfl9.com/ssr.html
 * http://rt.cn2k.net/?p=328
 * https://vxblue.com/archives/115.html
 */
@Entity
@Getter
@Setter
@ToString
// @RequiredArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id", "remarks", "group"})
public class ShadowSocksDetailsEntity implements Serializable {
	// SSR 连接 分隔符
	public static final String SSR_LINK_SEPARATOR = ":";
	private static final long serialVersionUID = 952212276705742190L;

	// 必填字段
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Setter
	private long id;

	@Column
	// @NonNull
	private String server; // 地址

	@Column
	// @NonNull
	private int server_port;    // 端口

	@Column
	// @NonNull
	private String password;    // 密码

	@Column
	// @NonNull
	private String method;    // 加密

	@Column
	// @NonNull
	private String protocol;    // 协议

	@Column
	// @NonNull
	private String obfs;    // 混淆

	// 非必填
	@Column
	private String remarks;    // 备注

	@Column(name = "grp")
	private String group; // 组

	@Column
	private boolean valid;    // 是否有效

	@Column
	private Date validTime;        // 有效性验证时间

	@Column
	private String title;        // 网站名

	public ShadowSocksDetailsEntity(String server, int server_port, String password, String method, String protocol, String obfs) {
		this.server = server;
		this.server_port = server_port;
		this.password = password;
		this.method = method;
		this.protocol = protocol;
		this.obfs = obfs;
	}

	public String getJsonStr() throws JsonProcessingException {
		Map<String, Object> json = new HashMap<>();
		json.put("server", server);
		json.put("server_port", server_port);
		json.put("local_address", "127.0.0.1");
		json.put("local_port", 1080);
		json.put("password", password);
		json.put("group", group);
		json.put("obfs", obfs);
		json.put("method", method);
		json.put("ssr_protocol", protocol);
		return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(json);
	}


	/**
	 * 生成连接
	 * 连接规则：https://github.com/ssrbackup/shadowsocks-rss/wiki/SSR-QRcode-scheme
	 */
	public String getLink() {
		// ssr://base64(host:port:protocol:method:obfs:base64pass/?obfsparam=base64param&protoparam=base64param&remarks=base64remarks&group=base64group&udpport=0&uot=0)
		StringBuilder link = new StringBuilder();
		link
				.append(server)
				.append(SSR_LINK_SEPARATOR).append(server_port)
				.append(SSR_LINK_SEPARATOR).append(protocol)
				.append(SSR_LINK_SEPARATOR).append(method)
				.append(SSR_LINK_SEPARATOR).append(obfs)
				.append(SSR_LINK_SEPARATOR).append(Base64.encodeBase64URLSafeString(password.getBytes(StandardCharsets.UTF_8)))
				.append("/?obfsparam=")
				// .append("&protoparam=")
				.append("&remarks=").append(Base64.encodeBase64URLSafeString(remarks.getBytes(StandardCharsets.UTF_8)))
				.append("&group=").append(Base64.encodeBase64URLSafeString(group.getBytes(StandardCharsets.UTF_8)));
		return "ssr://" + Base64.encodeBase64URLSafeString(link.toString().getBytes(StandardCharsets.UTF_8));
	}

	/*public String getLinkNotSafe() {
		// 104.236.187.174:1118:auth_sha1_v4:chacha20:tls1.2_ticket_auth:ZGFzamtqZGFr/?obfsparam=&remarks=MTExOCDml6fph5HlsbEgMTDkurogMTAwRyBTU1I&group=Q2hhcmxlcyBYdQ
		StringBuilder link = new StringBuilder();
		link
				.append(server)
				.append(SSR_LINK_SEPARATOR).append(server_port)
				.append(SSR_LINK_SEPARATOR).append(protocol)
				.append(SSR_LINK_SEPARATOR).append(method)
				.append(SSR_LINK_SEPARATOR).append(obfs)
				.append(SSR_LINK_SEPARATOR).append(Base64.encodeBase64URLSafeString(password.getBytes(StandardCharsets.UTF_8)))
				.append("/?obfsparam=")
				// .append("&protoparam=")
				.append("&remarks=").append(Base64.encodeBase64URLSafeString(remarks.getBytes(StandardCharsets.UTF_8)))
				.append("&group=").append(Base64.encodeBase64URLSafeString(group.getBytes(StandardCharsets.UTF_8)));
		return "ssr://" + Base64.encodeBase64URLSafeString(link.toString().getBytes(StandardCharsets.UTF_8)) + " ";
	}*/
}
