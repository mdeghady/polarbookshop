package com.polarbookshop.orderservice.dto;

public record Book(
        String isbn,
        String title,
        String author,
        Double price
) {
}
