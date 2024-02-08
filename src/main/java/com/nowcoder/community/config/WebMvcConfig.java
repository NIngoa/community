package com.nowcoder.community.config;

import com.nowcoder.community.controller.intercepter.*;
import com.nowcoder.community.dao.LoginTicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private Interceptor interceptor;
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;
    @Autowired
    private DateInterceptor dateInterceptor;
/*    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;*/

    @Autowired
    private MessageCountInterceptor messageCountInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg")
                .addPathPatterns("/register", "/login");
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(messageCountInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(dateInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }
}
