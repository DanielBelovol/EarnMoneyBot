package com.example.demo.repositories;

import com.example.demo.data.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    boolean existsByLink(String link);
    void deleteByLink(String link);
    Channel findByLink(String link);
    Channel findByName(String name);
}
