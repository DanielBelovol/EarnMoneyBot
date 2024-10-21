package com.example.demo.repositories;

import com.example.demo.data.Channel;
import com.example.demo.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
