package com.chat.chatbackend;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.chat.chatbackend.codegen.types.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import com.netflix.graphql.dgs.autoconfig.DgsExtendedScalarsAutoConfiguration;

import java.util.Map;
import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import graphql.ExecutionResult;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.Many;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

@SpringBootTest(classes = { DgsAutoConfiguration.class, DgsExtendedScalarsAutoConfiguration.class,
        ChannelDataFetcher.class, ChannelDataService.class })
class ChannelDataFetcherTest {

    @Autowired
    DgsQueryExecutor queryExecutor;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void resultingData() {
        Many<String> test = Sinks.many().multicast().directBestEffort();

        test.asFlux().publish().subscribe(new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(2);
            }

            @Override
            public void onNext(String executionResult) {
                assertThat(executionResult).isEqualTo("null");
                // if (executionResult.getErrors().size() > 0) {
                // System.out.println(executionResult.getErrors());
                // }
                // Map<String, Object> review = executionResult.getData();
                // reviews.add(new ObjectMapper().convertValue(review.get("reviewAdded"),
                // Review.class));
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });

        test.emitNext("null", EmitFailureHandler.FAIL_FAST);

        ExecutionResult executionResult = queryExecutor.execute("subscription {\r\n" + //
                "  messageSent(channelName: \"General\") {\r\n" + //
                "    message\r\n" + //
                "    time\r\n" + //
                "  }\r\n" + //
                "}");
        Publisher<ExecutionResult> publisher = executionResult.getData();

        ExecutionResult exec2 = queryExecutor.execute("mutation {\r\n" + //
                "  addMessage(channelName:\"General\", userId:1, message:\"Test message\") {\r\n" + //
                "    message\r\n" + //
                "  }\r\n" + //
                "}");
        assertThat(tomessage2(exec2).getMessage()).isEqualTo("Test message");
        VirtualTimeScheduler _ = VirtualTimeScheduler.create();
        StepVerifier.withVirtualTime(() -> publisher, 1)
                .expectSubscription()
                .thenRequest(1)
                .assertNext(result -> assertThat(tomessage(result).getMessage()).isEqualTo("300"))
                .thenCancel()

                .verify(Duration.ofSeconds(2));
    }

    private Message tomessage(ExecutionResult result) {
        Map<String, Object> data = result.getData();
        return objectMapper.convertValue(data.get("messageSent"), Message.class);
    }

    private Message tomessage2(ExecutionResult result) {
        Map<String, Object> data = result.getData();
        return objectMapper.convertValue(data.get("addMessage"), Message.class);
    }
}