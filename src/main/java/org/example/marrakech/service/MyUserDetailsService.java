package org.example.marrakech.service;

import org.example.marrakech.entity.User;
import org.example.marrakech.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public MyUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User appUser = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    // Построим UserDetails с простой ролью USER. При необходимости можно добавить роли.
    return org.springframework.security.core.userdetails.User
        .withUsername(appUser.getUsername())
        .password(appUser.getPasswordHash())
        .roles("USER")
        .build();
  }
}
