package com.blog.service;

import com.blog.config.Constant;

import com.blog.exception.BusinessException;
import com.blog.util.CodeUties;
import com.blog.util.bo.EmailCodeBo;
import com.blog.util.bo.HttpSessionBO;
import com.blog.util.redis.RedisTransKey;
import com.blog.util.redis.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Date;
import java.util.concurrent.TimeUnit;


@Service
@Validated
@Slf4j
@AllArgsConstructor
public class MailService {

    private final JavaMailSenderImpl javaMailSender;
    private final EmailCodeBo emailCodeBo;
    private final RedisUtils redisUtils;

    /**
     * 发送邮箱验证码，并将验证码信息保存在redis中
     * @param toEmail
     */
    public void getEmailCode(String toEmail){
        String code = CodeUties.getCode();
        emailCodeBo.setEmail(toEmail);
        emailCodeBo.setCode(code);
        /*redis缓存emailCodeBo，key为邮箱，时间为60s*/
        redisUtils.set(RedisTransKey.setEmailKey(toEmail), emailCodeBo, 60, TimeUnit.SECONDS);
        sendCodeMailMessage(toEmail,code);
   }
    /**
     * @param toEmail
     * @param code
     * @Descriptionn 发送邮件验证码
     * @Time 2024-08-22 17:03
     */
/*    public void sendCodeMailMessage(HttpSessionBO sessionBo) {
        String code = (String) sessionBo.getCode();
        String toEmail = (String) sessionBo.getEmail();
        String subject = "【博客】验证码";
        String text = "您的验证码为：" + code;
        sendTextMailMessage(toEmail, subject, text);
        log.info("邮件验证码发送成功");
    }*/
    private void sendCodeMailMessage(String toEmail, String code) {
        String subject = "【博客】验证码";
        String text = "您的验证码为：" + code;
        sendTextMailMessage(toEmail, subject, text);
        log.info("邮件验证码发送成功");
    }

    /**
     * @param to
     * @param subject
     * @param text
     * @Descriptionn 发送文本邮件
     */
    private void sendTextMailMessage(String to, String subject, String text) {
        try {
            //true 代表支持复杂的类型
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);
            //邮件发信人
            mimeMessageHelper.setFrom(Constant.sendMailer);
            //邮件收信人  1或多个
            mimeMessageHelper.setTo(to.split(","));
            //邮件主题
            mimeMessageHelper.setSubject(subject);
            //邮件内容
            mimeMessageHelper.setText(text);
            //邮件发送时间

            mimeMessageHelper.setSentDate(new Date());

            //发送邮件
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            log.info("邮件发送成功" + Constant.sendMailer + "->" + to);

        } catch (Exception e) {
            log.error("邮件发送失败" + Constant.sendMailer + "->" + to, e);
            throw new BusinessException("邮件发送失败", e);
        }
    }
}
