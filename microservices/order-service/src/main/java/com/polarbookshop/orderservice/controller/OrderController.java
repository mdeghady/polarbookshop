package com.polarbookshop.orderservice.controller;

import com.polarbookshop.orderservice.dto.OrderRequest;
import com.polarbookshop.orderservice.model.Order;
import com.polarbookshop.orderservice.repository.OrderRepository;
import com.polarbookshop.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Flux<Order> getAllOrders(@AuthenticationPrincipal Jwt jwt){
        return orderService.getAllOrders(jwt.getSubject());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Order> submitOrder(@RequestBody @Valid OrderRequest orderRequest){
        return orderService.submitOrder(
                orderRequest.isbn() , orderRequest.quantity()
        );
    }
}
