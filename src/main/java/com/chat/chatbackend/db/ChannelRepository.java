package com.chat.chatbackend.db;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

public interface ChannelRepository extends ReactiveCassandraRepository<ChannelRecord, String> {

}
