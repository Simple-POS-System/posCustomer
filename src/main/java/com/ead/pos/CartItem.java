package com.ead.pos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItem {
    private String productId;
    private int quantity;

    @Override
    public String toString() {
        return "CartItem{" +
                "productId='" + productId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
