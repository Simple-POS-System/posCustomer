package com.ead.pos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "customers")
public class Customer {
    @Id
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private String address;
    private List<CartItem> cartItems;
    private int totalCost;
    private OrderStatus orderStatus;
    private List<String> activeOrders;


    public enum OrderStatus {
        IN_QUEUE,
        PACKING,
        READY_TO_DISPATCH,
        SHIPPED,
        DELIVERED,
        NOT_APPLICABLE
    }

    @Override
    public String toString() {
        return "Customer{" +
                "userId='" + userId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", address='" + address + '\'' +
                ", cartItems=" + cartItems +
                ", totalCost=" + totalCost +
                ", orderStatus=" + orderStatus +
                '}';
    }
}
