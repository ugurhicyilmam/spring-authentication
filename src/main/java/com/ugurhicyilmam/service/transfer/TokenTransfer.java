package com.ugurhicyilmam.service.transfer;

import com.ugurhicyilmam.util.TokenUtils;
import lombok.Data;

@Data
public class TokenTransfer {
    private String accessToken;
    private String refreshToken;

    public String getAccessToken() {
        return TokenUtils.encodeBase64(accessToken);
    }

    public String getRefreshToken() {
        return TokenUtils.encodeBase64(refreshToken);
    }
}
