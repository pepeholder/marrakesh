package org.example.marrakech.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class HomeController {

  @GetMapping("/home")
  public String home(Model model) {
    String message = "Добро пожаловать в приложение!";
    System.out.println("Добавлен атрибут message: " + message);
    model.addAttribute("message", message);
    return "home";
  }
}

