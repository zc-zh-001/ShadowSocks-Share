package com.example.ShadowSocksShare.domain;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 数据库操作
 */
@Repository
public interface ShadowSocksRepository extends JpaRepository<ShadowSocksEntity, Long> {

	/**
	 * 按目标 URL 删除信息，URL 唯一
	 */
	/*@Modifying
	// @Transactional(readOnly = true)
	@Query("DELETE FROM ShadowSocksEntity WHERE TARGETURL = ?1")
	void deleteByTargetURL(String targetURL);*/

	/**
	 * 按 TargetURL 查询
	 */
	ShadowSocksEntity findByTargetURL(String targetURL);
}
