package com.ugurhicyilmam.controller.request;

import com.ugurhicyilmam.controller.validation.EmailValid;
import com.ugurhicyilmam.controller.validation.LanguageValid;
import com.ugurhicyilmam.util.enums.Language;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@Data
public class RegisterRequest {
    @Size(min = 1, max = 30)
    private String firstName;

    @Size(min = 1, max = 30)
    private String lastName;

    @Size(min = 6, max = 20)
    private String password;

    @NotNull
    private String passwordConfirmation;

    @EmailValid
    private String email;

    @LanguageValid
    private String language;

    @AssertTrue(message = "{com.ugurhicyilmam.constraint.PasswordConfirmation.message}")
    private boolean isPasswordConfirmation() {
        return Objects.equals(password, passwordConfirmation);
    }
}
