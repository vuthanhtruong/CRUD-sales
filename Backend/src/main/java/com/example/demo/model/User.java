package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User extends Person {

    @Id
    private String id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Account account;
}