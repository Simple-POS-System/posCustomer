package com.ead.pos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {
    private String productId;
    private String productName;
    private int quantity;
    private int unitPrice;

}
