package com.ugurhicyilmam.controller.validation;

import com.ugurhicyilmam.util.enums.Language;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LanguageValidImplTest {

    private LanguageValidImpl languageValid;

    @Before
    public void setUp() throws Exception {
        languageValid = new LanguageValidImpl();
    }

    @After
    public void tearDown() throws Exception {
        languageValid = null;
    }

    @Test
    public void isValid_shouldReturnTrueIfValidLanguage() throws Exception {
        for (Language language : Language.values())
            assertTrue(languageValid.isValid(language.toString(), null));
    }

    @Test
    public void isValid_shouldReturnFalseIfInvalidLanguage() throws Exception {
        assertFalse(languageValid.isValid(null, null));
        assertFalse(languageValid.isValid("", null));
        assertFalse(languageValid.isValid("gibberish", null));
    }

}