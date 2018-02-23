package com.example.ShadowSocksShare.config;

import com.example.ShadowSocksShare.service.CountSerivce;
import com.example.ShadowSocksShare.web.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Spring MVC 配置
 */
@Configuration
public class WebAppConfigurer extends WebMvcConfigurerAdapter {
	@Autowired
	private CountSerivce countSerivce;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 添加 计数 拦截器
		registry.addInterceptor(new Interceptor(countSerivce)).addPathPatterns("/**").excludePathPatterns("/css/**", "/images/**", "/js/**", "/favicon.ico", "/count");
		super.addInterceptors(registry);
	}
}
