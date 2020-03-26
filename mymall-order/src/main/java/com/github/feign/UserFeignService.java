package com.github.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "mymall-user-center")
public interface UserFeignService {

    @PostMapping(value = "/user/info")
    String hello(@RequestHeader("Authorization") String Authorization );

    @PostMapping(value = "/user/info")
    String hello();
}