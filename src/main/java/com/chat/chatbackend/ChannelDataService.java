package com.chat.chatbackend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chat.chatbackend.codegen.types.Channel;
import com.chat.chatbackend.codegen.types.Message;
import com.chat.chatbackend.codegen.types.User;
import com.chat.chatbackend.db.ChannelRepository;
import com.chat.chatbackend.db.MessageRecord;
import com.chat.chatbackend.db.MessageRepository;
import com.datastax.oss.driver.api.core.uuid.Uuids;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

@Service
public class ChannelDataService {

    private FluxSink<Message> messageStream;
    private ConnectableFlux<Message> messagePublisher;

    @Autowired
    private MessageRepository messageDb;
    @Autowired
    private ChannelRepository channelDb;

    @PostConstruct
    private void createService() {

        Flux<Message> publisher = Flux.create(emitter -> {
            messageStream = emitter;
        });

        messagePublisher = publisher.publish();
        messagePublisher.connect();
    }

    public Mono<Message> addMessage(String channelName, String userId, String userName, String message) {
        var now = new Date();
        return this.messageDb
                .save(new MessageRecord(channelName, Uuids.startOf(now.getTime()), now, userId, userName, message))
                .map(MessageRecord::toGraphQL)
                .doOnNext(inner -> this.messageStream.next(inner));
    }

    public ConnectableFlux<Message> getMessagePublisher() {
        return messagePublisher;
    }

    public Mono<List<Channel>> getChannels() {
        return this.channelDb.findAll()
                .map(inner -> Channel.newBuilder().id(inner.channel_id()).build())
                .collectList();
    }

    public Mono<List<Message>> getMessagesByChannel(String channelName) {
        return this.messageDb.findByChannelId(channelName).map(MessageRecord::toGraphQL).collectList();
    }

    public Mono<List<Message>> getMessagesByUser(String userId) {
        // TODO filter by user name
        return this.messageDb.findAll().map(MessageRecord::toGraphQL).collectList();
    }

    public Mono<List<User>> getChannelUsers(String channelName) {

        return messageDb.findAll()
                .filter(inner -> inner.channel_id().equals(channelName))
                .map(inner -> new User(inner.user_id(), inner.user_name()))
                .distinct(inner -> inner.getId())
                .collectList()
                .map(inner -> inner.stream().distinct().collect(Collectors.toList()));

    }

}
