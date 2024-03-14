package com.polarbookshop.orderservice.repository;

import com.polarbookshop.orderservice.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderRepository extends ReactiveCrudRepository<Order , Long> {

    Flux<Order> findAllByCreatedBy(String userId);
}
