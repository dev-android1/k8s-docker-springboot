package com.nagp.assignment.controller;

import com.nagp.assignment.entity.User;
import com.nagp.assignment.repository.UserRepository;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    private DataSource dataSource;
    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /**
     *  First version --  Welcome user, This is version for first demo
     *  Second version --  Welcome user, This is version for second demo to check rollout
     *
     */

    @GetMapping("/welcome")
    public String greetUser() {
        return "Welcome user, This is version for first demo";
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        if (userRepo.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        return userRepo.save(user);
    }

    @GetMapping("/pool-status")
    public String poolStatus() {
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        HikariPoolMXBean hikariPoolMXBean = hikariDataSource.getHikariPoolMXBean();
        return "Active: " + hikariPoolMXBean.getActiveConnections() +
                ", Idle: " + hikariPoolMXBean.getIdleConnections();
    }
}
