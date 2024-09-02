package com.blog.service;

import com.blog.exception.BusinessException;
import com.blog.util.CodeUtils;
import com.blog.util.bo.EmailCodeBo;
import com.blog.util.redis.RedisTransKey;
import com.blog.util.redis.RedisProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSenderImpl mockJavaMailSender;
    @Mock
    private EmailCodeBo mockEmailCodeBo;
    @Mock
    private RedisProcessor mockRedisProcessor;

    private MailService mailServiceUnderTest;

    @BeforeEach
    void setUp() {
        mailServiceUnderTest = new MailService(mockJavaMailSender, mockEmailCodeBo, mockRedisProcessor);
    }

    @Test
    void testGetEmailCode() {

        try (MockedStatic<CodeUtils> mockedCodeUties = mockStatic(CodeUtils.class)){
            final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
            when(mockJavaMailSender.createMimeMessage()).thenReturn(mimeMessage);
            mockedCodeUties.when(CodeUtils::getCode).thenReturn("tested");

            mailServiceUnderTest.getEmailCode("2436056388@qq.com");

            verify(mockEmailCodeBo).setEmail("2436056388@qq.com");
            verify(mockEmailCodeBo).setCode("tested");

            final EmailCodeBo value = new EmailCodeBo();
            value.setCode("tested");
            value.setEmail("2436056388@qq.com");
            verify(mockRedisProcessor).set(eq(RedisTransKey.emailKey("2436056388@qq.com")), any(EmailCodeBo.class), eq(60L), eq(TimeUnit.SECONDS));
            verify(mockJavaMailSender).send(any(MimeMessage.class));

            mockedCodeUties.verify(CodeUtils::getCode);
        }
    }

    @Test
    void testGetEmailCode_JavaMailSenderImplSendThrowsMailException() {

        try(MockedStatic<CodeUtils> codeUtiesMockedStatic = mockStatic(CodeUtils.class)) {
            final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
            when(mockJavaMailSender.createMimeMessage()).thenReturn(mimeMessage);
            codeUtiesMockedStatic.when(CodeUtils::getCode).thenReturn("tested");

            doThrow(new RuntimeException()).when(mockJavaMailSender).send(any(MimeMessage.class));

            assertThrows(BusinessException.class, () -> {
                mailServiceUnderTest.getEmailCode("2436056388@qq.com");
            });
            verify(mockEmailCodeBo).setEmail("2436056388@qq.com");
            verify(mockEmailCodeBo).setCode(anyString());
            verify(mockRedisProcessor).set(anyString(), any(EmailCodeBo.class), eq(60L), eq(TimeUnit.SECONDS));
        }
    }
}
