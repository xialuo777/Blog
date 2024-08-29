package com.blog.util.bo;

import com.blog.exception.BusinessException;
import com.blog.util.CodeUties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpSession;
@Slf4j
@Data
public class HttpSessionBO<T,U> {
    private T email;
    private U code;

    public HttpSessionBO(T email, U code) {
        this.email = email;
        this.code = code;
    }

    public static HttpSessionBO<String,String> getHttpSessionBO(HttpSession session,String toMail) {
        String code = CodeUties.getCode();
        session.setAttribute("email",toMail);
        session.setAttribute("code",code);
        Object emailObj = session.getAttribute("email");
        Object codeObj =  session.getAttribute("code");
        return new HttpSessionBO( emailObj,codeObj);
    }
    public static HttpSessionBO getHttpSessionBO(HttpSession session) {
        Object emailObj = session.getAttribute("email");
        Object codeObj =  session.getAttribute("code");
        return new HttpSessionBO( emailObj,codeObj);
    }

}
