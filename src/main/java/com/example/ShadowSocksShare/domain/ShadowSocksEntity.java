package com.example.ShadowSocksShare.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * ssr 信息
 */
@Slf4j
@Entity
// @Table(uniqueConstraints = @UniqueConstraint(columnNames = "targetURL"))    // 唯一约束
@Table(indexes = {@Index(columnList = "targetURL", unique = false)})           // 非唯一约束

@Getter
@Setter
@ToString
// @RequiredArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
public class ShadowSocksEntity implements Serializable {
	private static final long serialVersionUID = 8349835505366821722L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column
	// @NonNull
	private String targetURL;    // 目标网站 URL

	@Column
	// @NonNull
	private String title;    // 网站名

	@Column
	// @NonNull
	private boolean valid;    // 是否有效

	@Column
	@Temporal(TemporalType.TIMESTAMP)    // 爬取完成时间
	// @NonNull
	private Date finishTime;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)   // 级联保存、更新、删除、刷新;延迟加载
	@JoinColumn(name = "ss_id")                                        // 在 Details 表增加一个外键列来实现一对多的单向关联
	private Set<ShadowSocksDetailsEntity> shadowSocksSet;           // 一对多，网站 ShadowSocks 信息

	public ShadowSocksEntity(String targetURL, String title, boolean valid, Date finishTime) {
		this.targetURL = targetURL;
		this.title = title;
		this.valid = valid;
		this.finishTime = finishTime;
	}
}
