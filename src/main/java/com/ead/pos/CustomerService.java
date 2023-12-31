package com.ead.pos;
import com.ead.pos.Exceptions.UserAlreadyExistsException;
import com.ead.pos.Exceptions.UserNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    RestTemplate restTemplate = new RestTemplate();


    public String generateUserId() {
        Customer lastCustomer = customerRepository.findFirstByOrderByUserIdDesc();
        if (lastCustomer != null) {
            int lastUserId = Integer.parseInt(lastCustomer.getUserId().substring(1));
            int newUserId = lastUserId + 1;
            return "U" + newUserId;
        } else {
            return "U1";
        }
    }

    public ResponseEntity<String> saveCustomer(Customer customer) {
        try {
            Customer existingCustomer = customerRepository.findByEmail(customer.getEmail());
            if (existingCustomer != null) {
                throw new UserAlreadyExistsException("Customer with email: " + customer.getEmail() + " is already exist");
            }
            customer.setUserId(generateUserId());
            customer.setOrderStatus(Customer.OrderStatus.NOT_APPLICABLE);
            customerRepository.save(customer);
            return ResponseEntity.ok("Customer saved successfully");
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<String> updateCustomer(Customer customer, String userId) {
        try {
            Customer existingCustomer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            existingCustomer.setFirstName(customer.getFirstName());
            existingCustomer.setLastName(customer.getLastName());
            existingCustomer.setContactNumber(customer.getContactNumber());
            existingCustomer.setAddress(customer.getAddress());
            customerRepository.save(existingCustomer);
            return ResponseEntity.ok("Customer updated successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(String userId) throws UserNotFoundException {
        return customerRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    public ResponseEntity<String> deleteCustomerById(String userId) {
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            customerRepository.delete(customer);
            return ResponseEntity.ok("Customer deleted successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<?> getCartItems(String userId) throws UserNotFoundException {
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            if (customer.getCartItems() == null) {
                return ResponseEntity.ok(new CartItem());
            }
            return ResponseEntity.ok(customer.getCartItems());
        } catch (UserNotFoundException e) {
            return null;
        }
    }

    public ResponseEntity<String> userCartAdd(CartItem cartItem, String userId) {
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            String productId = cartItem.getProductId();
            String productUrl = "http://localhost:8070/product/getById/" + productId;
            ResponseEntity<String> productResponse = restTemplate.getForEntity(productUrl, String.class);
            if (productResponse.getStatusCode().is2xxSuccessful()) {
                if (productResponse.getBody() == null) {
                    return ResponseEntity.badRequest().body("Product not found with id: " + productId);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                Product productDetails = objectMapper.readValue(productResponse.getBody(), Product.class);
                if(productDetails.getQuantity() < cartItem.getQuantity()){
                    return ResponseEntity.badRequest().body("Product quantity is not enough");
                }
                List<CartItem> currentCartItems = customer.getCartItems();
                if (currentCartItems == null) {
                    currentCartItems = new ArrayList<>();
                    customer.setCartItems(currentCartItems);
                }
                cartItem.setUnitPrice(productDetails.getUnitPrice());
                currentCartItems.add(cartItem);
                customer.setTotalCost(customer.getTotalCost() + (productDetails.getUnitPrice() * cartItem.getQuantity()));
                customerRepository.save(customer);
                return ResponseEntity.ok("Cart item added successfully");
            } else {
                return ResponseEntity.badRequest().body("Error fetching product details");
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> userCartUpdate(CartItem cartItem, String userId) {
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            String productId = cartItem.getProductId();
            String productUrl = "http://localhost:8070/product/getById/" + productId;
            ResponseEntity<String> productResponse = restTemplate.getForEntity(productUrl, String.class);
            if (productResponse.getStatusCode().is2xxSuccessful()) {
                if (productResponse.getBody() == null) {
                    return ResponseEntity.badRequest().body("Product not found with id: " + productId);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                Product productDetails = objectMapper.readValue(productResponse.getBody(), Product.class);
                if(productDetails.getQuantity() < cartItem.getQuantity()){
                    return ResponseEntity.badRequest().body("Product quantity is not enough");
                }
                List<CartItem> currentCartItems = customer.getCartItems();
                if (currentCartItems == null) {
                    return ResponseEntity.badRequest().body("Cart is empty");
                }
                for (CartItem item : currentCartItems) {
                    if (item.getProductId().equals(cartItem.getProductId())) {
                        customer.setTotalCost(customer.getTotalCost() - (productDetails.getUnitPrice() * item.getQuantity()));
                        item.setQuantity(cartItem.getQuantity());
                        customer.setTotalCost(customer.getTotalCost() + (productDetails.getUnitPrice() * cartItem.getQuantity()));
                        customerRepository.save(customer);
                        return ResponseEntity.ok("Cart item updated successfully");
                    }
                }
                return ResponseEntity.badRequest().body("Cart item not found");
            } else {
                return ResponseEntity.badRequest().body("Error fetching product details");
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> removeCartItem (String userId, String productId){
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            List<CartItem> currentCartItems = customer.getCartItems();
            if (currentCartItems == null) {
                return ResponseEntity.badRequest().body("Cart is empty");
            }
            for (CartItem item : currentCartItems) {
                if (item.getProductId().equals(productId)) {
                    customer.setTotalCost(customer.getTotalCost() - (item.getUnitPrice() * item.getQuantity()));
                    currentCartItems.remove(item);
                    customerRepository.save(customer);
                    return ResponseEntity.ok("Cart item removed successfully");
                }
            }
            return ResponseEntity.badRequest().body("Cart item not found");
        }catch (UserNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<?> getOrderStatus(String userId) {
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            return ResponseEntity.ok(customer.getOrderStatus().toString());
        }catch (UserNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }

    public ResponseEntity<?> setOrderStatus (String userId, String status){
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            if(status.equals("PACKING") || status.equals("READY_TO_DISPATCH") || status.equals("SHIPPED") || status.equals("DELIVERED") || status.equals("NOT_APPLICABLE")){
                customer.setOrderStatus(Customer.OrderStatus.valueOf(status));
                customerRepository.save(customer);
                return ResponseEntity.ok("Order status updated successfully");
            }
            return ResponseEntity.badRequest().body("Invalid order status");
        }catch (UserNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }

    public ResponseEntity<?> placeOrder(String userId){
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            List<CartItem> currentCartItems = customer.getCartItems();
            if (currentCartItems == null) {
                return ResponseEntity.badRequest().body("Cart is empty");
            }
            for (CartItem item : currentCartItems) {
                String productId = item.getProductId();
                String productUrl = "http://localhost:8070/product/checkAvailability/" + productId + "/" + item.getQuantity();
                ResponseEntity<Integer> productResponse = restTemplate.getForEntity(productUrl, Integer.class);
                if (productResponse.getStatusCode().is2xxSuccessful()) {
                    if (productResponse.getBody() == null) {
                        return ResponseEntity.badRequest().body("Product not found with id: " + productId);
                    }
                    if(productResponse.getBody() == 0){
                        removeCartItem(userId,productId);
                        return ResponseEntity.badRequest().body("Product "+item.getProductId()+" quantity is not enough. Please try again");
                    }
                } else {
                    return ResponseEntity.badRequest().body("Error fetching product details");
                }
            }
            for(CartItem item: currentCartItems){
                String productId = item.getProductId();
                String productUrl = "http://localhost:8070/product/updateQuantity/" + productId + "/" + item.getQuantity();
                restTemplate.put(productUrl,null);
            }
            Order order = new Order();
            order.setCustomerId(userId);
            order.setOrderItems(currentCartItems);
            String orderUrl = "http://localhost:8060/order/add";
            ResponseEntity<Order> savedOrder = restTemplate.postForEntity(orderUrl,order,Order.class);
            System.out.println("Order ID: " + Objects.requireNonNull(savedOrder.getBody()).getOrderId());
            customer.getActiveOrders().add(Objects.requireNonNull(savedOrder.getBody()).getOrderId());
            customer.setOrderStatus(Customer.OrderStatus.IN_QUEUE);
            return ResponseEntity.ok("Order placed successfully");
        }catch (UserNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }

    public ResponseEntity<String> clearCart(String userId){
        try {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            customer.setCartItems(null);
            customerRepository.save(customer);
            return ResponseEntity.ok("Cart cleared successfully");
        }catch (UserNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
