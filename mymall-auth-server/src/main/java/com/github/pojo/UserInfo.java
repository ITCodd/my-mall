package com.github.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfo {
    private Integer id;
    private String username;
    private Integer status;
}
