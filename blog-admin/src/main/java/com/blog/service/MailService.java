package com.blog.service;

import com.blog.constant.Constant;

import com.blog.exception.BusinessException;
import com.blog.util.CodeUtils;
import com.blog.util.bo.EmailCodeBo;
import com.blog.util.redis.RedisTransKey;
import com.blog.util.redis.RedisProcessor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author: zhang
 * @time: 2024-09-14 12:48
 */
@Service
@Validated
@Slf4j
@AllArgsConstructor
public class MailService {

    private final JavaMailSenderImpl javaMailSender;
    private final EmailCodeBo emailCodeBo;
    private final RedisProcessor redisProcessor;

    /**
     * 发送邮箱验证码，并将验证码信息保存在redis中
     *
     * @param toEmail 邮箱
     */
    public void getEmailCode(String toEmail) {
        String code = CodeUtils.getCode();
        emailCodeBo.setEmail(toEmail);
        emailCodeBo.setCode(code);
        /*redis缓存emailCodeBo，key为邮箱，时间为60s*/
        redisProcessor.set(RedisTransKey.emailKey(toEmail), emailCodeBo, 1, TimeUnit.DAYS);
        sendCodeMailMessage(toEmail, code);
    }

    /**
     * 发送邮件验证码
     * @param toEmail 邮箱
     * @param code    验证码
     * @time 2024-08-22 17:03
     */
    private void sendCodeMailMessage(String toEmail, String code) {
        String subject = "【博客】验证码";
        String text = "您的验证码为：" + code;
        sendTextMailMessage(toEmail, subject, text);
        log.info("邮件验证码发送成功");
    }

    /**
     * 发送文本信息
     * @param to 发送目的邮箱
     * @param subject 发送邮件主题
     * @param text 发送邮件内容
     */
    private void sendTextMailMessage(String to, String subject, String text) {
        try {
            //true 代表支持复杂的类型
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);
            //邮件发信人
            mimeMessageHelper.setFrom(Constant.SEND_MAILER);
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
            log.info("邮件发送成功" + Constant.SEND_MAILER + "->" + to);

        } catch (Exception e) {
            log.error("邮件发送失败" + Constant.SEND_MAILER + "->" + to, e);
            throw new BusinessException("邮件发送失败，请检查邮箱是否输入正确");
        }
    }
}
