package com.chat.chatbackend.db;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.chat.chatbackend.codegen.types.Message;
import com.chat.chatbackend.codegen.types.User;

@Table("messages")
public record MessageRecord(@PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1) String channel_id,
        @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2) UUID message_id,
        Date time,
        String user_id,
        String user_name,
        String message) {

    public Message toGraphQL() {
        return Message.newBuilder()
                .channel_id(this.channel_id())
                .id(this.message_id().toString())
                .time(this.time().getTime())
                .user(new User(this.user_id(), this.user_name()))
                .message(this.message())
                .build();
    }
}
