package edu.cit.sabornido.rentease.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest extends AuthRequest {
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstname;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastname;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "RENTER|OWNER", message = "Role must be RENTER or OWNER")
    private String role;
}
