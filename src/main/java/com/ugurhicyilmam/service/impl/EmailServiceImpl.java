package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.EmailService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;


@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine emailTemplateEngine;
    private final MessageSource messageSource;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine emailTemplateEngine, MessageSource messageSource) {
        this.mailSender = mailSender;
        this.emailTemplateEngine = emailTemplateEngine;
        this.messageSource = messageSource;
    }

    @Override
    public void sendActivationEmail(User user) {
        Locale userLocale = Locale.forLanguageTag(user.getLanguage().toString());

        Email email = new Email();
        email.setTo(user.getEmail());
        email.setSubject(messageSource.getMessage("mail.registration.subject", null, userLocale));
        email.setContent(processActivationTemplate(messageSource.getMessage("mail.registration.message", null, userLocale)));

        sendHtmlEmail(email);
    }

    private String processActivationTemplate(String message) {
        final Context context = new Context();
        context.setVariable("message", message);
        return emailTemplateEngine.process("email-activation", context);
    }

    private void sendHtmlEmail(Email email) {
        MimeMessagePreparator mimeMessagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("ugur@hicyilmam.com");
            messageHelper.setTo(email.getTo());
            messageHelper.setSubject(email.getSubject());
            messageHelper.setText(email.getContent(), true);
        };
        try {
            mailSender.send(mimeMessagePreparator);
        } catch (MailException e) {
            logger.error("E-mail could not be sent. Exception message: {]", e.getMessage());
            logger.debug("{}", e);
        }
    }

    @Data
    private static class Email {
        private String to;
        private String subject;
        private String content;
    }
}
