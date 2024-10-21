package com.example.demo.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Entity
@Data
@Setter
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "id")
    private final long id;
    @Column(name = "username")
    private final String userName;
    @Column(name = "money")
    private long money;

    @Column(name = "role")
    private final UserRole userRole;
}
