package com.example.ShadowSocksShare.domain;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * 数据库操作
 */
@Repository
public interface ShadowSocksDetailsRepository extends JpaRepository<ShadowSocksDetailsEntity, Long> {

	/**
	 * 随机查询一条可用 ss 信息
	 */
	@Query(value = "SELECT * FROM SHADOW_SOCKS_DETAILS_ENTITY where valid = true ORDER BY RAND() limit 1", nativeQuery = true)
	ShadowSocksDetailsEntity findFirstByValidOrderByRandomAsc();

	/**
	 * <= validTime
	 */
	Page<ShadowSocksDetailsEntity> findByValidTimeLessThanEqual(Date validTime, Pageable pageable);
}
