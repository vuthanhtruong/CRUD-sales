package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Account {

    @Id
    private String id;

    private String username;
    private String password;

    private LocalDate createdTime;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Person user;
}