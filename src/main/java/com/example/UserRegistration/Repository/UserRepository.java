package com.example.UserRegistration.Repository;
import com.example.UserRegistration.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    
        Optional<User> findByEmail(String email);
    }

