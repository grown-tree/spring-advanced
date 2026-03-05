package org.example.expert.domain.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AdminAccessCheckAop {

    @Around("execution(* org.example.expert.domain.*.controller.*AdminController.*(..))")
    public Object checkAdminAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        //요청 시각
        LocalDateTime requestTime = LocalDateTime.now();

        //AOP에서는 HttpServletRequest를 직접 파라미터로 받을 수 없기 때문에
        //스프링이 제공하는 RequestContextHolder를 통해 현재 요청 정보를 가져옴
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();

        //요청 URL
        String requestUrl = request.getRequestURI();
        //사용자 ID
        Long userId = (Long) request.getAttribute("userId");

        //실제 메서드 실행 = 응답
        Object result = joinPoint.proceed();

        //객체를 JSON타입으로 변환하기위해 선언
        ObjectMapper objectMapper = new ObjectMapper();

        String requestBody = objectMapper.writeValueAsString(joinPoint.getArgs());
        String responseBody = objectMapper.writeValueAsString(result);

        //로깅
        log.info("userId:{}, requestTime:{}, url:{}, requestBody:{}, responseBody:{}",
                userId, requestTime, requestUrl,requestBody,responseBody);

        return  result;

    }
}
