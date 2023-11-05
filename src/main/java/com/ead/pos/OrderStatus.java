package com.ead.pos;

import lombok.Getter;
import lombok.Setter;

public enum OrderStatus {
    PACKING,
    READY_TO_DISPATCH,
    SHIPPED,
    DELIVERED
}
