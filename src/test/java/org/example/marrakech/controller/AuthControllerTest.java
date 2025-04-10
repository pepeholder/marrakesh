package org.example.marrakech.controller;

import org.example.marrakech.dto.LoginDTO;
import org.example.marrakech.dto.UserDTO;
import org.example.marrakech.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private UserService userService;

  @Mock
  private AuthenticationManager authenticationManager;

  @InjectMocks
  private AuthController authController;

  @Test
  void register_successful() {
    UserDTO userDTO = new UserDTO();
    userDTO.setUsername("lera");
    userDTO.setPassword("1234");

    ResponseEntity<?> response = authController.register(userDTO);

    verify(userService).createUser("lera", "1234");

    assertEquals(200, response.getStatusCode().value());
    assertEquals("User registered successfully", ((Map<?, ?>) Objects.requireNonNull(response.getBody())).get("message"));
  }

  @Test
  void register_exception() {
    UserDTO userDTO = new UserDTO();
    userDTO.setUsername("lera");
    userDTO.setPassword("1234");

    doThrow(new RuntimeException("User already exists"))
        .when(userService).createUser("lera", "1234");

    ResponseEntity<?> response = authController.register(userDTO);

    assertEquals(400, response.getStatusCode().value());
    assertEquals("User already exists", ((Map<?, ?>) Objects.requireNonNull(response.getBody())).get("error"));
  }

  @Test
  void login_successful() {
    LoginDTO loginDTO = new LoginDTO();
    loginDTO.setUsername("lera");
    loginDTO.setPassword("1234");

    ResponseEntity<?> response = authController.login(loginDTO);

    verify(authenticationManager).authenticate(
        new UsernamePasswordAuthenticationToken("lera", "1234")
    );

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Login successful", ((Map<?, ?>) Objects.requireNonNull(response.getBody())).get("message"));
  }

  @Test
  void login_invalidCredentials() {
    LoginDTO loginDTO = new LoginDTO();
    loginDTO.setUsername("lera");
    loginDTO.setPassword("wrong");

    doThrow(new BadCredentialsException("Invalid credentials"))
        .when(authenticationManager).authenticate(any());

    ResponseEntity<?> response = authController.login(loginDTO);

    assertEquals(401, response.getStatusCode().value());
    assertEquals("Invalid credentials", ((Map<?, ?>) Objects.requireNonNull(response.getBody())).get("error"));
  }
}
