package com.ugurhicyilmam.controller.validation;

import com.ugurhicyilmam.service.UserService;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EmailValidImpl implements ConstraintValidator<EmailValid, String> {

    private static final Map<String, String> domains = new ConcurrentHashMap<>();

    static {
        domains.put("yildiz.edu.tr", "");
        domains.put("boun.edu.tr", "");
    }

    private final UserService userService;
    private final boolean validateEmailDomain;

    @Autowired
    public EmailValidImpl(UserService userService, @Value("${application.validate-email-domain}") boolean validateEmailDomain) {
        this.userService = userService;
        this.validateEmailDomain = validateEmailDomain;
    }

    @Override
    public void initialize(EmailValid constraint) {
        //ignore
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (!isValidEmailAddress(email))
            return false;

        if (validateEmailDomain && !emailDomainValid(email))
            return false;

        return userService.findByEmail(email) == null;
    }

    public Set<String> validDomains() {
        return domains.keySet();
    }

    private boolean emailDomainValid(String email) {
        String emailDomain = extractDomain(email);
        return domains.containsKey(emailDomain);
    }

    private String extractDomain(String email) {
        return email.split("@")[1];
    }

    private boolean isValidEmailAddress(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
