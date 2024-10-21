package com.example.demo.repositories;

import com.example.demo.data.Channel;
import com.example.demo.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u JOIN ChannelSubscriber cs ON u.id = cs.userId WHERE cs.channelId = :channelId")
    List<User> findUsersByChannelId(@Param("channelId") long channelId);
}

