package com.ugurhicyilmam.service.transfer;

import lombok.Data;

@Data
public class LoginTransfer {
    private TokenTransfer tokenTransfer;
    private UserTransfer userInformation;
}
