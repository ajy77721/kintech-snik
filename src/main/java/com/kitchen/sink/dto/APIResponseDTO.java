package com.kitchen.sink.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Ignore null fields
public class APIResponseDTO<T> {
    private boolean status;
    private ErrorDTO error;
    private T data;
}
