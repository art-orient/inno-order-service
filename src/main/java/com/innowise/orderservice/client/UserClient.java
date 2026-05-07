package com.innowise.orderservice.client;

import com.innowise.orderservice.model.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", url = "${services.user.url}")
public interface UserClient {

  @GetMapping("/api/users/by-email")
  UserDto getByEmail(@RequestParam("email") String email);

  @GetMapping("/api/users/{id}")
  UserDto getById(@PathVariable("id") Long id);
}