package com.ead.pos;
import com.ead.pos.Exceptions.ProductNotFoundException;
import com.ead.pos.Exceptions.UserAlreadyExistsException;
import com.ead.pos.Exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

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

    public ResponseEntity<String> deteleCustomerById(String userId) {
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
            List<CartItem> cartItems = customer.getCartItems();
            if (cartItems == null) {
                cartItems = new ArrayList<>();
                customer.setCartItems(cartItems);
            }
            cartItems.add(cartItem);
            customerRepository.save(customer);

            return ResponseEntity.ok("Cart item added successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public ResponseEntity<?> userCartUpdate(CartItem cartItem, String userId) {
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        List<CartItem> cartItems = customer.getCartItems();
        if (cartItems != null) {
            boolean isItemPresent = false;
            for (CartItem item : cartItems) {
                if (item.getProductId().equals(cartItem.getProductId())) {
                    item.setQuantity(cartItem.getQuantity());
                    isItemPresent = true;
                    break;
                }
            }
            if (!isItemPresent) {
                throw new ProductNotFoundException("Product not found with id: " + cartItem.getProductId());
            }
        }
        customerRepository.save(customer);
        return ResponseEntity.ok(customer.getCartItems());
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
