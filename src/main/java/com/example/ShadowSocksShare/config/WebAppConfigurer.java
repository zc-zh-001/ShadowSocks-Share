package com.example.ShadowSocksShare.config;

import com.example.ShadowSocksShare.service.CountSerivce;
import com.example.ShadowSocksShare.web.Interceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Spring MVC 配置
 */
@Configuration
public class WebAppConfigurer implements WebMvcConfigurer, ApplicationContextAware {


	private CountSerivce countSerivce;
	private ApplicationContext applicationContext;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 添加 计数 拦截器
		registry.addInterceptor(new Interceptor(countSerivce)).addPathPatterns("/**").excludePathPatterns("/webjars/**", "/css/**", "/images/**", "/js/**", "/favicon.ico", "/count");
		// 本地化拦截器
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("language");
		registry.addInterceptor(localeChangeInterceptor).addPathPatterns("/**");
	}

	@Autowired
	public void setCountSerivce(CountSerivce countSerivce) {
		this.countSerivce = countSerivce;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
