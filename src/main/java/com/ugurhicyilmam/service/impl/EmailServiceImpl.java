package com.ugurhicyilmam.service.impl;

import com.ugurhicyilmam.model.User;
import com.ugurhicyilmam.service.EmailService;
import com.ugurhicyilmam.util.enums.Language;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine emailTemplateEngine;
    private final MessageSource messageSource;
    private final String baseUrl;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine emailTemplateEngine, MessageSource messageSource, @Value("${application.email.base-url}") String baseUrl) {
        this.mailSender = mailSender;
        this.emailTemplateEngine = emailTemplateEngine;
        this.messageSource = messageSource;
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendActivationEmail(User user) {
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("token", "TOKEN");
        sendEmailTo(user, EmailType.ACTIVATION, emailData);
    }

    /**
     * Sends email to User according to given emailType and emailArguments
     */
    @Override
    public void sendEmailTo(User user, EmailType emailType, Map<String, Object> emailData) {
        Email email = new Email();
        email.setTo(user.getEmail());
        email.setSubject(getSubjectForEmailType(emailType, user.getLanguage()));
        email.setContent(prepareContent(emailType, user.getLanguage(), emailData));

        sendHtmlEmail(email);
    }

    /**
     * Gets localized subject according to email type
     */
    private String getSubjectForEmailType(EmailType emailType, Language language, Object... args) {
        return messageSource.getMessage(emailType.getMessageBase() + ".subject", args, Locale.forLanguageTag(language.toString()));
    }

    /**
     * Prepares html content of email according to EmailType, Language, and given email arguments.
     */
    private String prepareContent(EmailType emailType, Language language, Map<String, Object> args) {
        Context context = new Context();
        //put globals into context
        context.setVariable("serverUrl", baseUrl);
        args.forEach(context::setVariable);
        String templatePath = "email/" + language.toString().toLowerCase() + "/" + emailType.getTemplate();
        return emailTemplateEngine.process(templatePath, context);
    }

    /**
     * Sends and email with Content-Type: HTML
     */
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

    public enum EmailType {
        ACTIVATION("email-activation");

        private final String template;
        private final String messageBase;

        EmailType(String template) {
            this.template = template;
            this.messageBase = template.replaceAll("-", ".");
        }

        public String getTemplate() {
            return template;
        }

        public String getMessageBase() {
            return messageBase;
        }
    }
}
