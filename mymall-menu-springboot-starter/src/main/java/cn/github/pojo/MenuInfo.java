package cn.github.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class MenuInfo {
    private Integer id;
    private String menuCode;
    private String menuName;
    private String menuDesc;
    private String menuUrl;
    private String parentCode;
    private MenuInfo parent;
    private List<MenuInfo> children;
}
