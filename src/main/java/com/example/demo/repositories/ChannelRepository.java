package com.example.demo.repositories;

import com.example.demo.data.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    boolean existsByLink(String link);
    void deleteByLink(String link);
    Channel findByLink(String link);
    Channel findByName(String name);
    @Query("SELECT c FROM Channel c JOIN ChannelSubscriber cs ON c.id = cs.channelId WHERE cs.userId = :userId")
    List<Channel> findChannelsByUserId(@Param("userId") long userId);

}
