package com.nowcoder.community.config;

import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN, AUTHORITY_MODERATOR, AUTHORITY_USER
                )
                .antMatchers("/discuss/top", "/discuss/wonderful")
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers("/discuss/delete"
                        , "/data/**",
                        "/actuator/**")
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                .and().csrf().disable();

        //权限不够时的处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    //没有登录
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //异步请求xml
                        if (xRequestedWith != null && xRequestedWith.equals("XMLHttpRequest")) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter printWriter = response.getWriter();
                            printWriter.write(CommunityUtil.getJsonStr(403, "您还没有登录!"));
                        } else {
                            //重定向到登录页
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    //权限不够
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestHeader = request.getHeader("x-requested-with");
                        //异步请求xml
                        if (xRequestHeader != null && xRequestHeader.equals("XMLHttpRequest")) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter printWriter = response.getWriter();
                            printWriter.write(CommunityUtil.getJsonStr(403, "您没有权限访问该资源!"));
                        } else {
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });
        //Security底层默认拦截logout请求进行退出处理
        //覆盖默认逻辑
        http.logout().logoutUrl("/securityLogout");

    }
}