package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "person")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Person {

    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthday;
}