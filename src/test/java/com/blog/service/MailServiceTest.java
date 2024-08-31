package com.blog.service;

import com.blog.exception.BusinessException;
import com.blog.util.bo.HttpSessionBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mockJavaMailSender;

    @InjectMocks
    private MailService mailServiceUnderTest;



    @Test
    void testSendCodeMailMessage() {

        String toEmail = "212270053@hdu.edu.cn";
        String code = "123456";
        HttpSessionBO sessionBO = new HttpSessionBO(toEmail, code);

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mockJavaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mockJavaMailSender).send(any(MimeMessage.class));

        mailServiceUnderTest.sendCodeMailMessage( sessionBO);

        verify(mockJavaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendCodeMailMessage_withException() {

        String toEmail = "212270053@hdu.edu.cn";
        String code = "123456";
        HttpSessionBO sessionBO = new HttpSessionBO(toEmail, code);

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mockJavaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new BusinessException("验证码发送失败")).when(mockJavaMailSender).send(any(MimeMessage.class));

        assertThrows(BusinessException.class,()->mailServiceUnderTest.sendCodeMailMessage( sessionBO));

    }


}
