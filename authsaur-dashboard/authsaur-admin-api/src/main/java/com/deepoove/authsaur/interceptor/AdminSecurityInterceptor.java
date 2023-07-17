package com.deepoove.authsaur.interceptor;

import cn.hutool.json.JSONUtil;
import com.deepoove.authsaur.model.TokenCacheModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
public class AdminSecurityInterceptor implements HandlerInterceptor {

    private final RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOGGER.info("[preHandle][" + request + "]" + "[" + request.getMethod() + "]" + request.getRequestURI());

        String token = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(token)) {
            Object str = redisTemplate.opsForValue().get(token);
            if (null != str) {
                TokenCacheModel tokenCacheModel = JSONUtil.toBean(String.valueOf(str), TokenCacheModel.class);
                AssertionHolder.setAssertion(tokenCacheModel.getUser());
                return true;
            }
        }
        response.setStatus(401);
        return false;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {
        AssertionHolder.clear();
    }
}
