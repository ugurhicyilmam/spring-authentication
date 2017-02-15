package com.ugurhicyilmam.controller.validation;

import com.ugurhicyilmam.util.enums.Language;
import org.slf4j.Logger;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LanguageValidImpl implements ConstraintValidator<LanguageValid, String> {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(LanguageValidImpl.class);

    @Override
    public void initialize(LanguageValid constraint) {
        // ignore
    }

    @Override
    public boolean isValid(String language, ConstraintValidatorContext context) {
        return language != null && isCastableToLanguage(language);
    }

    private boolean isCastableToLanguage(String language) {
        try {
            Language.valueOf(language);
        } catch (IllegalArgumentException ex) {
            logger.warn("Illegal Language argument provided: {}", language);
            logger.debug("IllegalArgumentException by value: {}, Exception: {}", language, ex);
            return false;
        }
        return true;
    }
}
