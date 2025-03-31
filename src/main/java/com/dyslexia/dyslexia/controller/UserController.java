package com.dyslexia.dyslexia.controller;

import com.dyslexia.dyslexia.model.User;
import com.dyslexia.dyslexia.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @PostMapping
  public User create(@RequestBody User user) {
    return userRepository.save(user);
  }

  @GetMapping
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @GetMapping("/{email}")
  public User findByEmail(@PathVariable String email) {
    return userRepository.findByEmail(email);
  }
}
