package com.ead.pos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class Order {
    private String orderId;
    private String customerId;
    private LocalDateTime orderTime;
    private List<CartItem> orderItems;
    private OrderStatus orderStatus;
    private String deliveryPersonId;
}
