package com.polarbookshop.orderservice.service;

import com.polarbookshop.orderservice.dto.OrderDispatchedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Configuration
public class OrderFunctions {

    public static final Logger log = LoggerFactory.getLogger(OrderFunctions.class);

    @Bean
    public Consumer<Flux<OrderDispatchedMessage>> dispatchOrder(OrderService orderService) {
        return flux ->
                orderService.consumeOrderDispatchedEvent(flux)
                        .doOnNext(order -> log.info("The order with id {} is dispatched",
                            order.id()))
                        .subscribe();
    }
}
