package com.polarbookshop.orderservice.service;

import com.polarbookshop.orderservice.dto.Book;
import com.polarbookshop.orderservice.dto.OrderAcceptedMessage;
import com.polarbookshop.orderservice.dto.OrderDispatchedMessage;
import com.polarbookshop.orderservice.model.Order;
import com.polarbookshop.orderservice.model.OrderStatus;
import com.polarbookshop.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {

    private static final Logger log =
            LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final BookCatalogService bookClient;
    private final StreamBridge streamBridge;

    public OrderService(OrderRepository orderRepository, BookCatalogService bookClient, StreamBridge streamBridge) {
        this.orderRepository = orderRepository;
        this.bookClient = bookClient;
        this.streamBridge = streamBridge;
    }

    public Flux<Order> getAllOrders(String userId) {
        return orderRepository.findAllByCreatedBy(userId);
    }
    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Mono<Order> submitOrder(String isbn , int quantity){
        return bookClient.grtBookByIsbn(isbn)
                .map(book -> buildAcceptBook(book , quantity))
                .defaultIfEmpty(
                    buildRejectOrder(isbn , quantity)
                ).flatMap(orderRepository::save)
                .doOnNext(this::publishOrderAcceptedEvent);
    }

    public static Order buildAcceptBook(Book book, int quantity) {
        return Order.of(
                book.isbn(),
                book.title() + "-" + book.author(),
                book.price(),
                quantity,
                OrderStatus.ACCEPTED);
    }

    public static Order buildRejectOrder(String isbn, int quantity) {
        return Order.of(isbn , null , null ,quantity , OrderStatus.REJECTED);
    }

    public Flux<Order> consumeOrderDispatchedEvent(Flux<OrderDispatchedMessage> flux) {
        return flux
                .flatMap(message -> orderRepository.findById(message.orderId()))
                .map(this::buildDispatchedOrder)
                .flatMap(orderRepository::save);
    }

    private Order buildDispatchedOrder(Order existingOrder) {
        return new Order(
                existingOrder.id(),
                existingOrder.bookIsbn(),
                existingOrder.bookName(),
                existingOrder.bookPrice(),
                existingOrder.quantity(),
                OrderStatus.DISPATCHED,
                existingOrder.createdDate(),
                existingOrder.lastModifiedDate(),
                existingOrder.createdBy(),
                existingOrder.lastModifiedBy(),
                existingOrder.version()
        );
    }

    public void publishOrderAcceptedEvent(Order order){
        if(!order.status().equals(OrderStatus.ACCEPTED)){
            return;
        }

        var orderAcceptedMessage = new OrderAcceptedMessage(order.id());

        log.info("Sending order accepted event with id: {}", order.id());
        var result = streamBridge.send("acceptOrder-out-0",
                orderAcceptedMessage);
        log.info("Result of sending data for order with id {}: {}",
                order.id(), result);

    }
}
