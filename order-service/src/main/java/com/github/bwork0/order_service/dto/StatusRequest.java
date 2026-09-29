package com.github.bwork0.order_service.dto;

import com.github.bwork0.order_service.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull OrderStatus status) {
}