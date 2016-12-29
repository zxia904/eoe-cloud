package top.sdaily.core.web.spring.interceptor;

import com.alibaba.fastjson.JSON;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import top.sdaily.core.web.context.SessionUser;
import top.sdaily.core.web.exception.FailedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * Created by soya on 2016/12/1.
 */
public class SecurityInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        HashSet<String> noAuthPath = new HashSet<String>();
        noAuthPath.add("/login");

        if(noAuthPath.contains(httpServletRequest.getRequestURI())){
            return true;
        }

        String token = httpServletRequest.getHeader("Access-Token");
        System.out.println("preHandle entry ......" + token);

        if(stringRedisTemplate == null) {
            ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(httpServletRequest.getServletContext());
            stringRedisTemplate = (StringRedisTemplate) applicationContext.getBean("stringRedisTemplate");
        }

        SessionUser user = JSON.parseObject(stringRedisTemplate.opsForValue().get(token),SessionUser.class);

        if(user == null) {
            throw new FailedException("token过期，请重新登录");
        }

        //========================
        // 刷新token有效期
        //========================
        stringRedisTemplate.expire(token,30, TimeUnit.MINUTES);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}