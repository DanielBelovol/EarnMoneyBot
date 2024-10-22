package com.example.demo.repositories;

import com.example.demo.data.ChannelSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelSubscribersRepository extends JpaRepository<ChannelSubscriber, Long> {
    boolean existsByChannelIdAndUserId(long channelId, long userId);
    int countByUserId(long userId);

}

