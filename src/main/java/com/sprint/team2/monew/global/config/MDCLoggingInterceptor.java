package com.sprint.team2.monew.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class MDCLoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("requestIP", request.getRemoteAddr());
        MDC.put("requestMethod", request.getMethod());
        MDC.put("requestUrl", request.getRequestURI());
        // 응답헤더 설정
        response.setHeader("request-ID", MDC.get("requestId"));
        response.setHeader("request-IP", MDC.get("requestIP"));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.clear();
    }
}
