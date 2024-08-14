package com.chat.chatbackend.db;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("channels")
public record ChannelRecord(@PrimaryKey String channel_id) {

}
