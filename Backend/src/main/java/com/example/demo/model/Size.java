package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "size")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Size {

    @Id
    private String id;

    private String name;
}