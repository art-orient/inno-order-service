package com.innowise.orderservice.client;

import com.innowise.orderservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {

  @GetMapping("/api/users/by-email")
  UserDto getByEmail(@RequestParam("email") String email);

  @GetMapping("/api/users/{id}")
  UserDto getById(@PathVariable("id") Long id);
}