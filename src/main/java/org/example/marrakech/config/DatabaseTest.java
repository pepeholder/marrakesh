package org.example.marrakech.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class DatabaseTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @PostConstruct
  public void checkConnection() {
    try {
      String result = jdbcTemplate.queryForObject("SELECT version();", String.class);
      System.out.println("✅ Подключение к базе успешно! Версия PostgreSQL: " + result);
    } catch (Exception e) {
      System.err.println("❌ Ошибка подключения к базе: " + e.getMessage());
    }
  }
}
