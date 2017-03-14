package com.ugurhicyilmam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Collections;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("localhost");
        javaMailSender.setPort(25);
        return javaMailSender;
    }

//    @Bean
//    public TemplateEngine emailTemplateEngine() {
//        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
//        templateEngine.addTemplateResolver(htmlTemplateResolver());
//        return templateEngine;
//    }
//
//    private ITemplateResolver htmlTemplateResolver() {
//        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
//        templateResolver.setResolvablePatterns(Collections.singleton("html/*"));
//        templateResolver.setPrefix("/mail/");
//        templateResolver.setSuffix(".html");
//        templateResolver.setTemplateMode("HTML");
//        templateResolver.setCharacterEncoding("UTF-8");
//        templateResolver.setCacheable(false);
//        return templateResolver;
//    }


}
