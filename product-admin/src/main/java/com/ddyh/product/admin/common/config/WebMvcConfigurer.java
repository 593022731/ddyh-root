package com.ddyh.product.admin.common.config;

import com.ddyh.product.admin.common.interceptor.UserAuthenticationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author: cqry2017
 * @Date: 2019/6/15 18:03
 */
@Configuration
public class WebMvcConfigurer extends WebMvcConfigurationSupport {

    /**
     * 在添加拦截器之前，先创建该bean，纳入到spring中。
     * 解决拦截器中无法依赖注入的问题
     *
     * @return
     */
    @Bean
    public UserAuthenticationInterceptor getUserAuthenticationInterceptor() {
        return new UserAuthenticationInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getUserAuthenticationInterceptor()).addPathPatterns("/**");
    }

}
