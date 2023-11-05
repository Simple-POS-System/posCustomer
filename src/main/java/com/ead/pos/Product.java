package com.ead.pos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Product {
    private String productId;
    private String productName;
    private int quantity;
    private int unitPrice;

}

