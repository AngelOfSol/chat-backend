package com.chat.chatbackend;

import java.util.List;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsSubscription;
import com.netflix.graphql.dgs.InputArgument;

import com.chat.chatbackend.codegen.DgsConstants;
import com.chat.chatbackend.codegen.types.Channel;
import com.chat.chatbackend.codegen.types.Message;
import com.chat.chatbackend.codegen.types.User;

@CrossOrigin
@DgsComponent
public class ChannelDataFetcher {
    private ChannelDataService service;

    public ChannelDataFetcher(ChannelDataService service) {
        this.service = service;
    }

    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = DgsConstants.QUERY.Channels)
    public List<Channel> channels() {
        var channels = service.channelList.keySet().stream().map(key -> Channel.newBuilder().id(key).build())
                .collect(Collectors.toList());
        return channels;
    }

    @DgsData(parentType = DgsConstants.CHANNEL.TYPE_NAME, field = DgsConstants.CHANNEL.Messages)
    public List<Message> messagesOfChannel(DgsDataFetchingEnvironment dfe) {
        var messages = service.channelList.get(dfe.<Channel>getSource().getId());
        return messages;
    }

    @DgsData(parentType = DgsConstants.MESSAGE.TYPE_NAME, field = DgsConstants.MESSAGE.User)
    public User userOfMessage(DgsDataFetchingEnvironment dfe) {
        Message source = dfe.getSource();
        var user = service.users.stream().filter(item -> item.getId().equals(source.getUser().getId())).findFirst()
                .orElseThrow();
        return user;
    }

    @DgsData(parentType = DgsConstants.CHANNEL.TYPE_NAME, field = DgsConstants.CHANNEL.Users)
    public List<User> usersOfChannel(DgsDataFetchingEnvironment dfe) {
        Channel source = dfe.getSource();
        var users = service.getChannelUsers(source.getId());
        return users;
    }

    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = DgsConstants.QUERY.Messages)
    public List<Message> messages(
            @InputArgument(name = DgsConstants.QUERY.MESSAGES_INPUT_ARGUMENT.ChannelName) String channelName) {
        var messages = service.channelList.get(channelName);
        return messages;
    }

    @DgsData(parentType = DgsConstants.MUTATION_TYPE, field = DgsConstants.MUTATION.AddMessage)
    public Message addMessage(
            @InputArgument(name = DgsConstants.MUTATION.ADDMESSAGE_INPUT_ARGUMENT.ChannelName) String channelName,
            @InputArgument(name = DgsConstants.MUTATION.ADDMESSAGE_INPUT_ARGUMENT.UserId) String userId,
            @InputArgument(name = DgsConstants.MUTATION.ADDMESSAGE_INPUT_ARGUMENT.Message) String messageData) {
        var time = Long.valueOf(System.currentTimeMillis());
        var message = new Message(userId.concat(time.toString()),
                User.newBuilder().id(userId).build(), time,
                messageData);

        service.addMessage(channelName, message);
        return message;
    }

    @DgsSubscription(field = DgsConstants.SUBSCRIPTION.MessageSent)
    public Publisher<Message> messageSent(
            @InputArgument(name = DgsConstants.SUBSCRIPTION.MESSAGESENT_INPUT_ARGUMENT.ChannelName) String channelName) {
        return service.getMessagePublisher().filter(value -> value.channelName().equals(channelName))
                .map(value -> value.message());

    }

}
