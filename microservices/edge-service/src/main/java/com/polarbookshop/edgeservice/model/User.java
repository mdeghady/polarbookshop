package com.polarbookshop.edgeservice.model;

import java.util.List;

public record User(
        String username,
        String firstName,
        String lastName,
        List<String> roles
) {
}
