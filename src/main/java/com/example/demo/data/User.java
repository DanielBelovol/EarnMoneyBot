package com.example.demo.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@Setter
@AllArgsConstructor
@Table(name = "users")
public class User {
    public User(){

    }
    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "username")
    private String userName;
    @Column(name = "money")
    private long money;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
}
