package com.nowcoder.community.controller.intercepter;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DateService;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DateInterceptor implements HandlerInterceptor {
    @Autowired
    private DateService dateService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录UV
        String ip = request.getRemoteHost();
        dateService.recordUV(ip);
        // 记录DAU
        User user = hostHolder.getUser();
        if (user != null) {
            dateService.recordDAU(user.getId());
        }
        return true;
    }
}
