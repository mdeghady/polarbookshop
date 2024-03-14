package com.polarbookshop.orderservice.service;

import com.polarbookshop.orderservice.dto.Book;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class BookCatalogService {

    private static final String BOOKS_ROOT_API = "/books/";
    private final WebClient webClient;

    public BookCatalogService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Book> grtBookByIsbn(String isbn){
        return webClient
                .get()
                .uri(BOOKS_ROOT_API + isbn)
                .retrieve()
                .bodyToMono(Book.class)
                .timeout(Duration.ofSeconds(3) , Mono.empty())
                .retryWhen(
                        Retry.backoff(3 , Duration.ofMillis(100))
                )
                .onErrorResume(Exception.class, exception -> Mono.empty());
    }

}
