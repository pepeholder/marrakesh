package org.example.marrakech.controller;

import org.example.marrakech.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistrationController {

  private final UserService userService;

  public RegistrationController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/register")
  public String showRegistrationForm() {
    return "register";
  }

  @PostMapping("/register")
  public String registerUser(@RequestParam String username,
                             @RequestParam String password,
                             Model model) {
    // В простейшей реализации создаём пользователя без валидации
    try {
      userService.createUser(username, password);
      return "redirect:/login";
    } catch (Exception e) {
      model.addAttribute("error", "Ошибка регистрации: " + e.getMessage());
      return "register";
    }
  }
}
