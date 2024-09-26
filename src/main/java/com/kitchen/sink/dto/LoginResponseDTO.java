package com.kitchen.sink.dto;

import lombok.Builder;

@Builder
public record LoginResponseDTO(String token, String email) {
}
