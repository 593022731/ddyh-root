package com.ddyh.product.admin.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ddyh.product.facade.dto.JdBackUserDTO;
import com.ddyh.product.facade.facade.BackUserFacade;
import com.ddyh.product.facade.param.BackUserParam;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Controller
public class BaseController {

    @Reference
    private BackUserFacade backUserFacade;

    public static Date addMinute(Date date, int minute) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minute);
        return calendar.getTime();
    }


    protected static String getToken(JdBackUserDTO backUserDto) {
        return JWT.create().withExpiresAt(addMinute(new Date(), 60)).withAudience(backUserDto.getId().toString())
                .sign(Algorithm.HMAC256(backUserDto.getPassword()));
    }

    protected JdBackUserDTO getJdBackUser(String token) {
        JdBackUserDTO jdBackUserDto;
        try {
            String userId = JWT.decode(token).getAudience().get(0);
            BackUserParam backUserParam = new BackUserParam();
            backUserParam.setId(Integer.valueOf(userId));
            jdBackUserDto = backUserFacade.findBackUserByParams(backUserParam);
        } catch (Exception j) {
            return null;
        }
        return jdBackUserDto;
    }

    protected String getToken(HttpServletRequest request) {
        return request.getHeader("token");
    }
}
