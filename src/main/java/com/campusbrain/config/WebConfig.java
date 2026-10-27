package com.campusbrain.config;

import com.campusbrain.security.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/admin/login").setViewName("forward:/admin/index.html");
        registry.addViewController("/admin/login/").setViewName("forward:/admin/index.html");
        registry.addViewController("/admin").setViewName("forward:/admin/index.html");
        registry.addViewController("/admin/").setViewName("forward:/admin/index.html");
        registry.addViewController("/upload").setViewName("forward:/admin/index.html");
        registry.addViewController("/upload/").setViewName("forward:/admin/index.html");
    }
}
