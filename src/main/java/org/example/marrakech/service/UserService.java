package org.example.marrakech.service;

import org.example.marrakech.entity.User;
import org.example.marrakech.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public void createUser(String username, String password) {
    String passwordHash = passwordEncoder.encode(password);
    User user = new User(username, passwordHash);
    userRepository.save(user);
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public User findByUsername(String username) {
    return userRepository.findByUsername(username).orElse(null);
  }
}
