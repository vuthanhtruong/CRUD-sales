package com.example.demo.dto;

import com.example.demo.model.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountDTO {
    @NotBlank(message = "First name must not be blank")
    @Size(max = 50, message = "First name must be <= 50 characters")
    private String firstName;

    @NotBlank(message = "Last name must not be blank")
    @Size(max = 50, message = "Last name must be <= 50 characters")
    private String lastName;

    @NotBlank(message = "Phone must not be blank")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Phone number is invalid (Vietnam format)")
    private String phone;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email is invalid")
    @Size(max = 120, message = "Email too long")
    private String email;

    @Size(max = 1500000, message = "Avatar is too large")
    private String avatarUrl;

    @NotBlank(message = "Address must not be blank")
    @Size(max = 255, message = "Address too long")
    private String address;

    @NotBlank(message = "Username must not be blank")
    @Size(min = 4, max = 20, message = "Username must be 4-20 characters")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Gender must not be null")
    private Gender gender;

    @NotNull(message = "Birthday must not be null")
    @Past(message = "Birthday must be in the past")
    private LocalDate birthday;
}
