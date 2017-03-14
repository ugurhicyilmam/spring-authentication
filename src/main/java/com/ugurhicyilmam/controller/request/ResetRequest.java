package com.ugurhicyilmam.controller.request;

import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@Data
public class ResetRequest {
    @Size(min = 6, max = 20)
    private String password;
    @NotNull
    private String passwordConfirmation;
    @AssertTrue(message = "{com.ugurhicyilmam.constraint.PasswordConfirmation.message}")
    private boolean isPasswordConfirmation() {
        return Objects.equals(password, passwordConfirmation);
    }
}
