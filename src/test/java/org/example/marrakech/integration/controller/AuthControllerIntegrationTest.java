package org.example.marrakech.integration.controller;

import jakarta.transaction.Transactional;
import org.example.marrakech.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  private String getUrl(String path) {
    return "http://localhost:" + port + "/api/auth" + path;
  }

  @Test
  public void testRegisterUser_Success() {
    UserDTO user = new UserDTO();
    user.setUsername("testuser3");
    user.setPassword("testpass3");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<UserDTO> request = new HttpEntity<>(user, headers);

    ResponseEntity<String> response = restTemplate.postForEntity(getUrl("/register"), request, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(Objects.requireNonNull(response.getBody()).contains("User registered successfully"));
  }

  @Test
  public void testLoginUser_Success() {
    UserDTO user = new UserDTO();
    user.setUsername("testuser2");
    user.setPassword("testpass2");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<UserDTO> registerRequest = new HttpEntity<>(user, headers);
    restTemplate.postForEntity(getUrl("/register"), registerRequest, String.class);

    HttpEntity<UserDTO> loginRequest = new HttpEntity<>(user, headers);
    ResponseEntity<String> loginResponse = restTemplate.postForEntity(getUrl("/login"), loginRequest, String.class);

    assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
    assertTrue(Objects.requireNonNull(loginResponse.getBody()).contains("Login successful"));
  }

  @Test
  public void testLoginUser_Fail() {
    UserDTO user = new UserDTO();
    user.setUsername("nonexistent");
    user.setPassword("wrongpass");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<UserDTO> loginRequest = new HttpEntity<>(user, headers);
    ResponseEntity<String> loginResponse = restTemplate.postForEntity(getUrl("/login"), loginRequest, String.class);

    assertEquals(HttpStatus.UNAUTHORIZED, loginResponse.getStatusCode());
    assertTrue(Objects.requireNonNull(loginResponse.getBody()).contains("Invalid credentials"));
  }
}
