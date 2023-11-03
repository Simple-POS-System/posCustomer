package com.ead.pos;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    Customer findFirstByOrderByUserIdDesc();
    Customer findByEmail(String email);


}
