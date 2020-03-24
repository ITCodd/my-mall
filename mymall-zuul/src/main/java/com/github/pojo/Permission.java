package com.github.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Permission {
    private String serviceId;
    private String url;
    private String menu;
    private Boolean access;

}
