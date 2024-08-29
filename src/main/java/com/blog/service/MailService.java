package com.blog.service;

import com.blog.config.Constant;

import com.blog.exception.BusinessException;
import com.blog.util.bo.HttpSessionBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Date;


@Service
@Validated
@Slf4j
public class MailService {

    @Autowired
    private JavaMailSenderImpl javaMailSender;



    /**
     *
     * @Descriptionn 发送邮件验证码
     * @param sessionBo
     * @Time 2024-08-22 17:03
     */
    public void sendCodeMailMessage(HttpSessionBO sessionBo) {
        String code = (String) sessionBo.getCode();
        String toEmail = (String) sessionBo.getEmail();
        String subject = "【博客】验证码";
        String text = "您的验证码为：" + code;
        sendTextMailMessage(toEmail, subject, text);
        log.info("邮件验证码发送成功");
    }
    /**
     * @Descriptionn 发送文本邮件
     * @param to
     * @param subject
     * @param text
     */
    public void sendTextMailMessage(String to, String subject, String text) {
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
            log.info("邮件发送成功"+Constant.sendMailer+"->"+to);

        } catch (Exception e) {
            log.error("邮件发送失败"+Constant.sendMailer+"->"+to,e);
            throw new BusinessException("邮件发送失败",e);
        }
    }
}
