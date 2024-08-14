package com.chat.chatbackend.db;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

public interface MessageRepository extends ReactiveCassandraRepository<MessageRecord, String> {

    // @Query("INSERT INTO messages (message_id, channel_id, user_id, user_name,
    // time, message) VALUES (now(), ?0, ?1, ?2, toTimeStamp(now()), ?3)")
    // Mono<MessageRecord> addMessage(String channelId, String userId, String
    // userName, String message);

    @Query("SELECT * FROM messages WHERE channel_id = ?0")
    Flux<MessageRecord> findByChannelId(String channel_id);
}
