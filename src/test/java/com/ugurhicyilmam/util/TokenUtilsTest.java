package com.ugurhicyilmam.util;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class TokenUtilsTest {
    @Test
    public void generateToken_shouldGenerateUniqueToken() throws Exception {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 100000; i++) {
            String generatedToken = TokenUtils.generateToken();
            if (tokens.contains(generatedToken))
                fail();
            tokens.add(generatedToken);
        }
    }

    @Test
    public void generateToken_shouldGeneratedTokenHaveLength32() throws Exception {
        int tokenLength = TokenUtils.generateToken().length();
        assertTrue(30 < tokenLength && tokenLength < 35);
    }

    @Test
    public void encodeBase64_shouldEncodeToBase64() throws Exception {
        assertEquals("ZW5jb2RlVG9CYXNlNjQ=", TokenUtils.encodeBase64("encodeToBase64"));
    }

    @Test
    public void decodeBase64_shouldDecodeBase64() throws Exception {
        assertEquals("encodeToBase64", TokenUtils.decodeBase64("ZW5jb2RlVG9CYXNlNjQ="));
    }


}