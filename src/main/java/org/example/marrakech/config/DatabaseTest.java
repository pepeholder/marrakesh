package org.example.marrakech.config;


import org.example.marrakech.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class DatabaseTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

//  @Autowired
//  private UserService userService;
//
//  @PostConstruct
//  public void checkConnection() {
//    userService.createUser("Vika6", "password34515");
//  }
}
