package org.example.marrakech.service;

import org.example.marrakech.entity.User;
import org.example.marrakech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  public void createUser(String username, String password) {
    User user = new User(username, password);
    userRepository.save(user);
    System.out.println("Пользователь создан: " + username);
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }
}
