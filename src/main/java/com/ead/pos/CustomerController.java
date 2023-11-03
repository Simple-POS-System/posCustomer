package com.ead.pos;

import com.ead.pos.Exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @PostMapping("/add")
    public ResponseEntity<String> addCustomer(@RequestBody Customer customer){
        return customerService.saveCustomer(customer);
    }

    @GetMapping("/getAll")
    public List<Customer> getAllCustomers(){
        return customerService.getAllCustomers();
    }

    @GetMapping("/getById/{userId}")
    public ResponseEntity<?> getCustomerById(@PathVariable String userId) {
        try {
            Customer customer = customerService.getCustomerById(userId);
            return ResponseEntity.ok(customer);
        } catch (UserNotFoundException e) {
            return null;
        }
    }

    @PutMapping("/updateDetails/{userId}")
    public ResponseEntity<String> updateCustomer(@RequestBody Customer customer,@PathVariable String userId){
        return customerService.updateCustomer(customer,userId);
    }

    @DeleteMapping("/deleteById/{userId}")
    public ResponseEntity<String> deleteCustomerById(@PathVariable String userId) {
        return customerService.deteleCustomerById(userId);
    }

    @GetMapping("/getCart/{userId}")
    public ResponseEntity<?> getCart(@PathVariable String userId){
        return customerService.getCartItems(userId);
    }

    @PostMapping("/addToCart/{userId}")
    public ResponseEntity<String> addToCart(@RequestBody CartItem cartItem, @PathVariable String userId) {
        return customerService.userCartAdd(cartItem, userId);
    }

    @PutMapping("/updateCart/{userId}")
    public ResponseEntity<?> updateCart(@RequestBody CartItem cartItem,@PathVariable String userId){
        try {
            return customerService.userCartUpdate(cartItem,userId);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getOrderStatus/{userId}")
    public ResponseEntity<?> getOrderStatus(@PathVariable String userId){
        return customerService.getOrderStatus(userId);
    }

    @DeleteMapping("/deleteCart/{userId}")
    public ResponseEntity<?> deleteCart(@PathVariable String userId){
        return customerService.clearCart(userId);
    }
}
