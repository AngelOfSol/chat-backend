package com.chat.chatbackend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.chat.chatbackend.codegen.types.Message;
import com.chat.chatbackend.codegen.types.User;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Service
public class ChannelDataService {
    public record ChannelMessage(String channelName, Message message) {
    }

    public List<User> users = List.of(new User("9656ac8f-f0be-4889-bc8b-fcee5f237cac", "Alice"), new User("2", "Bob"));

    public FluxSink<ChannelMessage> messageStream;
    public ConnectableFlux<ChannelMessage> messagePublisher;

    public Map<String, List<Message>> channelList = Map.of("General", new ArrayList<Message>(List.of()),
            "Specific", new ArrayList<Message>(List.of()),
            "The Third", new ArrayList<Message>(List.of()));

    @PostConstruct
    private void createService() {

        Flux<ChannelMessage> publisher = Flux.create(emitter -> {
            messageStream = emitter;
        });

        messagePublisher = publisher.publish();
        messagePublisher.connect();
    }

    public void addMessage(String channelName, Message message) {
        channelList.get(channelName).add(message);
        messageStream.next(new ChannelMessage(channelName, message));
    }

    public ConnectableFlux<ChannelMessage> getMessagePublisher() {
        return messagePublisher;
    }

    public User getUser(String userName) {
        return users.stream().filter(item -> item.getName().equals(userName)).findFirst()
                .orElseThrow();
    }

    public List<User> getChannelUsers(String channelName) {
        return channelList
                .get(channelName).stream()
                .map(value -> value.getUser().getId())
                .distinct()
                .flatMap(id -> users.stream().filter(item -> item.getId().equals(id)))
                .collect(Collectors.toList());
    }

}
