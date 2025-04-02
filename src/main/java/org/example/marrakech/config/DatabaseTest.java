package org.example.marrakech.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class DatabaseTest {

  @Autowired
  public DatabaseTest(JdbcTemplate jdbcTemplate) {
  }

  @PostConstruct
  public void checkConnection() {
    System.out.println("Соединение с базой данных установлено.");
  }
}
