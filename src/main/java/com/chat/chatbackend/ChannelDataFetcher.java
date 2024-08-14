package com.chat.chatbackend;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsSubscription;
import com.netflix.graphql.dgs.InputArgument;

import reactor.core.publisher.Mono;

import com.chat.chatbackend.codegen.DgsConstants;
import com.chat.chatbackend.codegen.types.Channel;
import com.chat.chatbackend.codegen.types.Message;
import com.chat.chatbackend.codegen.types.User;
import com.chat.chatbackend.db.MessageRecord;

@CrossOrigin
@DgsComponent
public class ChannelDataFetcher {
    private ChannelDataService service;

    public ChannelDataFetcher(ChannelDataService service) {
        this.service = service;
    }

    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = DgsConstants.QUERY.Channels)
    public Mono<List<Channel>> channels() {
        return service.getChannels();
    }

    @DgsData(parentType = DgsConstants.CHANNEL.TYPE_NAME, field = DgsConstants.CHANNEL.Messages)
    public Mono<List<Message>> messagesOfChannel(DgsDataFetchingEnvironment dfe) {
        var messages = service.getMessagesByChannel(dfe.<Channel>getSource().getId());
        return messages;
    }

    // @DgsData(parentType = DgsConstants.MESSAGE.TYPE_NAME, field =
    // DgsConstants.MESSAGE.User)
    // public User userOfMessage(DgsDataFetchingEnvironment dfe) {
    // Message source = dfe.getSource();
    // var user = service.users.stream().filter(item ->
    // item.getId().equals(source.getUser().getId())).findFirst()
    // .orElseThrow();
    // return user;
    // }

    @DgsData(parentType = DgsConstants.CHANNEL.TYPE_NAME, field = DgsConstants.CHANNEL.Users)
    public Mono<List<User>> usersOfChannel(DgsDataFetchingEnvironment dfe) {
        Channel source = dfe.getSource();
        var users = service.getChannelUsers(source.getId());
        return users;
    }

    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = DgsConstants.QUERY.Messages)
    public Mono<List<Message>> messages(
            @InputArgument(name = DgsConstants.QUERY.MESSAGES_INPUT_ARGUMENT.ChannelName) String channelName) {
        var messages = service.getMessagesByChannel(channelName);
        return messages;
    }

    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = DgsConstants.QUERY.AllMessagesForUser)
    public Mono<List<Message>> allMessagesForUser(
            @InputArgument(name = DgsConstants.QUERY.ALLMESSAGESFORUSER_INPUT_ARGUMENT.UserId) String userId) {
        var messages = service.getMessagesByUser(userId);
        return messages;
    }

    @DgsData(parentType = DgsConstants.MUTATION_TYPE, field = DgsConstants.MUTATION.AddMessage)
    public Mono<Message> addMessage(
            @InputArgument(name = DgsConstants.MUTATION.ADDMESSAGE_INPUT_ARGUMENT.ChannelName) String channelName,
            @InputArgument(name = DgsConstants.MUTATION.ADDMESSAGE_INPUT_ARGUMENT.UserId) String userId,
            @InputArgument(name = DgsConstants.MUTATION.ADDMESSAGE_INPUT_ARGUMENT.UserName) String userName,
            @InputArgument(name = DgsConstants.MUTATION.ADDMESSAGE_INPUT_ARGUMENT.Message) String messageData) {

        return service.addMessage(channelName, userId, userName, messageData);
    }

    @DgsSubscription(field = DgsConstants.SUBSCRIPTION.MessageSent)
    public Publisher<Message> messageSent(
            @InputArgument(name = DgsConstants.SUBSCRIPTION.MESSAGESENT_INPUT_ARGUMENT.ChannelName) String channelName) {
        return service.getMessagePublisher().filter(value -> value.getChannel_id().equals(channelName));

    }

    @DgsSubscription(field = DgsConstants.SUBSCRIPTION.MessageSentForUser)
    public Publisher<Message> messageSentForUser(
            @InputArgument(name = DgsConstants.SUBSCRIPTION.MESSAGESENTFORUSER_INPUT_ARGUMENT.UserId) String userId) {
        // TODO filter by user, currently all messages because backend schema undecided
        return service.getMessagePublisher();

    }

}
