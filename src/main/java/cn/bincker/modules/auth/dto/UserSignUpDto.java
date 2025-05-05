package cn.bincker.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserSignUpDto {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$")
    private String username;
    @NotBlank
    @Min(6)
    private String password;
    @Email
    @NotBlank
    private String email;
}
